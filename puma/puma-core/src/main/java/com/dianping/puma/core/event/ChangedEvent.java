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
 * <p>
 * 变更时间的基类
 * </p>
 * 
 * <p>
 * 域信息和含义如下：
 * </p>
 * <blockquote>
 * <table border=0 cellspacing=3 cellpadding=0 summary="">
 * <tr bgcolor="#ccccff">
 * <th align=left>域
 * <th align=left>含义
 * <tr>
 * <td><code>executeTime</code>
 * <td><code>运行时间</code>
 * <tr bgcolor="#eeeeff">
 * <td><code>database</code>
 * <td><code>数据库信息</code>
 * <tr>
 * <td><code>table</code>
 * <td><code>数据库表</code>
 * <tr bgcolor="#eeeeff">
 * <td><code>seq</code>
 * <td><code>时间编号</code>
 * </table>
 * </blockquote>
 * 
 * @author Leo Liang
 * 
 */
public abstract class ChangedEvent implements Serializable {
	private static final long	serialVersionUID	= -2358086827502066009L;
	private long				executeTime;
	private String				database;
	private String				table;
	private long				seq;

	/**
	 * @return the seq
	 */
	public long getSeq() {
		return seq;
	}

	/**
	 * @param seq
	 *            the seq to set
	 */
	public void setSeq(long seq) {
		this.seq = seq;
	}

	/**
	 * @return the executeTime
	 */
	public long getExecuteTime() {
		return executeTime;
	}

	/**
	 * @param executeTime
	 *            the executeTime to set
	 */
	public void setExecuteTime(long executeTime) {
		this.executeTime = executeTime;
	}

	/**
	 * @return the database
	 */
	public String getDatabase() {
		return database;
	}

	/**
	 * @param database
	 *            the database to set
	 */
	public void setDatabase(String database) {
		this.database = database;
	}

	/**
	 * @return the table
	 */
	public String getTable() {
		return table;
	}

	/**
	 * @param table
	 *            the table to set
	 */
	public void setTable(String table) {
		this.table = table;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ChangedEvent [executeTime=" + executeTime + ", database=" + database + ", table=" + table + ", seq="
				+ seq + "]";
	}

}
