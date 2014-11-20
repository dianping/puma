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

/**
 * 
 * TODO Comment of Int24Column
 * 
 * @see http://code.google.com/p/open-replicator/
 * @author Leo Liang
 * 
 */
public final class Int24Column implements Column {
	private static final long			serialVersionUID	= 8060965726201826237L;
	public static final int				MIN_VALUE			= -8388608;
	public static final int				MAX_VALUE			= 8388607;

	private static final Int24Column[]	CACHE				= new Int24Column[255];
	static {
		for (int i = 0; i < CACHE.length; i++) {
			CACHE[i] = new Int24Column(i + Byte.MIN_VALUE);
		}
	}

	private final int					value;

	private Int24Column(int value) {
		this.value = value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.valueOf(value);
	}

	public Integer getValue() {
		return this.value;
	}

	public static final Int24Column valueOf(int value) {
		if (value < MIN_VALUE || value > MAX_VALUE) {
			throw new IllegalArgumentException("invalid value: " + value);
		}
		final int index = value - Byte.MIN_VALUE;
		return (index >= 0 && index < CACHE.length) ? CACHE[index] : new Int24Column(value);
	}
}
