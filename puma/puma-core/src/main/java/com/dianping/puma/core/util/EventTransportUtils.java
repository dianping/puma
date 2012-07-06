/**
 * Project: puma-core
 * 
 * File Created at 2012-7-6
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
package com.dianping.puma.core.util;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.codehaus.jackson.map.ObjectMapper;

import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.event.DdlEvent;
import com.dianping.puma.core.event.RowChangedEvent;

/**
 * TODO Comment of TransportUtils
 * 
 * @author Leo Liang
 * 
 */
public class EventTransportUtils {
	public static final int	DDL_EVENT	= 0;
	public static final int	DML_EVENT	= 1;

	public static void write(ChangedEvent event, OutputStream out) throws IOException {
		ObjectMapper om = new ObjectMapper();
		byte[] data = om.writeValueAsBytes(event);
		out.write(intToByteArray(data.length + 1));

		if (event instanceof DdlEvent) {
			out.write(DDL_EVENT);
		} else {
			out.write(DML_EVENT);
		}

		out.write(data);
	}

	public static ChangedEvent read(InputStream is) throws IOException {
		ObjectMapper om = new ObjectMapper();
		byte[] lengthBytes = new byte[4];
		readFully(is, lengthBytes, 0, 4);
		int length = byteArrayToInt(lengthBytes);
		byte[] data = new byte[length - 1];
		int type = is.read();
		readFully(is, data, 0, length - 1);
		if (type == DDL_EVENT) {
			return om.readValue(data, DdlEvent.class);
		} else {
			return om.readValue(data, RowChangedEvent.class);
		}

	}

	private static int readFully(InputStream in, byte[] b, int off, int len) throws IOException {
		if (len < 0) {
			throw new IndexOutOfBoundsException();
		}

		int n = 0;
		while (n < len) {
			int count = in.read(b, off + n, len - n);
			if (count < 0) {
				throw new EOFException("Can not read response from server. Expected to " + len + " bytes, read " + n
						+ " bytes before connection was unexpectedly lost.");
			}

			n += count;
		}
		return n;
	}

	private static final byte[] intToByteArray(int value) {
		return new byte[] { (byte) (value >>> 24), (byte) (value >>> 16), (byte) (value >>> 8), (byte) value };
	}

	private static final int byteArrayToInt(byte[] data) {
		if (data.length <= 4) {
			int r = 0;
			for (int i = 0; i < data.length; i++) {
				r |= ((data[i] & 0xff) << ((data.length - i - 1) << 3));
			}
			return r;
		}
		return 0;
	}

}
