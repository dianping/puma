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
package com.dianping.puma.common.mysql.column;

/**
 * 
 * TODO Comment of TinyColumn
 * 
 * @author Leo Liang
 * 
 */
public final class TinyColumn implements Column {
	private static final long			serialVersionUID	= -7981385814804060055L;
	public static final int				MIN_VALUE			= Byte.MIN_VALUE;
	public static final int				MAX_VALUE			= Byte.MAX_VALUE;

	private static final TinyColumn[]	CACHE				= new TinyColumn[255];
	static {
		for (int i = 0; i < CACHE.length; i++) {
			CACHE[i] = new TinyColumn(i + Byte.MIN_VALUE);
		}
	}

	private final int					value;

	private TinyColumn(int value) {
		this.value = value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "TinyColumn [value=" + value + "]";
	}

	public Integer getValue() {
		return this.value;
	}

	public static final TinyColumn valueOf(int value) {
		if (value < MIN_VALUE || value > MAX_VALUE)
			throw new IllegalArgumentException("invalid value: " + value);
		return CACHE[value - Byte.MIN_VALUE];
	}
}
