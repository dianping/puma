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

import java.math.BigDecimal;

/**
 * @see http://code.google.com/p/open-replicator/
 * TODO Comment of DecimalColumn
 * @author Leo Liang
 *
 */
public final class DecimalColumn implements Column {
	private static final long	serialVersionUID	= 5487785770715315227L;
	private final BigDecimal	value;
	private final int			precision;
	private final int			scale;

	private DecimalColumn(BigDecimal value, int precision, int scale) {
		this.value = value;
		this.scale = scale;
		this.precision = precision;
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

	public BigDecimal getValue() {
		return this.value;
	}

	public int getPrecision() {
		return precision;
	}

	public int getScale() {
		return scale;
	}

	public static final DecimalColumn valueOf(BigDecimal value, int precision, int scale) {
		if (precision < scale) {
			throw new IllegalArgumentException("invalid precision: " + precision + ", scale: " + scale);
		}
		return new DecimalColumn(value, precision, scale);
	}
}
