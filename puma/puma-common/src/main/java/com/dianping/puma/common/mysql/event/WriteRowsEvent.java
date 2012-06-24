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
import java.util.BitSet;
import java.util.LinkedList;
import java.util.List;

import com.dianping.puma.common.bo.PumaContext;
import com.dianping.puma.common.mysql.Row;
import com.dianping.puma.common.util.PacketUtils;

/**
 * TODO Comment of WriteRowsEvent
 * 
 * @author Leo Liang
 * 
 */
public class WriteRowsEvent extends AbstractRowsEvent {
	private static final long	serialVersionUID	= 5158982187051056761L;
	private BitSet				usedColumns;
	private List<Row>			rows;

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "WriteRowsEvent [usedColumns=" + usedColumns + ", rows=" + rows + ", super.toString()="
				+ super.toString() + "]";
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

	@Override
	protected void innderParser(ByteBuffer buf, PumaContext context) throws IOException {
		usedColumns = PacketUtils.readBitSet(buf, columnCount.intValue());
		TableMapEvent tme = context.getTableMaps().get(tableId);
		rows = parseRows(buf, tme);

	}

	protected List<Row> parseRows(ByteBuffer buf, TableMapEvent tme) throws IOException {
		List<Row> r = new LinkedList<Row>();
		while (buf.hasRemaining()) {
			r.add(parseRow(buf, tme, usedColumns));
		}
		return r;
	}

}
