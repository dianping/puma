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

/**
 * 
 * @author Leo Liang
 * 
 */
public final class ByteArrayUtils {
	private ByteArrayUtils() {

	}

	public static int byteArrayToInt(byte[] data, int start, int length) {
		if (length <= 4) {
			int r = 0;
			for (int i = start; i < length; i++) {
				r |= ((data[i] & 0xff) << ((length - (i - start) - 1) << 3));
			}
			return r;
		}
		return 0;
	}

	public static final byte[] intToByteArray(int value) {
		return new byte[] { (byte) (value >>> 24), (byte) (value >>> 16), (byte) (value >>> 8), (byte) value };
	}
}
