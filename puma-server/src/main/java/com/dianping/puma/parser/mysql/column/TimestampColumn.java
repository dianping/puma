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

import java.sql.Timestamp;

/**
 * 
 * TODO Comment of TimestampColumn
 * 
 * @see http://code.google.com/p/open-replicator/
 * @author Leo Liang
 * 
 */
public final class TimestampColumn implements Column {
	private static final long serialVersionUID = 3097163231761587681L;
	private final Timestamp value;

	private TimestampColumn(Timestamp value) {
		if (value.getTime() <= 1000) {
			value = new java.sql.Timestamp(1000);
		}
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

	public Timestamp getValue() {
		if (this.value.getTime() <= 1000) {
			return new java.sql.Timestamp(1000);
		}
		return this.value;
	}

	public static final TimestampColumn valueOf(Timestamp value) {
		if (value.getTime() <= 1) {
			value = new java.sql.Timestamp(1000);
		}
		return new TimestampColumn(value);
	}
}
