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
import com.dianping.puma.parser.mysql.Metadata;
import com.dianping.puma.utils.PacketUtils;
import com.google.common.primitives.UnsignedLong;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.BitSet;

/**
 * TODO Comment of TableMapEvent
 * 
 * @author Leo Liang
 * 
 */
public class TableMapEvent extends AbstractBinlogEvent {

	private static final long serialVersionUID = -6294463562672565471L;

	private long tableId;

	private int reserved;

	private byte databaseNameLength;

	private String databaseName;

	private byte tableNameLength;

	private String tableName;

	private UnsignedLong columnCount;

	private byte[] columnTypes;

	private UnsignedLong columnMetadataCount;

	private Metadata columnMetadata;

	private BitSet columnNullabilities;

	/**
	 * @return the tableId
	 */
	public long getTableId() {
		return tableId;
	}

	/**
	 * @return the reserved
	 */
	public int getReserved() {
		return reserved;
	}

	/**
	 * @return the databaseNameLength
	 */
	public byte getDatabaseNameLength() {
		return databaseNameLength;
	}

	/**
	 * @return the databaseName
	 */
	public String getDatabaseName() {
		return databaseName;
	}

	/**
	 * @return the tableNameLength
	 */
	public byte getTableNameLength() {
		return tableNameLength;
	}

	/**
	 * @return the tableName
	 */
	public String getTableName() {
		return tableName;
	}

	/**
	 * @return the columnCount
	 */
	public UnsignedLong getColumnCount() {
		return columnCount;
	}

	/**
	 * @return the columnTypes
	 */
	public byte[] getColumnTypes() {
		return columnTypes;
	}

	/**
	 * @return the columnMetadataCount
	 */
	public UnsignedLong getColumnMetadataCount() {
		return columnMetadataCount;
	}

	/**
	 * @return the columnMetadata
	 */
	public Metadata getColumnMetadata() {
		return columnMetadata;
	}

	/**
	 * @return the columnNullabilities
	 */
	public BitSet getColumnNullabilities() {
		return columnNullabilities;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("super", super.toString()).append("tableId", tableId)
		      .append("reserved", reserved).append("databaseNameLength", databaseNameLength)
		      .append("databaseName", databaseName).append("tableNameLength", tableNameLength)
		      .append("tableName", tableName).append("columnCount", columnCount).append("columnTypes", columnTypes)
		      .append("columnMetadataCount", columnMetadataCount).append("columnMetadata", columnMetadata)
		      .append("columnNullabilities", columnNullabilities).toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.puma.parser.mysql.event.AbstractBinlogEvent#doParse(java .nio.ByteBuffer,
	 * com.dianping.puma.common.bo.PumaContext)
	 */
	@Override
	public void doParse(ByteBuffer buf, PumaContext context) throws IOException {
		tableId = PacketUtils.readLong(buf, 6);
		reserved = PacketUtils.readInt(buf, 2);
		databaseNameLength = buf.get();
		databaseName = PacketUtils.readNullTerminatedString(buf);
		tableNameLength = buf.get();
		tableName = PacketUtils.readNullTerminatedString(buf);
		columnCount = PacketUtils.readLengthCodedUnsignedLong(buf);
		columnTypes = PacketUtils.readBytes(buf, columnCount.intValue());
		columnMetadataCount = PacketUtils.readLengthCodedUnsignedLong(buf);

		columnMetadata = Metadata.valueOf(columnTypes, PacketUtils.readBytes(buf, columnMetadataCount.intValue()));
		columnNullabilities = PacketUtils.readBitSet(buf, columnCount.intValue());

		context.getTableMaps().put(tableId, this);
	}

}
