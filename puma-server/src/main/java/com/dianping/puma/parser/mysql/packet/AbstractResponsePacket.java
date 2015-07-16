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

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import com.dianping.puma.common.PumaContext;
import com.dianping.puma.utils.PacketUtils;

/**
 * 
 * @author Leo Liang
 * 
 */
public abstract class AbstractResponsePacket extends AbstractPacket implements ResponsePacket {
	private static final long serialVersionUID = 3648947016393523542L;

	protected void readHeader(InputStream is) throws IOException {
		byte[] buf = new byte[4];
		int lenRead = PacketUtils.readFully(is, buf, 0, 4);

		if (lenRead < 4) {
			// TODO close
			throw new IOException("Unexpected end of input stream");
		}

		ByteBuffer byteBuffer = ByteBuffer.wrap(buf);
		length = PacketUtils.readInt(byteBuffer, 3);
		seq = PacketUtils.readInt(byteBuffer, 1);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dianping.puma.server.mysql.packet.Packet#readPacket(java.io.InputStream
	 * )
	 */
	@Override
	public void readPacket(InputStream is, PumaContext context) throws IOException {
		readHeader(is);
		byte[] buf = new byte[length];
		int lenRead = 0;
		lenRead = PacketUtils.readFully(is, buf, 0, length);

		if (lenRead != length) {
			throw new IOException("Short read, expected " + length + " bytes, only read " + lenRead);
		}

		ByteBuffer byteBuffer = ByteBuffer.wrap(buf);
		doReadPacket(byteBuffer, context);

	}

	protected abstract void doReadPacket(ByteBuffer buf, PumaContext context) throws IOException;
}
