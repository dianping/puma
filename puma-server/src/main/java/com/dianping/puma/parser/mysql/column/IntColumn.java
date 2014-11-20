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
 * TODO Comment of IntColumn
 * 
 * @see http://code.google.com/p/open-replicator/
 * @author Leo Liang
 * 
 */
public final class IntColumn implements Column {
	private static final long			serialVersionUID	= -8483539867897207855L;
	private static final IntColumn[]	CACHE				= new IntColumn[255];
	static {
		for (int i = 0; i < CACHE.length; i++) {
			CACHE[i] = new IntColumn(i + Byte.MIN_VALUE);
		}
	}

	private final int					value;

	private IntColumn(int value) {
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

	public static final IntColumn valueOf(int value) {
		final int index = value - Byte.MIN_VALUE;
		return (index >= 0 && index < CACHE.length) ? CACHE[index] : new IntColumn(value);
	}
}
