/**
 * Project: ${puma-client.aid}
 * <p/>
 * File Created at 2012-7-3
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
import com.dianping.puma.core.util.constant.DdlEventSubType;
import com.dianping.puma.core.util.constant.DdlEventType;
import com.dianping.puma.core.util.sql.DDLType;

import java.io.Serializable;

/**
 * <p>
 * 变更时间的基类
 * </p>
 * <p/>
 * <p>
 * 域信息和含义如下：
 * </p>
 * <blockquote>
 * <table border=0 cellspacing=3 cellpadding=0 summary="">
 * <tr bgcolor="#ccccff">
 * <th align=left>域
 * <th align=left>含义
 * <tr>
 * <td><code>sql</code>
 * <td><code>sql语句</code>
 * </table>
 * </blockquote>
 *
 * @author Leo Liang
 */

public class DdlEvent extends ChangedEvent implements Serializable {
    private static final long serialVersionUID = -5676914333310337620L;

    private final EventType eventType = EventType.DDL;

    private String sql;

    private DDLType ddlType;

    private DdlEventType ddlEventType;

    private DdlEventSubType ddlEventSubType;

    public DdlEvent() {

    }

    public DdlEvent(long executeTime, long serverId, String binlogFile, long binlogPosition) {
        this.executeTime = executeTime;
        this.serverId = serverId;
        this.binlogInfo = new BinlogInfo(serverId, binlogFile, binlogPosition, 0, executeTime);
    }

    /**
     * @return the sql
     */
    public String getSql() {
        return sql;
    }

    /**
     * @param sql the sql to set
     */
    public void setSql(String sql) {
        this.sql = sql;
    }

    @Override
    public String genFullName() {
        return getDatabase() + "." + getTable();
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

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((sql == null) ? 0 : sql.hashCode());
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
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        DdlEvent other = (DdlEvent) obj;
        if (sql == null) {
            if (other.sql != null) {
                return false;
            }
        } else if (!sql.equals(other.sql)) {
            return false;
        }
        return true;
    }

    public void setDdlEventType(DdlEventType ddlEventType) {
        this.ddlEventType = ddlEventType;
    }

    public void setDdlEventSubType(DdlEventSubType ddlEventSubType) {
        this.ddlEventSubType = ddlEventSubType;
    }

    public void setDDLType(DDLType ddlType) {
        this.ddlType = ddlType;
    }

    public DDLType getDdlType() {
        return ddlType;
    }

    public DdlEventType getDdlEventType() {
        return ddlEventType;
    }

    public DdlEventSubType getDdlEventSubType() {
        return ddlEventSubType;
    }

    @Override
    public EventType getEventType() {
        return eventType;
    }
}
