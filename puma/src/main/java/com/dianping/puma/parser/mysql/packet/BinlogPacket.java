/**
 * Project: ${puma-server.aid}
 * 
 * File Created at 2012-6-12 $Id$
 * 
 * Copyright 2010 dianping.com. All rights reserved.
 * 
 * This software is the confidential and proprietary information of Dianping
 * Company. ("Confidential Information"). You shall not disclose such
 * Confidential Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with dianping.com.
 */
package com.dianping.puma.parser.mysql.packet;

import com.dianping.puma.common.PumaContext;
import com.dianping.puma.utils.PacketUtils;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * TODO Comment of OKErrorPacket
 * 
 * @author Leo Liang
 * 
 */
public class BinlogPacket extends AbstractResponsePacket {

	private static final long		serialVersionUID	= -3612746649357670630L;
	private static final byte		ERROR_FIELD_COUNT	= (byte) 0xff;
	private static final byte		OK_FIELD_COUNT		= 0x00;

	private boolean					ok					= false;

	private byte					fieldCount;
	private transient ByteBuffer	binlogBuf;

	/**
	 * @return the fieldCount
	 */
	public byte getFieldCount() {
		return fieldCount;
	}

	/**
	 * @param fieldCount
	 *            the fieldCount to set
	 */
	public void setFieldCount(byte fieldCount) {
		this.fieldCount = fieldCount;
	}

	public boolean isOk() {
		return ok;
	}

	public void setOk(boolean ok) {
		this.ok = ok;
	}

	/**
	 * @return the binlogBuf
	 */
	public ByteBuffer getBinlogBuf() {
		return binlogBuf;
	}

	/**
	 * @param binlogBuf
	 *            the binlogBuf to set
	 */
	public void setBinlogBuf(ByteBuffer binlogBuf) {
		this.binlogBuf = binlogBuf;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dianping.puma.server.mysql.packet.AbstractResponsePacket#doReadPacket
	 * (java.nio.ByteBuffer, com.dianping.puma.server.PumaContext)
	 */
	@Override
	protected void doReadPacket(ByteBuffer buf, PumaContext context) throws IOException {
		fieldCount = buf.get();
		if (ERROR_FIELD_COUNT == fieldCount) {
			ok = false;
		} else if (OK_FIELD_COUNT == fieldCount) {
			binlogBuf = ByteBuffer.wrap(PacketUtils.readBytes(buf, buf.remaining()));
			ok = true;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "BinlogPacket [ok=" + ok + ", fieldCount=" + fieldCount + ", binlogBuf=" + binlogBuf + "]";
	}
}
