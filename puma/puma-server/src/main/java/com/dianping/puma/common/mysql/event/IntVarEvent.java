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

import com.dianping.puma.common.bo.PumaContext;
import com.dianping.puma.common.util.PacketUtils;
import com.dianping.puma.core.datatype.UnsignedLong;

/**
 * TODO Comment of IntvarEvent
 * 
 * @author Leo Liang
 * 
 */
public class IntVarEvent extends AbstractBinlogEvent {
	private static final long	serialVersionUID	= 5285705565213997195L;
	private byte				type;
	private UnsignedLong		value;

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
		value = UnsignedLong.asUnsigned(PacketUtils.readLong(buf, 8));
	}

}
