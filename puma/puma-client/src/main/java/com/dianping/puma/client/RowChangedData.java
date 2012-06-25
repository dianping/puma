/**
 * Project: ${puma-client.aid}
 * 
 * File Created at 2012-6-25
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
package com.dianping.puma.client;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * TODO Comment of RowChangedData
 * 
 * @author Leo Liang
 * 
 */
public class RowChangedData implements Serializable {

	private static final long		serialVersionUID	= -3426837914222597530L;

	private long					executeTime;
	private Map<String, ColumnInfo>	columns				= new HashMap<String, ColumnInfo>();

	/**
	 * @return the columns
	 */
	public Map<String, ColumnInfo> getColumns() {
		return columns;
	}

	/**
	 * @param executeTime
	 *            the executeTime to set
	 */
	public void setExecuteTime(long executeTime) {
		this.executeTime = executeTime;
	}

	/**
	 * @return the executeTime
	 */
	public long getExecuteTime() {
		return executeTime;
	}

	public ColumnInfo getColumn(String columnName) {
		return columns.get(columnName);
	}

	/**
	 * @param columns
	 *            the columns to set
	 */
	public void setColumns(Map<String, ColumnInfo> columns) {
		this.columns = columns;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "RowChangedData [executeTime=" + executeTime + ", columns=" + columns + "]";
	}

	public static enum ColumnType {

	}

	public static class ColumnInfo implements Serializable {
		private static final long	serialVersionUID	= 8036820944314281838L;
		private ColumnType			type;
		private Object				oldValue;
		private Object				newValue;

		/**
		 * @param type
		 * @param oldValue
		 * @param newValue
		 */
		public ColumnInfo(ColumnType type, Object oldValue, Object newValue) {
			super();
			this.type = type;
			this.oldValue = oldValue;
			this.newValue = newValue;
		}

		/**
		 * @return the type
		 */
		public ColumnType getType() {
			return type;
		}

		/**
		 * @return the oldValue
		 */
		public Object getOldValue() {
			return oldValue;
		}

		/**
		 * @return the newValue
		 */
		public Object getNewValue() {
			return newValue;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "ColumnInfo [type=" + type + ", oldValue=" + oldValue + ", newValue=" + newValue + "]";
		}

	}
}
