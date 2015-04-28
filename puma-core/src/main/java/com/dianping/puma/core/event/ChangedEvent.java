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
 * <tr>
 * <td><code>serverId</code>
 * <td><code>事件来源的Mysql的serverId</code>
 * <tr bgcolor="#eeeeff">
 * <td><code>binlog</code>
 * <td><code>事件来源的binlog</code>
 * <tr>
 * <td><code>binlogPos</code>
 * <td><code>事件来源的binlog位置</code>
 * </table>
 * </blockquote>
 * 
 * @author Leo Liang
 * 
 */
public abstract class ChangedEvent extends Event implements Serializable {
	private static final long	serialVersionUID	= -2358086827502066009L;
	private long				executeTime;
	private String				database;
	private String				table;
	private long				serverId;
	private String				binlog;
	private long				binlogPos;
	private long 				binlogNextPos;
	private long                binlogServerId;
	
	/**
     * @return the binlogServerId
     */
    public long getBinlogServerId() {
        return binlogServerId;
    }

    /**
     * @param binlogServerId the binlogServerId to set
     */
    public void setBinlogServerId(long binlogServerId) {
        this.binlogServerId = binlogServerId;
    }

    /**
	 * @return the serverId
	 */
	public long getServerId() {
		return serverId;
	}

	/**
	 * @param serverId
	 *            the serverId to set
	 */
	public void setServerId(long serverId) {
		this.serverId = serverId;
	}

	/**
	 * @return the binlog
	 */
	public String getBinlog() {
		return binlog;
	}

	/**
	 * @param binlog
	 *            the binlog to set
	 */
	public void setBinlog(String binlog) {
		this.binlog = binlog;
	}

	/**
	 * @return the binlogPos
	 */
	public long getBinlogPos() {
		return binlogPos;
	}

	public void setBinlogNextPos(long binlogNextPos) {
		this.binlogNextPos = binlogNextPos;
	}

	public long getBinlogNextPos() {
		return binlogNextPos;
	}

	/**
	 * @param binlogPos
	 *            the binlogPos to set
	 */
	public void setBinlogPos(long binlogPos) {
		this.binlogPos = binlogPos;
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
				+ getSeq() + ", serverId=" + serverId + ", binlog=" + binlog + ", binlogPos=" + binlogPos + "]";
	}

	abstract public String getFullName();

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((binlog == null) ? 0 : binlog.hashCode());
		result = prime * result + (int) (binlogPos ^ (binlogPos >>> 32));
		result = prime * result + ((database == null) ? 0 : database.hashCode());
		result = prime * result + (int) (executeTime ^ (executeTime >>> 32));
		result = prime * result + (int) (serverId ^ (serverId >>> 32));
		result = prime * result + (int) (getSeq() ^ (getSeq() >>> 32));
		result = prime * result + ((table == null) ? 0 : table.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		ChangedEvent other = (ChangedEvent) obj;
		if (binlog == null) {
			if (other.binlog != null) {
				return false;
			}
		} else if (!binlog.equals(other.binlog)) {
			return false;
		}
		if (binlogPos != other.binlogPos) {
			return false;
		}
		if (database == null) {
			if (other.database != null) {
				return false;
			}
		} else if (!database.equals(other.database)) {
			return false;
		}
		if (executeTime != other.executeTime) {
			return false;
		}
		if (serverId != other.serverId) {
			return false;
		}
		if (getSeq() != other.getSeq()) {
			return false;
		}
		if (table == null) {
			if (other.table != null) {
				return false;
			}
		} else if (!table.equals(other.table)) {
			return false;
		}
		return true;
	}

}
