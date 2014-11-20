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
package com.dianping.puma.parser.mysql.variable.user;

/**
 * 
 * TODO Comment of UserVariableString
 * 
 * @see http://code.google.com/p/open-replicator/
 * @author Leo Liang
 * 
 */
public class UserVariableString implements UserVariable {
	private static final long	serialVersionUID	= 1249617199914361863L;
	private final byte[]		value;
	private final int			collation;

	public UserVariableString(byte[] value, int collation) {
		this.value = value;
		this.collation = collation;
	}

	public byte[] getValue() {
		return this.value;
	}

	public int getCollation() {
		return collation;
	}
}
