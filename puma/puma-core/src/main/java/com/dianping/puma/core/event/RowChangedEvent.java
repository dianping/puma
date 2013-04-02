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
 * <p>
 * 变更时间的基类
 * </p>
 * <p>
 * 域信息和含义如下：
 * </p>
 * <blockquote>
 * <table border=0 cellspacing=3 cellpadding=0 summary="">
 * <tr bgcolor="#ccccff">
 * <th align=left>域
 * <th align=left>含义
 * <tr>
 * <td><code>columns</code>
 * <td><code>列信息Map(key为列名，value为列信息)</code>
 * <tr bgcolor="#eeeeff">
 * <td><code>actionType</code>
 * <td><code>数据库操作类型</code>
 * <tr>
 * <td><code>isTransactionBegin</code>
 * <td><code>是否是transaction开始事件</code>
 * <tr bgcolor="#eeeeff">
 * <td><code>isTransactionCommit</code>
 * <td><code>是否是transaction提交事件</code>
 * </table>
 * <p>
 * <tt>ColumnInfo</tt>域信息和含义如下：
 * </p>
 * <table border=0 cellspacing=3 cellpadding=0 summary="">
 * <tr bgcolor="#ccccff">
 * <th align=left>域
 * <th align=left>含义
 * <tr>
 * <td><code>isKey</code>
 * <td><code>是否是主键</code>
 * <tr bgcolor="#eeeeff">
 * <td><code>oldValue</code>
 * <td><code>变更前的列值</code>
 * <tr>
 * <td><code>newValue</code>
 * <td><code>变更后的列值</code>
 * </table>
 * <p>
 * <tt>ActionType</tt>取值含义如下：
 * </p>
 * <table border=0 cellspacing=3 cellpadding=0 summary="">
 * <tr bgcolor="#ccccff">
 * <th align=left>值
 * <th align=left>含义
 * <tr>
 * <td><code>INSERT</code>
 * <td><code>0</code>
 * <tr bgcolor="#eeeeff">
 * <td><code>DELETE</code>
 * <td><code>1</code>
 * <tr>
 * <td><code>UPDATE</code>
 * <td><code>2</code>
 * </table>
 * </blockquote>
 * 
 * @author Leo Liang
 */
public class RowChangedEvent extends ChangedEvent implements Serializable, Cloneable {

    private static final long serialVersionUID = -3426837914222597530L;

    public static final int INSERT = 0;
    public static final int DELETE = 1;
    public static final int UPDATE = 2;

    private Map<String, ColumnInfo> columns = new HashMap<String, ColumnInfo>();
    private int actionType;
    private boolean isTransactionBegin = false;
    private boolean isTransactionCommit = false;

    /**
     * @return the isTransactionBegin
     */
    public boolean isTransactionBegin() {
        return isTransactionBegin;
    }

    /**
     * @param isTransactionBegin the isTransactionBegin to set
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
     * @param isTransactionCommit the isTransactionCommit to set
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
     * @param columns the columns to set
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
     * @param actionType the actionType to set
     */
    public void setActionType(int actionType) {
        this.actionType = actionType;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "RowChangedEvent [columns=" + columns + ", actionType=" + actionType + ", isTransactionBegin=" + isTransactionBegin
                + ", isTransactionCommit=" + isTransactionCommit + ", super.toString()=" + super.toString() + "]";
    }

    @Override
    public RowChangedEvent clone() {
        RowChangedEvent e;
        try {
            e = (RowChangedEvent) super.clone();
        } catch (CloneNotSupportedException e1) {
            throw new RuntimeException(e1);
        }
        e.columns = new HashMap<String, ColumnInfo>();
        for (Map.Entry<String, ColumnInfo> entry : this.columns.entrySet()) {
            ColumnInfo columnInfo0 = entry.getValue();
            ColumnInfo columnInfo = new ColumnInfo(columnInfo0.isKey, columnInfo0.oldValue, columnInfo0.newValue);
            e.columns.put(entry.getKey(), columnInfo);
        }
        return e;
    }

    public static class ColumnInfo implements Serializable {
        private static final long serialVersionUID = 8036820944314281838L;
        private boolean isKey;
        private Object oldValue;
        private Object newValue;

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
         * @param isKey the isKey to set
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
         * @param oldValue the oldValue to set
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
         * @param newValue the newValue to set
         */
        public void setNewValue(Object newValue) {
            this.newValue = newValue;
        }

        /*
         * (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return "ColumnInfo [isKey=" + isKey + ", oldValue=" + oldValue + ", newValue=" + newValue + "]";
        }

        /*
         * (non-Javadoc)
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + (isKey ? 1231 : 1237);
            result = prime * result + ((newValue == null) ? 0 : newValue.hashCode());
            result = prime * result + ((oldValue == null) ? 0 : oldValue.hashCode());
            return result;
        }

        /*
         * (non-Javadoc)
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
            ColumnInfo other = (ColumnInfo) obj;
            if (isKey != other.isKey) {
                return false;
            }
            if (newValue == null) {
                if (other.newValue != null) {
                    return false;
                }
            } else if (!newValue.equals(other.newValue)) {
                return false;
            }
            if (oldValue == null) {
                if (other.oldValue != null) {
                    return false;
                }
            } else if (!oldValue.equals(other.oldValue)) {
                return false;
            }
            return true;
        }

    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + actionType;
        result = prime * result + ((columns == null) ? 0 : columns.hashCode());
        result = prime * result + (isTransactionBegin ? 1231 : 1237);
        result = prime * result + (isTransactionCommit ? 1231 : 1237);
        return result;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        RowChangedEvent other = (RowChangedEvent) obj;
        if (actionType != other.actionType) {
            return false;
        }
        if (columns == null) {
            if (other.columns != null) {
                return false;
            }
        } else if (!columns.equals(other.columns)) {
            return false;
        }
        if (isTransactionBegin != other.isTransactionBegin) {
            return false;
        }
        if (isTransactionCommit != other.isTransactionCommit) {
            return false;
        }
        return true;
    }

}
