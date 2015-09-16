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

import java.util.Arrays;

/**
 * 
 * TODO Comment of BlobColumn
 * 
 * @see http://code.google.com/p/open-replicator/
 * @author Leo Liang
 * 
 */
public final class BlobColumn implements Column {
	private static final long	serialVersionUID	= 7148702557181753593L;
	private final byte[]		value;

	private BlobColumn(byte[] value) {
		this.value = value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "BlobColumn [value=" + Arrays.toString(value) + "]";
	}

	public byte[] getValue() {
		return value;
	}

	public static final BlobColumn valueOf(byte[] value) {
		return new BlobColumn(value);
	}
}
