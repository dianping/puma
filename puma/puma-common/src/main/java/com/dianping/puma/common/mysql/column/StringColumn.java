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
 * TODO Comment of StringColumn
 * 
 * @author Leo Liang
 * 
 */
public final class StringColumn implements Column {
	private static final long	serialVersionUID	= 2596823444368172645L;
	private final byte[]		value;

	private StringColumn(byte[] value) {
		this.value = value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return new String(value);
	}

	public byte[] getValue() {
		return this.value;
	}

	public static final StringColumn valueOf(byte[] value) {
		return new StringColumn(value);
	}
}
