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
 * TODO Comment of TimestampColumn
 *
 * @author Leo Liang
 * @see http://code.google.com/p/open-replicator/
 */
public final class TimestampColumn implements Column {
	private static final long serialVersionUID = 3097163231761587681L;

	private final String timestamp;

	private TimestampColumn(long value) {
		if (value == 0) {
			timestamp = "0000-00-00 00:00:00";
		} else {
			String temp = (new Timestamp(value * 1000)).toString();
			timestamp = temp.substring(0, temp.length() - 2);
		}
	}

	@Override
	public String toString() {
		return timestamp;
	}

	public String getValue() {
		return timestamp;
	}

	public static TimestampColumn valueOf(long value) {
		return new TimestampColumn(value);
	}
}
