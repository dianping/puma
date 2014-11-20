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
 * TODO Comment of LongLongColumn
 * 
 * @see http://code.google.com/p/open-replicator/
 * @author Leo Liang
 * 
 */
public final class LongLongColumn implements Column {
	private static final long	serialVersionUID	= 9185299741456590048L;
	public static final long	MIN_VALUE			= Long.MIN_VALUE;
	public static final long	MAX_VALUE			= Long.MAX_VALUE;

	private final long			value;

	private LongLongColumn(long value) {
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

	public Long getValue() {
		return this.value;
	}

	public static final LongLongColumn valueOf(long value) {
		return new LongLongColumn(value);
	}
}
