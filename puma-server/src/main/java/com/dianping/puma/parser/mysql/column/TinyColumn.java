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
 * TODO Comment of TinyColumn
 * 
 * @see http://code.google.com/p/open-replicator/
 * @author Leo Liang
 * 
 */
public final class TinyColumn implements Column {
	private static final long	serialVersionUID	= -7981385814804060055L;
	public static final int		MIN_VALUE			= -128;
	public static final int		MAX_VALUE			= 127;

	private final int			value;

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
		return String.valueOf(value);
	}

	public Integer getValue() {
		return this.value;
	}

	public static final TinyColumn valueOf(int value) {
		if (value < MIN_VALUE || value > MAX_VALUE) {
			throw new IllegalArgumentException("invalid value: " + value);
		}
		return new TinyColumn(value);
	}
}
