/**
 * Project: ${puma-common.aid}
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
package com.dianping.puma.utils;

import java.util.Arrays;
import java.util.BitSet;

/**
 * TODO Comment of CodecUtils
 * 
 * @see http://code.google.com/p/open-replicator/
 * 
 */
public final class CodecUtils {

	private CodecUtils() {

	}

	public static BitSet fromByteArray(byte[] bytes) {
		BitSet bits = new BitSet();
		for (int i = 0; i < bytes.length * 8; i++) {
			if ((bytes[bytes.length - i / 8 - 1] & (1 << (i % 8))) > 0) {
				bits.set(i);
			}
		}
		return bits;
	}

	public static byte[] toByteArray(BitSet bits) {
		byte[] bytes = new byte[bits.length() / 8 + 1];
		for (int i = 0; i < bits.length(); i++) {
			if (bits.get(i)) {
				bytes[bytes.length - i / 8 - 1] |= 1 << (i % 8);
			}
		}
		return bytes;
	}

	public static int toBigEndian(final int v) {
		int r = v;
		for (int i = 0; i < 4; i++) {
			r = ((r & 0x000000FF) << 24) | (r >>> 8);
		}
		return r;
	}

	public static long toBigEndian(final long v) {
		long r = v;
		for (int i = 0; i < 8; i++) {
			r = ((r & 0x00000000000000FFL) << 56) | (r >>> 8);
		}
		return r;
	}

	public static byte[] toBigEndian(byte[] value) {
		for (int i = 0, length = value.length >> 2; i <= length; i++) {
			final int j = value.length - 1 - i;
			final byte t = value[i];
			value[i] = value[j];
			value[j] = t;
		}
		return value;
	}

	/**
	 * 
	 */
	public static int toUnsigned(byte b) {
		return b & 0xFF;
	}

	public static int toUnsigned(short s) {
		return s & 0xFFFF;
	}

	public static long toUnsigned(int i) {
		return i & 0xFFFFFFFFL;
	}

	/**
	 * 
	 */
	public static byte[] toByteArray(byte num) {
		return new byte[] { num };
	}

	public static byte[] toByteArray(short num) {
		final byte[] r = new byte[2];
		for (int i = 0; i < 2; i++) {
			r[i] = (byte) (num >>> (8 - i * 8));
		}
		return r;
	}

	public static byte[] toByteArray(int num) {
		final byte[] r = new byte[4];
		for (int i = 0; i < 4; i++) {
			r[i] = (byte) (num >>> (24 - i * 8));
		}
		return r;
	}

	public static byte[] toByteArray(long num) {
		final byte[] r = new byte[8];
		for (int i = 0; i < 8; i++) {
			r[i] = (byte) (num >>> (56 - i * 8));
		}
		return r;
	}

	/**
	 * 
	 */
	public static int toInt(byte[] data, int offset, int length) {
		int r = 0;
		for (int i = offset; i < (offset + length); i++) {
			final byte b = data[i];
			r = (r << 8) | (b >= 0 ? (int) b : (b + 256));
		}
		return r;
	}

	public static long toLong(byte[] data, int offset, int length) {
		long r = 0;
		for (int i = offset; i < (offset + length); i++) {
			final byte b = data[i];
			r = (r << 8) | (b >= 0 ? (int) b : (b + 256));
		}
		return r;
	}

	public static byte[] or(byte[] data1, byte[] data2) {
		//
		if (data1.length != data2.length) {
			throw new IllegalArgumentException("array lenth does NOT match, " + data1.length + " vs " + data2.length);
		}

		//
		final byte r[] = new byte[data1.length];
		for (int i = 0; i < r.length; i++) {
			r[i] = (byte) (data1[i] | data2[i]);
		}
		return r;
	}

	public static byte[] and(byte[] data1, byte[] data2) {
		//
		if (data1.length != data2.length) {
			throw new IllegalArgumentException("array lenth does NOT match, " + data1.length + " vs " + data2.length);
		}

		//
		final byte r[] = new byte[data1.length];
		for (int i = 0; i < r.length; i++) {
			r[i] = (byte) (data1[i] & data2[i]);
		}
		return r;
	}

	public static byte[] xor(byte[] data1, byte[] data2) {
		//
		if (data1.length != data2.length) {
			throw new IllegalArgumentException("array lenth does NOT match, " + data1.length + " vs " + data2.length);
		}

		//
		final byte r[] = new byte[data1.length];
		for (int i = 0; i < r.length; i++) {
			r[i] = (byte) (data1[i] ^ data2[i]);
		}
		return r;
	}

	public static boolean equals(byte[] data1, byte[] data2) {
		return Arrays.equals(data1, data2);
	}

	public static byte[] concat(byte[] data1, byte[] data2) {
		final byte r[] = new byte[data1.length + data2.length];
		System.arraycopy(data1, 0, r, 0, data1.length);
		System.arraycopy(data2, 0, r, data1.length, data2.length);
		return r;
	}
}
