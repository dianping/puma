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

import com.dianping.puma.utils.PacketUtils;

import java.io.UnsupportedEncodingException;

/**
 * 
 * TODO Comment of StringColumn
 * 
 * @see http://code.google.com/p/open-replicator/
 * @author Leo Liang
 * 
 */
public final class StringColumn implements Column {
	private static final long serialVersionUID = 2596823444368172645L;
	private final byte[] value;

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
		try {
			return new String(value, PacketUtils.UTF_8);
		} catch (UnsupportedEncodingException e) {
			throw new IllegalArgumentException("Unsupported encoding: " + PacketUtils.UTF_8, e);
		}
	}

	public String getValue() {
		try {
			return new String(value, PacketUtils.UTF_8);
		} catch (UnsupportedEncodingException e) {
			throw new IllegalArgumentException("Unsupported encoding: " + PacketUtils.UTF_8, e);
		}
	}

	public static final StringColumn valueOf(byte[] value) {
		return new StringColumn(value);
	}
}
