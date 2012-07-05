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
package com.dianping.puma.core.event;

import java.io.Serializable;
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
public class RowChangedEvent extends ChangedEvent implements Serializable {

	private static final long		serialVersionUID	= -3426837914222597530L;

	public static final int			INSERT				= 0;
	public static final int			DELETE				= 1;
	public static final int			UPDATE				= 2;

	private Map<String, ColumnInfo>	columns				= new HashMap<String, ColumnInfo>();
	private int						actionType;
	private boolean					isTransactionBegin	= false;
	private boolean					isTransactionCommit	= false;

	/**
	 * @return the isTransactionBegin
	 */
	public boolean isTransactionBegin() {
		return isTransactionBegin;
	}

	/**
	 * @param isTransactionBegin
	 *            the isTransactionBegin to set
	 */
	public void setTransactionBegin(boolean isTransactionBegin) {
		this.isTransactionBegin = isTransactionBegin;
	}

	/**
	 * @return the isTransactionCommit
	 */
	public boolean isTransactionCommit() {
		return isTransactionCommit;
	}

	/**
	 * @param isTransactionCommit
	 *            the isTransactionCommit to set
	 */
	public void setTransactionCommit(boolean isTransactionCommit) {
		this.isTransactionCommit = isTransactionCommit;
	}

	/**
	 * @return the columns
	 */
	public Map<String, ColumnInfo> getColumns() {
		return columns;
	}

	/**
	 * @param columns
	 *            the columns to set
	 */
	public void setColumns(Map<String, ColumnInfo> columns) {
		this.columns = columns;
	}

	/**
	 * @return the actionType
	 */
	public int getActionType() {
		return actionType;
	}

	/**
	 * @param actionType
	 *            the actionType to set
	 */
	public void setActionType(int actionType) {
		this.actionType = actionType;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "RowChangedEvent [columns=" + columns + ", actionType=" + actionType + ", isTransactionBegin="
				+ isTransactionBegin + ", isTransactionCommit=" + isTransactionCommit + ", super.toString()="
				+ super.toString() + "]";
	}

	public static class ColumnInfo implements Serializable {
		private static final long	serialVersionUID	= 8036820944314281838L;
		private boolean				isKey;
		private Object				oldValue;
		private Object				newValue;

		/**
		 * 
		 */
		public ColumnInfo() {
			super();
		}

		/**
		 * @param isKey
		 * @param oldValue
		 * @param newValue
		 */
		public ColumnInfo(boolean isKey, Object oldValue, Object newValue) {
			super();
			this.isKey = isKey;
			this.oldValue = oldValue;
			this.newValue = newValue;
		}

		/**
		 * @return the isKey
		 */
		public boolean isKey() {
			return isKey;
		}

		/**
		 * @param isKey
		 *            the isKey to set
		 */
		public void setKey(boolean isKey) {
			this.isKey = isKey;
		}

		/**
		 * @return the oldValue
		 */
		public Object getOldValue() {
			return oldValue;
		}

		/**
		 * @param oldValue
		 *            the oldValue to set
		 */
		public void setOldValue(Object oldValue) {
			this.oldValue = oldValue;
		}

		/**
		 * @return the newValue
		 */
		public Object getNewValue() {
			return newValue;
		}

		/**
		 * @param newValue
		 *            the newValue to set
		 */
		public void setNewValue(Object newValue) {
			this.newValue = newValue;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "ColumnInfo [isKey=" + isKey + ", oldValue=" + oldValue + ", newValue=" + newValue + "]";
		}

	}
}
