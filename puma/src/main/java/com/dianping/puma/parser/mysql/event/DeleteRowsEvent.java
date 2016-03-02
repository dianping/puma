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

import com.dianping.puma.common.PumaContext;
import com.dianping.puma.parser.mysql.Row;
import com.dianping.puma.utils.PacketUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.BitSet;
import java.util.LinkedList;
import java.util.List;

/**
 * TODO Comment of DeleteRowsEvent
 * 
 * @author Leo Liang
 * 
 */
public class DeleteRowsEvent extends AbstractRowsEvent {

	private final Logger logger = LoggerFactory.getLogger(DeleteRowsEvent.class);

	private static final long serialVersionUID = -4574646483606347256L;

	private BitSet usedColumns;

	private List<Row> rows;

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
		return "DeleteRowsEvent [usedColumns=" + usedColumns + ", rows=" + rows + ", super.toString()="
		      + super.toString() + "]";
	}

	@Override
	protected void innerParse(ByteBuffer buf, PumaContext context) throws IOException {
		tableMapEvent = context.getTableMaps().get(tableId);
		usedColumns = PacketUtils.readBitSet(buf, columnCount.intValue());

		logger.debug("binlog event before parse rows:\n");
		logger.debug("{}", this);

		if (usedColumns.length() != columnCount.intValue()) {
			throw new RuntimeException("Illegal column image.");
		}

		rows = parseRows(buf, context);
	}

	protected List<Row> parseRows(ByteBuffer buf, PumaContext context) throws IOException {
		final List<Row> r = new LinkedList<Row>();
		while (isRemaining(buf, context)) {
			r.add(parseRow(buf, usedColumns));
		}
		return r;
	}
}
