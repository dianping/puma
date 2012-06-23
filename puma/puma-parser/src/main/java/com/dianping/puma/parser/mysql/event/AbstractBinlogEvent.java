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

import java.io.IOException;
import java.nio.ByteBuffer;

import com.dianping.puma.common.bo.PumaContext;

/**
 * TODO Comment of AbstractBinlogEvent
 * 
 * @author Leo Liang
 * 
 */
public abstract class AbstractBinlogEvent implements BinlogEvent {
	private static final long	serialVersionUID	= -8136236885229956889L;
	private BinlogHeader		header;

	@Override
	public void parse(ByteBuffer buf, PumaContext context, BinlogHeader header) throws IOException {
		this.header = header;
		doParse(buf, context);
	}

	public abstract void doParse(ByteBuffer buf, PumaContext context) throws IOException;

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "AbstractBinlogEvent [header=" + header + "]";
	}

}
