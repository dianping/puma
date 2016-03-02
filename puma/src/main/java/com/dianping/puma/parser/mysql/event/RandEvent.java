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
 * TODO Comment of RandEvent
 * 
 * @author Leo Liang
 * 
 */
public class RandEvent extends AbstractBinlogEvent {

	private static final long	serialVersionUID	= -752135358243417659L;
	private long				randSeed1;
	private long				randSeed2;

	/**
	 * @return the randSeed1
	 */
	public long getRandSeed1() {
		return randSeed1;
	}

	/**
	 * @return the randSeed2
	 */
	public long getRandSeed2() {
		return randSeed2;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "RandEvent [randSeed1=" + randSeed1 + ", randSeed2=" + randSeed2 + ", super.toString()="
				+ super.toString() + "]";
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
		randSeed1 = PacketUtils.readLong(buf, 8);
		randSeed2 = PacketUtils.readLong(buf, 8);
	}

}
