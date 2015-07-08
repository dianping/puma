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
 * TODO Comment of DatetimeColumn
 * 
 * @see http://code.google.com/p/open-replicator/
 * @author Leo Liang
 * 
 */
public final class DatetimeColumn implements Column {
	private static final long	serialVersionUID	= 8987206727200074893L;
	private final String			value;

	private DatetimeColumn(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return this.value;
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

	public static final DatetimeColumn valueOf(String value) {
		return new DatetimeColumn(value);
	}
}
