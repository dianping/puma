/**
 * Project: ${puma-common.aid}
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
import java.util.BitSet;
import java.util.LinkedList;
import java.util.List;

import com.dianping.puma.common.bo.PumaContext;
import com.dianping.puma.common.datatype.UnsignedLong;
import com.dianping.puma.common.mysql.Row;
import com.dianping.puma.common.util.PacketUtils;

/**
 * TODO Comment of DeleteRowsEvent
 * 
 * @author Leo Liang
 * 
 */
public class DeleteRowsEvent extends AbstractRowsEvent {

	private static final long	serialVersionUID	= -4574646483606347256L;
	private UnsignedLong		columnCount;
	private BitSet				usedColumns;
	private List<Row>			rows;

	/**
	 * @return the columnCount
	 */
	public UnsignedLong getColumnCount() {
		return columnCount;
	}

	/**
	 * @return the usedColumns
	 */
	public BitSet getUsedColumns() {
		return usedColumns;
	}

	/**
	 * @return the rows
	 */
	public List<Row> getRows() {
		return rows;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "DeleteRowsEvent [columnCount=" + columnCount + ", usedColumns=" + usedColumns + ", rows=" + rows
				+ ", super.toString()=" + super.toString() + "]";
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
		final TableMapEvent tme = context.getTableMaps().get(tableId);
		reserved = PacketUtils.readInt(buf, 2);
		columnCount = PacketUtils.readLengthCodedUnsignedLong(buf);
		int bitSetLength = (int) ((columnCount.intValue() + 7) / 8);
		usedColumns = new BitSet(bitSetLength);
		PacketUtils.readBitSet(usedColumns, buf, columnCount.intValue());
		rows = parseRows(buf, tme);

	}

	protected List<Row> parseRows(ByteBuffer buf, TableMapEvent tme) throws IOException {
		final List<Row> r = new LinkedList<Row>();
		while (buf.hasRemaining()) {
			r.add(parseRow(buf, tme, usedColumns));
		}
		return r;
	}
}
