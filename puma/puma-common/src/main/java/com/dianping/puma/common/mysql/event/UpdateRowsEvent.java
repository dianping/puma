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
import com.dianping.puma.common.mysql.Row;
import com.dianping.puma.common.mysql.RowChangedData;
import com.dianping.puma.common.util.PacketUtils;

/**
 * TODO Comment of UpdateRowsEvent
 * 
 * @author Leo Liang
 * 
 */
public class UpdateRowsEvent extends AbstractRowsEvent {

	private static final long			serialVersionUID	= -877826157536949565L;
	private BitSet						usedColumnsBefore;
	private BitSet						usedColumnsAfter;
	private List<RowChangedData<Row>>	rows;

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "UpdateRowsEvent [usedColumnsBefore=" + usedColumnsBefore + ", usedColumnsAfter=" + usedColumnsAfter
				+ ", rows=" + rows + ", super.toString()=" + super.toString() + "]";
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
	public List<RowChangedData<Row>> getRows() {
		return rows;
	}

	@Override
	protected void innderParser(ByteBuffer buf, PumaContext context) throws IOException {
		TableMapEvent tme = context.getTableMaps().get(tableId);
		usedColumnsBefore = PacketUtils.readBitSet(buf, columnCount.intValue());
		usedColumnsAfter = PacketUtils.readBitSet(buf, columnCount.intValue());

		rows = parseRows(buf, tme);
	}

	protected List<RowChangedData<Row>> parseRows(ByteBuffer buf, TableMapEvent tme) throws IOException {
		final List<RowChangedData<Row>> r = new LinkedList<RowChangedData<Row>>();
		while (buf.hasRemaining()) {
			final Row before = parseRow(buf, tme, usedColumnsBefore);
			final Row after = parseRow(buf, tme, usedColumnsAfter);
			r.add(new RowChangedData<Row>(before, after));
		}
		return r;
	}

}
