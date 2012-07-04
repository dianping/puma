/**
 * Project: ${puma-client.aid}
 * 
 * File Created at 2012-7-3
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
package com.dianping.puma.core.event;

import java.io.Serializable;

/**
 * TODO Comment of DdlEvent
 * 
 * @author Leo Liang
 * 
 */
public class DdlEvent extends ChangedEvent implements Serializable {
	private static final long	serialVersionUID	= -5676914333310337620L;
	private String				sql;

	/**
	 * @return the sql
	 */
	public String getSql() {
		return sql;
	}

	/**
	 * @param sql
	 *            the sql to set
	 */
	public void setSql(String sql) {
		this.sql = sql;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "DdlEvent [sql=" + sql + ", super.toString()=" + super.toString() + "]";
	}

}
