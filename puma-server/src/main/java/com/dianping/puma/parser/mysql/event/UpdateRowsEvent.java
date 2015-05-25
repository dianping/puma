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
package com.dianping.puma.parser.mysql.event;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.BitSet;
import java.util.LinkedList;
import java.util.List;

import com.dianping.puma.bo.PumaContext;
import com.dianping.puma.parser.mysql.Row;
import com.dianping.puma.parser.mysql.UpdatedRowData;
import com.dianping.puma.utils.PacketUtils;

/**
 * TODO Comment of UpdateRowsEvent
 * 
 * @author Leo Liang
 * 
 */
public class UpdateRowsEvent extends AbstractRowsEvent {

	private static final long serialVersionUID = -877826157536949565L;
	private BitSet usedColumnsBefore;
	private BitSet usedColumnsAfter;
	private List<UpdatedRowData<Row>> rows;

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
	public List<UpdatedRowData<Row>> getRows() {
		return rows;
	}

	@Override
	protected void innderParse(ByteBuffer buf, PumaContext context) throws IOException {
		tableMapEvent = context.getTableMaps().get(tableId);
		usedColumnsBefore = PacketUtils.readBitSet(buf, columnCount.intValue());
		usedColumnsAfter = PacketUtils.readBitSet(buf, columnCount.intValue());

		rows = parseRows(buf, context);
	}

	protected List<UpdatedRowData<Row>> parseRows(ByteBuffer buf, PumaContext context) throws IOException {
		final List<UpdatedRowData<Row>> r = new LinkedList<UpdatedRowData<Row>>();
		while (isRemaining(buf, context)) {
			final Row before = parseRow(buf, usedColumnsBefore);
			final Row after = parseRow(buf, usedColumnsAfter);
			r.add(new UpdatedRowData<Row>(before, after));
		}
		return r;
	}

}
