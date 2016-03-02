/**
 * Project: ${puma-parser.aid}
 * 
 * File Created at 2012-6-24
 * $Id$
 * 
 * Copyright 2010 dianping.com.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Dianping Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with dianping.com.
 */
package com.dianping.puma.parser.mysql.event;

import com.dianping.puma.common.PumaContext;
import com.dianping.puma.parser.mysql.BinlogConstants;
import com.dianping.puma.parser.mysql.Metadata;
import com.dianping.puma.parser.mysql.Row;
import com.dianping.puma.parser.mysql.column.*;
import com.dianping.puma.parser.mysql.utils.MySQLUtils;
import com.dianping.puma.utils.CodecUtils;
import com.dianping.puma.utils.PacketUtils;
import com.google.common.primitives.UnsignedLong;
import org.apache.commons.lang.exception.NestableRuntimeException;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

/**
 * TODO Comment of AbstractRowsEvent
 * 
 * @author Leo Liang
 * 
 */
public abstract class AbstractRowsEvent extends AbstractBinlogEvent {
	private static final long serialVersionUID = 2658456786993670332L;

	protected long tableId;

	protected int reserved;

	protected UnsignedLong columnCount;

	private int extraInfoLength;

	private byte extraInfo[];

	protected TableMapEvent tableMapEvent;

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("super", super.toString()).append("tableId", tableId)
		      .append("reserved", reserved).append("columnCount", columnCount).append("extraInfoLength", extraInfoLength)
		      .append("extraInfo", extraInfo).append("tableMapEvent", tableMapEvent).toString();
	}

	/**
	 * @return the columnCount
	 */
	public UnsignedLong getColumnCount() {
		return columnCount;
	}

	public long getTableId() {
		return tableId;
	}

	public void setTableId(long tableId) {
		this.tableId = tableId;
	}

	public int getReserved() {
		return reserved;
	}

	public void setReserved(int reserved) {
		this.reserved = reserved;
	}

	public void setExtraInfoLength(int extraInfoLength) {
		this.extraInfoLength = extraInfoLength;
	}

	public int getExtraInfoLength() {
		return extraInfoLength;
	}

	public void setExtraInfo(byte extraInfo[]) {
		this.extraInfo = extraInfo;
	}

	public byte[] getExtraInfo() {
		return extraInfo;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.puma.common.mysql.event.AbstractBinlogEvent#doParse(java .nio.ByteBuffer,
	 * com.dianping.puma.common.bo.PumaContext)
	 */
	@Override
	public void doParse(ByteBuffer buf, PumaContext context) throws IOException {
		tableId = PacketUtils.readLong(buf, 6);
		reserved = PacketUtils.readInt(buf, 2);
		if (getHeader().getEventType() == BinlogConstants.WRITE_ROWS_EVENT
		      || getHeader().getEventType() == BinlogConstants.DELETE_ROWS_EVENT
		      || getHeader().getEventType() == BinlogConstants.UPDATE_ROWS_EVENT) {
			extraInfoLength = PacketUtils.readInt(buf, 2);
			if (extraInfoLength > 2)
				extraInfo = PacketUtils.readBytes(buf, extraInfoLength - 2);
		}
		columnCount = PacketUtils.readLengthCodedUnsignedLong(buf);

		innerParse(buf, context);
	}

	protected abstract void innerParse(ByteBuffer buf, PumaContext context) throws IOException;

	/**
	 * 
	 * @see http://code.google.com/p/open-replicator/
	 * @param buf
	 * @param usedColumns
	 * @return
	 * @throws IOException
	 */
	protected Row parseRow(ByteBuffer buf, BitSet usedColumns) throws IOException {
		int unusedColumnCount = 0;
		byte[] types = tableMapEvent.getColumnTypes();
		Metadata metadata = tableMapEvent.getColumnMetadata();
		BitSet nullColumns = PacketUtils.readBitSet(buf, types.length);
		List<Column> columns = new ArrayList<Column>(types.length);
		for (int i = 0; i < types.length; ++i) {
			int length = 0;
			int meta = metadata.getMetadata(i);
			int type = CodecUtils.toUnsigned(types[i]);
			if (type == BinlogConstants.MYSQL_TYPE_STRING && meta > 256) {
				int meta0 = meta >> 8;
				int meta1 = meta & 0xFF;
				if ((meta0 & 0x30) != 0x30) {
					type = meta0 | 0x30;
					length = meta1 | (((meta0 & 0x30) ^ 0x30) << 4);
				} else {
					switch (meta0) {
					case BinlogConstants.MYSQL_TYPE_SET:
					case BinlogConstants.MYSQL_TYPE_ENUM:
					case BinlogConstants.MYSQL_TYPE_STRING:
						type = meta0;
						length = meta1;
						break;
					default:
						throw new NestableRuntimeException("assertion failed, unknown column type: " + type);
					}
				}
			}

			if (!usedColumns.get(i)) {
				unusedColumnCount++;
				continue;
			} else if (nullColumns.get(i - unusedColumnCount)) {
				columns.add(NullColumn.valueOf(type));
				continue;
			}

			int value = 0;
			switch (type) {
			case BinlogConstants.MYSQL_TYPE_TINY:
				value = PacketUtils.readInt(buf, 1);
				value = (value << 24) >> 24;
				columns.add(TinyColumn.valueOf(value));
				break;
			case BinlogConstants.MYSQL_TYPE_SHORT:
				value = PacketUtils.readInt(buf, 2);
				value = (value << 16) >> 16;
				columns.add(ShortColumn.valueOf(value));
				break;
			case BinlogConstants.MYSQL_TYPE_INT24:
				value = PacketUtils.readInt(buf, 3);
				value = (value << 8) >> 8;
				columns.add(Int24Column.valueOf(value));
				break;
			case BinlogConstants.MYSQL_TYPE_INT:
				columns.add(IntColumn.valueOf(PacketUtils.readInt(buf, 4)));
				break;
			case BinlogConstants.MYSQL_TYPE_LONGLONG:
				columns.add(LongLongColumn.valueOf(PacketUtils.readLong(buf, 8)));
				break;
			case BinlogConstants.MYSQL_TYPE_FLOAT:
				columns.add(FloatColumn.valueOf(Float.intBitsToFloat(PacketUtils.readInt(buf, 4))));
				break;
			case BinlogConstants.MYSQL_TYPE_DOUBLE:
				columns.add(DoubleColumn.valueOf(Double.longBitsToDouble(PacketUtils.readLong(buf, 8))));
				break;
			case BinlogConstants.MYSQL_TYPE_YEAR:
				columns.add(YearColumn.valueOf(MySQLUtils.toYear((short) PacketUtils.readInt(buf, 1))));
				break;
			case BinlogConstants.MYSQL_TYPE_DATE:
				columns.add(DateColumn.valueOf(MySQLUtils.toDate(PacketUtils.readInt(buf, 3))));
				break;
			case BinlogConstants.MYSQL_TYPE_TIME:
				columns.add(TimeColumn.valueOf(MySQLUtils.toTime(PacketUtils.readInt(buf, 3))));
				break;
			case BinlogConstants.MYSQL_TYPE_TIMESTAMP:
				columns.add(TimestampColumn.valueOf(PacketUtils.readLong(buf, 4)));
				break;
			case BinlogConstants.MYSQL_TYPE_DATETIME:
				columns.add(DatetimeColumn.valueOf(MySQLUtils.toDatetime(PacketUtils.readLong(buf, 8))));
				break;
			case BinlogConstants.MYSQL_TYPE_ENUM:
				columns.add(EnumColumn.valueOf(PacketUtils.readInt(buf, length)));
				break;
			case BinlogConstants.MYSQL_TYPE_SET:
				columns.add(SetColumn.valueOf(PacketUtils.readLong(buf, length)));
				break;
			case BinlogConstants.MYSQL_TYPE_STRING:
				final int stringLength = length < 256 ? PacketUtils.readInt(buf, 1) : PacketUtils.readInt(buf, 2);
				columns.add(StringColumn.valueOf(PacketUtils.readBytes(buf, stringLength)));
				break;
			case BinlogConstants.MYSQL_TYPE_BIT:
				final int bitLength = (meta >> 8) * 8 + (meta & 0xFF);
				columns.add(BitColumn.valueOf(bitLength, PacketUtils.readBit(buf, bitLength, false)));
				break;
			case BinlogConstants.MYSQL_TYPE_NEWDECIMAL:
				final int precision = meta & 0xFF;
				final int scale = meta >> 8;
				final int decimalLength = MySQLUtils.getDecimalBinarySize(precision, scale);
				columns.add(DecimalColumn.valueOf(
				      MySQLUtils.toDecimal(precision, scale, PacketUtils.readBytes(buf, decimalLength)), precision, scale));
				break;
			case BinlogConstants.MYSQL_TYPE_BLOB:
				final int blobLength = PacketUtils.readInt(buf, meta);
				columns.add(BlobColumn.valueOf(PacketUtils.readBytes(buf, blobLength)));
				break;
			case BinlogConstants.MYSQL_TYPE_VARCHAR:
			case BinlogConstants.MYSQL_TYPE_VAR_STRING:
				final int varcharLength = meta < 256 ? PacketUtils.readInt(buf, 1) : PacketUtils.readInt(buf, 2);
				columns.add(StringColumn.valueOf(PacketUtils.readBytes(buf, varcharLength)));
				break;
			case BinlogConstants.MYSQL_TYPE_TIME2:
				final int timeValue = PacketUtils.readInt(buf, 3, false);
				final int timeNanos = PacketUtils.readInt(buf, (meta + 1) / 2, false);
				columns.add(Time2Column.valueOf(MySQLUtils.toTime2(timeValue, timeNanos, meta)));
				break;
			case BinlogConstants.MYSQL_TYPE_DATETIME2:
				final long dateTimeValue = PacketUtils.readLong(buf, 5, false);
				final int dateTimenanos = PacketUtils.readInt(buf, (meta + 1) / 2, false);
				columns.add(Datetime2Column.valueOf(MySQLUtils.toDatetime2(dateTimeValue, dateTimenanos, meta)));
				break;
			case BinlogConstants.MYSQL_TYPE_TIMESTAMP2:
				final long timeStampValue = PacketUtils.readLong(buf, 4, false);
				final int timeStampNanos = PacketUtils.readInt(buf, (meta + 1) / 2, false);
				columns.add(Timestamp2Column.valueOf(MySQLUtils.toTimestamp2(timeStampValue, timeStampNanos, meta)));
				break;
			default:
				throw new NestableRuntimeException("assertion failed, unknown column type: " + type);
			}
		}
		return new Row(columns);
	}
}
