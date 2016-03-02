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
package com.dianping.puma.parser.mysql.column;

import com.dianping.puma.utils.CodecUtils;

import java.util.BitSet;

/**
 * 
 * TODO Comment of BitColumn
 * 
 * @see http://code.google.com/p/open-replicator/
 * @author Leo Liang
 * 
 */
public final class BitColumn implements Column {
	private static final long	serialVersionUID	= 4872621087247780593L;

	private static final int	BIT_MASKS[]			= { 1 << 0, 1 << 1, 1 << 2, 1 << 3, 1 << 4, 1 << 5, 1 << 6, 1 << 7 };

	private final int			length;
	private final byte[]		value;

	private BitColumn(int length, byte[] value) {
		this.length = length;
		this.value = value;
	}

	@Override
	public String toString() {
		final StringBuilder r = new StringBuilder(this.length);
		for (int i = 0; i < this.length; i++) {
			r.append(get(i) ? "1" : "0");
		}
		return r.toString();
	}

	public int getLength() {
		return this.length;
	}

	public byte[] getValue() {
		return this.value;
	}

	public boolean get(int index) {
		final int byteIndex = (index >> 3);
		final int bitIndex = (index - (byteIndex << 3));
		return (this.value[byteIndex] & BIT_MASKS[bitIndex]) != 0;
	}

	public void set(int index) {
		final int byteIndex = (index >> 3);
		final int bitIndex = (index - (byteIndex << 3));
		this.value[byteIndex] |= BIT_MASKS[bitIndex];
	}

	public static final BitColumn valueOf(int length, byte[] value) {
		if (length < 0 || length > (value.length << 3)) {
			throw new IllegalArgumentException("invalid length: " + length);
		}
		return new BitColumn(length, value);
	}

	public static final BitColumn valueOf(int length, BitSet value) {
		return valueOf(length, CodecUtils.toByteArray(value));
	}
}
