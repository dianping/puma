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
 * TODO Comment of NullColumn
 * 
 * @see http://code.google.com/p/open-replicator/
 * @author Leo Liang
 * 
 */
public final class NullColumn implements Column {
	/**
	 * 
	 */
	private static final long			serialVersionUID	= 7147821797515814476L;
	private static final NullColumn[]	CACHE				= new NullColumn[255];
	static {
		for (int i = 0; i < CACHE.length; i++) {
			CACHE[i] = new NullColumn(i);
		}
	}

	private final int					type;

	private NullColumn(int type) {
		this.type = type;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "NULL";
	}

	public int getType() {
		return type;
	}

	public Object getValue() {
		return null;
	}

	public static final NullColumn valueOf(int type) {
		if (type < 0 || type >= CACHE.length) {
			throw new IllegalArgumentException("invalid type: " + type);
		}
		return CACHE[type];
	}
}
