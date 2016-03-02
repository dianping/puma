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
import com.dianping.puma.parser.mysql.UpdatedRowData;
import com.dianping.puma.utils.PacketUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.BitSet;
import java.util.LinkedList;
import java.util.List;

/**
 * TODO Comment of UpdateRowsEvent
 * 
 * @author Leo Liang
 * 
 */
public class UpdateRowsEvent extends AbstractRowsEvent {

	private final Logger logger = LoggerFactory.getLogger(UpdateRowsEvent.class);

	private static final long serialVersionUID = -877826157536949565L;

	private BitSet usedColumnsBefore;

	private BitSet usedColumnsAfter;

	private List<UpdatedRowData<Row>> rows;

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("super", super.toString()).append("usedColumnsBefore",
				usedColumnsBefore)
		      .append("usedColumnsAfter", usedColumnsAfter).append("rows", rows).toString();
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
	protected void innerParse(ByteBuffer buf, PumaContext context) throws IOException {
		tableMapEvent = context.getTableMaps().get(tableId);
		usedColumnsBefore = PacketUtils.readBitSet(buf, columnCount.intValue());
		usedColumnsAfter = PacketUtils.readBitSet(buf, columnCount.intValue());

		logger.debug("binlog event before parse rows:\n");
		logger.debug("{}", this);

		if (usedColumnsBefore.length() != columnCount.intValue()) {
			throw new RuntimeException("Illegal before column image.");
		}

		if (usedColumnsAfter.length() != columnCount.intValue()) {
			throw new RuntimeException("Illegal after column image.");
		}

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
