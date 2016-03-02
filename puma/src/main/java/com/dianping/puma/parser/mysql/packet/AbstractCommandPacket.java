/**
 * Project: ${puma-server.aid}
 * 
 * File Created at 2012-6-8 $Id$
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
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * @author Leo Liang
 * 
 */
public abstract class AbstractCommandPacket extends AbstractPacket implements CommandPacket {

	private static final long	serialVersionUID	= -4515154194045893692L;
	protected byte[]			head;
	protected byte[]			body;
	protected byte				command;

	public AbstractCommandPacket(byte command) {
		this.command = command;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.puma.server.packet.CommandPacket#buildPacket()
	 */
	@Override
	public void buildPacket(PumaContext context) throws IOException {
		ByteBuffer bodyBuffer = doBuild(context);
		length = bodyBuffer.position();
		body = new byte[length];
		bodyBuffer.rewind();
		bodyBuffer.get(body, 0, length);
	}

	protected abstract ByteBuffer doBuild(PumaContext context) throws IOException;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.puma.server.packet.CommandPacket#getBytes()
	 */
	@Override
	public byte[] getBytes() {
		ByteBuffer byteBuf = ByteBuffer.allocate(4 + length);
		PacketUtils.writeInt(byteBuf, length, 3);
		PacketUtils.writeByte(byteBuf, (byte) seq);
		byteBuf.put(body);
		byte[] bytes = new byte[4 + length];
		byteBuf.rewind();
		byteBuf.get(bytes);
		return bytes;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.puma.server.packet.Packet#length()
	 */
	@Override
	public int length() {
		return length;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.puma.server.packet.Packet#seq()
	 */
	@Override
	public int seq() {
		return seq;
	}

	public void write(OutputStream os, PumaContext context) throws IOException {
		if (length > context.getMaxThreeBytes()) {
			int offset = 0;
			int splitSeq = seq;
			for (; offset + context.getMaxThreeBytes() <= length; offset += context.getMaxThreeBytes()) {
				ByteBuffer splitHeaderBuffer = ByteBuffer.allocate(4);
				PacketUtils.writeInt(splitHeaderBuffer, context.getMaxThreeBytes(), 3);
				PacketUtils.writeInt(splitHeaderBuffer, splitSeq++, 1);
				byte[] splitHeader = new byte[4];
				splitHeaderBuffer.get(splitHeader);
				os.write(splitHeader);
				os.write(body, offset, context.getMaxThreeBytes());
			}

			ByteBuffer splitHeaderBuffer = ByteBuffer.allocate(4);
			PacketUtils.writeInt(splitHeaderBuffer, body.length - offset, 3);
			PacketUtils.writeInt(splitHeaderBuffer, splitSeq++, 1);
			byte[] splitHeader = new byte[4];
			splitHeaderBuffer.get(splitHeader);
			os.write(splitHeader);
			os.write(body, offset, body.length - offset);

		} else {
			os.write(getBytes());
			os.flush();
		}

	}

}
