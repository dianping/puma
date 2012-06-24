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

import java.sql.Date;

/**
 * 
 * TODO Comment of DateColumn
 * 
 * @author Leo Liang
 * 
 */
public final class DateColumn implements Column {
	private static final long	serialVersionUID	= -4339512188008495629L;
	private final Date			value;

	private DateColumn(Date value) {
		this.value = value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "DateColumn [value=" + value + "]";
	}

	public Date getValue() {
		return this.value;
	}

	public static final DateColumn valueOf(Date value) {
		return new DateColumn(value);
	}
}
