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
package com.dianping.puma.common.mysql;

import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.Arrays;

import com.dianping.puma.common.util.CodecUtils;
import com.dianping.puma.common.util.PacketUtils;

/**
 * TODO Comment of Metadata
 * 
 * @author Leo Liang
 * 
 */
public final class Metadata implements Serializable {

	private static final long	serialVersionUID	= -4925248968122255302L;
	private final byte[]		type;
	private final int[]			metadata;

	public Metadata(byte[] type, int[] metadata) {
		this.type = type;
		this.metadata = metadata;
	}

	public byte getType(int column) {
		return this.type[column];
	}

	public int getMetadata(int column) {
		return this.metadata[column];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Metadata [type=" + Arrays.toString(type) + ", metadata=" + Arrays.toString(metadata) + "]";
	}

	public static final Metadata valueOf(byte[] type, byte[] data) throws IOException {
		int[] metadata = new int[type.length];
		ByteBuffer buf = ByteBuffer.wrap(data);
		for (int i = 0; i < type.length; i++) {
			int t = type[i] & 0xFF;
			switch (t) {
				case BinlogConstanst.MYSQL_TYPE_FLOAT:
				case BinlogConstanst.MYSQL_TYPE_DOUBLE:
				case BinlogConstanst.MYSQL_TYPE_TINY_BLOB:
				case BinlogConstanst.MYSQL_TYPE_BLOB:
				case BinlogConstanst.MYSQL_TYPE_MEDIUM_BLOB:
				case BinlogConstanst.MYSQL_TYPE_LONG_BLOB:
					metadata[i] = PacketUtils.readInt(buf, 1);
					break;
				case BinlogConstanst.MYSQL_TYPE_BIT:
				case BinlogConstanst.MYSQL_TYPE_VARCHAR:
				case BinlogConstanst.MYSQL_TYPE_NEWDECIMAL:
					metadata[i] = PacketUtils.readInt(buf, 2);
					break;
				case BinlogConstanst.MYSQL_TYPE_SET:
				case BinlogConstanst.MYSQL_TYPE_ENUM:
				case BinlogConstanst.MYSQL_TYPE_STRING:
					metadata[i] = CodecUtils.toInt(PacketUtils.readBytes(buf, 2), 0, 2); // Big-endian
					break;
				default:
					metadata[i] = 0;
			}
		}
		return new Metadata(type, metadata);
	}
}
