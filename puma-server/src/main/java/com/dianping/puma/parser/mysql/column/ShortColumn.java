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
 * TODO Comment of ShortColumn
 * 
 * @see http://code.google.com/p/open-replicator/
 * @author Leo Liang
 * 
 */
public final class ShortColumn implements Column {
	private static final long			serialVersionUID	= 2068406715501454736L;
	public static final int				MIN_VALUE			= Short.MIN_VALUE;
	public static final int				MAX_VALUE			= Short.MAX_VALUE;

	private static final ShortColumn[]	CACHE				= new ShortColumn[255];
	static {
		for (int i = 0; i < CACHE.length; i++) {
			CACHE[i] = new ShortColumn(i + Byte.MIN_VALUE);
		}
	}

	private final int					value;

	private ShortColumn(int value) {
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

	public static final ShortColumn valueOf(int value) {
		if (value < MIN_VALUE || value > MAX_VALUE) {
			throw new IllegalArgumentException("invalid value: " + value);
		}
		final int index = value - Byte.MIN_VALUE;
		return (index >= 0 && index < CACHE.length) ? CACHE[index] : new ShortColumn(value);
	}
}
