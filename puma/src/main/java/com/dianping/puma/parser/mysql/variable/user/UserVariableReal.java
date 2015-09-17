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
 * TODO Comment of UserVariableReal
 * 
 * @see http://code.google.com/p/open-replicator/
 * @author Leo Liang
 * 
 */
public class UserVariableReal implements UserVariable {
	private static final long	serialVersionUID	= 1074469803672216223L;
	private final double		value;

	public UserVariableReal(double value) {
		this.value = value;
	}

	public Double getValue() {
		return this.value;
	}
}
