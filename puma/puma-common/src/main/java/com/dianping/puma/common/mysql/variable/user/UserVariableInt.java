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
package com.dianping.puma.common.mysql.variable.user;

/**
 * 
 * TODO Comment of UserVariableInt
 * 
 * @author Leo Liang
 * 
 */
public class UserVariableInt implements UserVariable {
	private final long	value;

	public UserVariableInt(long value) {
		this.value = value;
	}

	public Long getValue() {
		return this.value;
	}
}
