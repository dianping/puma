/**
 * Project: ${puma-client.aid}
 * <p/>
 * File Created at 2012-6-25
 * $Id$
 * <p/>
 * Copyright 2010 dianping.com.
 * All rights reserved.
 * <p/>
 * This software is the confidential and proprietary information of
 * Dianping Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with dianping.com.
 */
package com.dianping.puma.core.event;

import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.core.util.sql.DMLType;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Leo Liang
 */
public class RowChangedEvent extends ChangedEvent implements Serializable, Cloneable {

    private final EventType eventType = EventType.DML;

    private static final long serialVersionUID = -3426837914222597530L;

    private DMLType dmlType;

    private boolean isTransactionBegin = false;

    private boolean isTransactionCommit = false;

    private Map<String, ColumnInfo> columns = new HashMap<String, ColumnInfo>();

    public RowChangedEvent() {
    }

    public RowChangedEvent(long executionTime, long serverId, String binlogFile, long binlogPosition) {
        this.executeTime = executionTime;
        this.serverId = serverId;
        this.binlogInfo = new BinlogInfo(serverId, binlogFile, binlogPosition, 0, executionTime);
    }

    public DMLType getDmlType() {
        return dmlType;
    }

    @Deprecated
    public int getActionType() {
        return dmlType == null ? DMLType.NULL.getDMLType() : dmlType.getDMLType();
    }

    public RowChangedEvent setDmlType(DMLType dmlType) {
        this.dmlType = dmlType;
        return this;
    }

    /**
     * @return the isTransactionBegin
     */
    public boolean isTransactionBegin() {
        return isTransactionBegin;
    }

    /**
     * @param isTransactionBegin the isTransactionBegin to set
     */
    public RowChangedEvent setTransactionBegin(boolean isTransactionBegin) {
        this.isTransactionBegin = isTransactionBegin;
        return this;
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
    public RowChangedEvent setTransactionCommit(boolean isTransactionCommit) {
        this.isTransactionCommit = isTransactionCommit;
        return this;
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
    public RowChangedEvent setColumns(Map<String, ColumnInfo> columns) {
        this.columns = columns;
        return this;
    }

    @Override
    public String genFullName() {
        if (isTransactionBegin || isTransactionCommit) {
            return getDatabase();
        } else {
            return getDatabase() + "." + getTable();
        }
    }

    /*
      * (non-Javadoc)
      * @see java.lang.Object#toString()
      */
    @Override
    public String toString() {
        return "RowChangedEvent [columns=" + columns + ", dmlType=" + dmlType.getDMLType() + ", isTransactionBegin="
                + isTransactionBegin
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

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof RowChangedEvent))
            return false;
        if (!super.equals(o))
            return false;

        RowChangedEvent that = (RowChangedEvent) o;

        if (isTransactionBegin != that.isTransactionBegin)
            return false;
        if (isTransactionCommit != that.isTransactionCommit)
            return false;
        if (!columns.equals(that.columns))
            return false;
        if (dmlType != that.dmlType)
            return false;
        return eventType == that.eventType;

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + eventType.hashCode();
        result = 31 * result + dmlType.hashCode();
        result = 31 * result + (isTransactionBegin ? 1 : 0);
        result = 31 * result + (isTransactionCommit ? 1 : 0);
        result = 31 * result + columns.hashCode();
        return result;
    }

    @Override
    public EventType getEventType() {
        return eventType;
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

}
