/**
 * Project: ${puma-server.aid}
 * 
 * File Created at 2012-6-7 $Id$
 * 
 * Copyright 2010 dianping.com. All rights reserved.
 * 
 * This software is the confidential and proprietary information of Dianping
 * Company. ("Confidential Information"). You shall not disclose such
 * Confidential Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with dianping.com.
 */
package com.dianping.puma.parser.mysql.packet;

/**
 * TODO Comment of AbstractPacket
 * 
 * @author Leo Liang
 * 
 */
public class AbstractPacket implements Packet {

	private static final long	serialVersionUID	= -6136841157699357610L;
	protected int				length;
	protected int				seq;

	public void setSeq(int seq) {
		this.seq = seq;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.puma.server.mysql.packet.Packet#length()
	 */
	@Override
	public int length() {
		return length;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.puma.server.mysql.packet.Packet#seq()
	 */
	@Override
	public int seq() {
		return seq;
	}
}
