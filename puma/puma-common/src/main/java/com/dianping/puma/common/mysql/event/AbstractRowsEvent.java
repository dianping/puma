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
package com.dianping.puma.common.mysql.event;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import org.apache.commons.lang.exception.NestableRuntimeException;

import com.dianping.puma.common.bo.PumaContext;
import com.dianping.puma.common.datatype.UnsignedLong;
import com.dianping.puma.common.mysql.BinlogConstanst;
import com.dianping.puma.common.mysql.Metadata;
import com.dianping.puma.common.mysql.Row;
import com.dianping.puma.common.mysql.column.BitColumn;
import com.dianping.puma.common.mysql.column.BlobColumn;
import com.dianping.puma.common.mysql.column.Column;
import com.dianping.puma.common.mysql.column.DateColumn;
import com.dianping.puma.common.mysql.column.DatetimeColumn;
import com.dianping.puma.common.mysql.column.DecimalColumn;
import com.dianping.puma.common.mysql.column.DoubleColumn;
import com.dianping.puma.common.mysql.column.EnumColumn;
import com.dianping.puma.common.mysql.column.FloatColumn;
import com.dianping.puma.common.mysql.column.Int24Column;
import com.dianping.puma.common.mysql.column.IntColumn;
import com.dianping.puma.common.mysql.column.LongLongColumn;
import com.dianping.puma.common.mysql.column.NullColumn;
import com.dianping.puma.common.mysql.column.SetColumn;
import com.dianping.puma.common.mysql.column.ShortColumn;
import com.dianping.puma.common.mysql.column.StringColumn;
import com.dianping.puma.common.mysql.column.TimeColumn;
import com.dianping.puma.common.mysql.column.TimestampColumn;
import com.dianping.puma.common.mysql.column.TinyColumn;
import com.dianping.puma.common.mysql.column.YearColumn;
import com.dianping.puma.common.util.CodecUtils;
import com.dianping.puma.common.util.MySQLUtils;
import com.dianping.puma.common.util.PacketUtils;

/**
 * TODO Comment of AbstractRowsEvent
 * 
 * @author Leo Liang
 * 
 */
public abstract class AbstractRowsEvent extends AbstractBinlogEvent {
	private static final long	serialVersionUID	= 2658456786993670332L;
	protected long				tableId;
	protected int				reserved;
	protected UnsignedLong		columnCount;
	protected TableMapEvent		tableMapEvent;

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "AbstractRowsEvent [tableId=" + tableId + ", reserved=" + reserved + ", columnCount=" + columnCount
				+ ", super.toString()=" + super.toString() + "]";
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dianping.puma.common.mysql.event.AbstractBinlogEvent#doParse(java
	 * .nio.ByteBuffer, com.dianping.puma.common.bo.PumaContext)
	 */
	@Override
	public void doParse(ByteBuffer buf, PumaContext context) throws IOException {
		tableId = PacketUtils.readLong(buf, 6);
		reserved = PacketUtils.readInt(buf, 2);
		columnCount = PacketUtils.readLengthCodedUnsignedLong(buf);
		innderParse(buf, context);
	}

	protected abstract void innderParse(ByteBuffer buf, PumaContext context) throws IOException;

	protected Row parseRow(ByteBuffer buf, BitSet usedColumns) throws IOException {
		byte[] types = tableMapEvent.getColumnTypes();
		Metadata metadata = tableMapEvent.getColumnMetadata();
		BitSet nullColumns = PacketUtils.readBitSet(buf, types.length);
		List<Column> columns = new ArrayList<Column>(types.length);
		for (int i = 0; i < types.length; ++i) {
			int length = 0;
			int meta = metadata.getMetadata(i);
			int type = CodecUtils.toUnsigned(types[i]);
			if (type == BinlogConstanst.MYSQL_TYPE_STRING && meta > 256) {
				int meta0 = meta >> 8;
				int meta1 = meta & 0xFF;
				if ((meta0 & 0x30) != 0x30) {
					type = meta0 | 0x30;
					length = meta1 | (((meta0 & 0x30) ^ 0x30) << 4);
				} else {
					switch (meta0) {
						case BinlogConstanst.MYSQL_TYPE_SET:
						case BinlogConstanst.MYSQL_TYPE_ENUM:
						case BinlogConstanst.MYSQL_TYPE_STRING:
							type = meta0;
							length = meta1;
							break;
						default:
							throw new NestableRuntimeException("assertion failed, unknown column type: " + type);
					}
				}
			}

			if (!usedColumns.get(i)) {
				continue;
			} else if (nullColumns.get(i)) {
				columns.add(NullColumn.valueOf(type));
				continue;
			}

			switch (type) {
				case BinlogConstanst.MYSQL_TYPE_TINY:
					columns.add(TinyColumn.valueOf(PacketUtils.readInt(buf, 1)));
					break;
				case BinlogConstanst.MYSQL_TYPE_SHORT:
					columns.add(ShortColumn.valueOf(PacketUtils.readInt(buf, 2)));
					break;
				case BinlogConstanst.MYSQL_TYPE_INT24:
					columns.add(Int24Column.valueOf(PacketUtils.readInt(buf, 3)));
					break;
				case BinlogConstanst.MYSQL_TYPE_INT:
					columns.add(IntColumn.valueOf(PacketUtils.readInt(buf, 4)));
					break;
				case BinlogConstanst.MYSQL_TYPE_LONGLONG:
					columns.add(LongLongColumn.valueOf(PacketUtils.readLong(buf, 8)));
					break;
				case BinlogConstanst.MYSQL_TYPE_FLOAT:
					columns.add(FloatColumn.valueOf(Float.intBitsToFloat(PacketUtils.readInt(buf, 4))));
					break;
				case BinlogConstanst.MYSQL_TYPE_DOUBLE:
					columns.add(DoubleColumn.valueOf(Double.longBitsToDouble(PacketUtils.readLong(buf, 8))));
					break;
				case BinlogConstanst.MYSQL_TYPE_YEAR:
					columns.add(YearColumn.valueOf(MySQLUtils.toYear(PacketUtils.readInt(buf, 1))));
					break;
				case BinlogConstanst.MYSQL_TYPE_DATE:
					columns.add(DateColumn.valueOf(MySQLUtils.toDate(PacketUtils.readInt(buf, 3))));
					break;
				case BinlogConstanst.MYSQL_TYPE_TIME:
					columns.add(TimeColumn.valueOf(MySQLUtils.toTime(PacketUtils.readInt(buf, 3))));
					break;
				case BinlogConstanst.MYSQL_TYPE_TIMESTAMP:
					columns.add(TimestampColumn.valueOf(MySQLUtils.toTimestamp(PacketUtils.readLong(buf, 4))));
					break;
				case BinlogConstanst.MYSQL_TYPE_DATETIME:
					columns.add(DatetimeColumn.valueOf(MySQLUtils.toDatetime(PacketUtils.readLong(buf, 8))));
					break;
				case BinlogConstanst.MYSQL_TYPE_ENUM:
					columns.add(EnumColumn.valueOf(PacketUtils.readInt(buf, length)));
					break;
				case BinlogConstanst.MYSQL_TYPE_SET:
					columns.add(SetColumn.valueOf(PacketUtils.readLong(buf, length)));
					break;
				case BinlogConstanst.MYSQL_TYPE_STRING:
					final int stringLength = length < 256 ? PacketUtils.readInt(buf, 1) : PacketUtils.readInt(buf, 2);
					columns.add(StringColumn.valueOf(PacketUtils.readBytes(buf, stringLength)));
					break;
				case BinlogConstanst.MYSQL_TYPE_BIT:
					final int bitLength = (meta >> 8) * 8 + (meta & 0xFF);
					columns.add(BitColumn.valueOf(bitLength, PacketUtils.readBitSet(buf, bitLength)));
					break;
				case BinlogConstanst.MYSQL_TYPE_NEWDECIMAL:
					final int precision = meta & 0xFF;
					final int scale = meta >> 8;
					final int decimalLength = MySQLUtils.getDecimalBinarySize(precision, scale);
					columns.add(DecimalColumn.valueOf(
							MySQLUtils.toDecimal(precision, scale, PacketUtils.readBytes(buf, decimalLength)),
							precision, scale));
					break;
				case BinlogConstanst.MYSQL_TYPE_BLOB:
					final int blobLength = PacketUtils.readInt(buf, meta);
					columns.add(BlobColumn.valueOf(PacketUtils.readBytes(buf, blobLength)));
					break;
				case BinlogConstanst.MYSQL_TYPE_VARCHAR:
				case BinlogConstanst.MYSQL_TYPE_VAR_STRING:
					final int varcharLength = meta < 256 ? PacketUtils.readInt(buf, 1) : PacketUtils.readInt(buf, 2);
					columns.add(StringColumn.valueOf(PacketUtils.readBytes(buf, varcharLength)));
					break;
				default:
					throw new NestableRuntimeException("assertion failed, unknown column type: " + type);
			}
		}
		return new Row(columns);
	}
}
