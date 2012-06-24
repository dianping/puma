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
 * TODO Comment of YearColumn
 * 
 * @author Leo Liang
 * 
 */
public final class YearColumn implements Column {
	private static final long			serialVersionUID	= 5662761825168326469L;
	private static final YearColumn[]	CACHE				= new YearColumn[255];
	static {
		for (int i = 0; i < CACHE.length; i++) {
			CACHE[i] = new YearColumn(i + 1900);
		}
	}

	private final int					value;

	private YearColumn(int value) {
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

	public static final YearColumn valueOf(int value) {
		final int index = value - 1900;
		return (index >= 0 && index < CACHE.length) ? CACHE[index] : new YearColumn(value);
	}
}
