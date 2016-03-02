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
import com.google.common.primitives.UnsignedLong;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * TODO Comment of IntvarEvent
 * 
 * @author Leo Liang
 * 
 */
public class IntVarEvent extends AbstractBinlogEvent {
	private static final long	serialVersionUID	= 5285705565213997195L;
	private byte				type;
	private UnsignedLong value;

	/**
	 * @return the type
	 */
	public byte getType() {
		return type;
	}

	/**
	 * @return the value
	 */
	public UnsignedLong getValue() {
		return value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "IntvarEvent [type=" + type + ", value=" + value + ", super.toString()=" + super.toString() + "]";
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
		type = buf.get();
		value = UnsignedLong.fromLongBits(PacketUtils.readLong(buf, 8));
	}

}
