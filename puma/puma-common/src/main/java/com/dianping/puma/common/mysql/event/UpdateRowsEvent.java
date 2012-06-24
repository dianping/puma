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
import com.dianping.puma.common.mysql.Pair;
import com.dianping.puma.common.mysql.Row;
import com.dianping.puma.common.util.PacketUtils;

/**
 * TODO Comment of UpdateRowsEvent
 * 
 * @author Leo Liang
 * 
 */
public class UpdateRowsEvent extends AbstractRowsEvent {

	private static final long	serialVersionUID	= -877826157536949565L;
	private UnsignedLong		columnCount;
	private BitSet				usedColumnsBefore;
	private BitSet				usedColumnsAfter;
	private List<Pair<Row>>		rows;

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "UpdateRowsEvent [columnCount=" + columnCount + ", usedColumnsBefore=" + usedColumnsBefore
				+ ", usedColumnsAfter=" + usedColumnsAfter + ", rows=" + rows + ", super.toString()="
				+ super.toString() + "]";
	}

	/**
	 * @return the columnCount
	 */
	public UnsignedLong getColumnCount() {
		return columnCount;
	}

	/**
	 * @return the usedColumnsBefore
	 */
	public BitSet getUsedColumnsBefore() {
		return usedColumnsBefore;
	}

	/**
	 * @return the usedColumnsAfter
	 */
	public BitSet getUsedColumnsAfter() {
		return usedColumnsAfter;
	}

	/**
	 * @return the rows
	 */
	public List<Pair<Row>> getRows() {
		return rows;
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
		TableMapEvent tme = context.getTableMaps().get(tableId);
		reserved = PacketUtils.readInt(buf, 2);
		columnCount = PacketUtils.readLengthCodedUnsignedLong(buf);
		int bitSetLength = (int) ((columnCount.intValue() + 7) / 8);
		usedColumnsBefore = new BitSet(bitSetLength);
		PacketUtils.readBitSet(usedColumnsBefore, buf, columnCount.intValue());
		usedColumnsAfter = new BitSet(bitSetLength);
		PacketUtils.readBitSet(usedColumnsAfter, buf, columnCount.intValue());

		rows = parseRows(buf, tme);

	}

	protected List<Pair<Row>> parseRows(ByteBuffer buf, TableMapEvent tme) throws IOException {
		final List<Pair<Row>> r = new LinkedList<Pair<Row>>();
		while (buf.hasRemaining()) {
			final Row before = parseRow(buf, tme, usedColumnsBefore);
			final Row after = parseRow(buf, tme, usedColumnsAfter);
			r.add(new Pair<Row>(before, after));
		}
		return r;
	}

}
