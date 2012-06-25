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
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

/**
 * <pre>
 * 对应一行的数据变化
 * </pre>
 * 
 * @author Leo Liang
 * 
 */
public class RowChangedData implements Serializable {

	private static final long		serialVersionUID	= -3426837914222597530L;

	private long					executeTime;
	private Map<String, ColumnInfo>	columns				= new HashMap<String, ColumnInfo>();
	private ActionType				actionType;

	/**
	 * @return the actionType
	 */
	public ActionType getActionType() {
		return actionType;
	}

	/**
	 * @param actionType
	 *            the actionType to set
	 */
	public void setActionType(ActionType actionType) {
		this.actionType = actionType;
	}

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
		return "RowChangedData [executeTime=" + executeTime + ", columns=" + columns + ", actionType=" + actionType
				+ "]";
	}

	public static enum ActionType {
		INSERT, DELETE, UPDATE;
	}

	public static class ColumnInfo implements Serializable {
		private static final long	serialVersionUID	= 8036820944314281838L;
		private Types				type;
		private Object				oldValue;
		private Object				newValue;

		/**
		 * @param type
		 * @param oldValue
		 * @param newValue
		 */
		public ColumnInfo(Types type, Object oldValue, Object newValue) {
			super();
			this.type = type;
			this.oldValue = oldValue;
			this.newValue = newValue;
		}

		/**
		 * 
		 */
		public ColumnInfo() {
			super();
		}

		/**
		 * @param type
		 *            the type to set
		 */
		public void setType(Types type) {
			this.type = type;
		}

		/**
		 * @param oldValue
		 *            the oldValue to set
		 */
		public void setOldValue(Object oldValue) {
			this.oldValue = oldValue;
		}

		/**
		 * @param newValue
		 *            the newValue to set
		 */
		public void setNewValue(Object newValue) {
			this.newValue = newValue;
		}

		/**
		 * @return the type
		 */
		public Types getType() {
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
