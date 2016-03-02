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
import com.dianping.puma.utils.PacketUtils;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * TODO Comment of RotateEvent
 * 
 * @author Leo Liang
 * 
 */
public class RotateEvent extends AbstractBinlogEvent {

	private static final long serialVersionUID = -3067716947078996892L;
	private long firstEventPosition;
	private String nextBinlogFileName;

	/**
	 * @return the firstEventPosition
	 */
	public long getFirstEventPosition() {
		return firstEventPosition;
	}

	/**
	 * @return the nextBinlogFileName
	 */
	public String getNextBinlogFileName() {
		return nextBinlogFileName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "RotateEvent [firstEventPosition=" + firstEventPosition + ", nextBinlogFileName=" + nextBinlogFileName
				+ ", super.toString()=" + super.toString() + "]";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dianping.puma.parser.mysql.event.AbstractBinlogEvent#doParse(java
	 * .nio.ByteBuffer, com.dianping.puma.common.bo.PumaContext)
	 */
	@Override
	public void doParse(ByteBuffer buf, PumaContext context) throws IOException {
		firstEventPosition = PacketUtils.readLong(buf, 8);
		int lenRemaining = lenRemaining(buf, context);
		nextBinlogFileName = PacketUtils.readFixedLengthString(buf, lenRemaining);
	}

}
