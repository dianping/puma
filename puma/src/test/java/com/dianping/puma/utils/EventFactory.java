package com.dianping.puma.utils;

import com.dianping.puma.core.event.DdlEvent;
import com.dianping.puma.core.event.RowChangedEvent;
import com.dianping.puma.core.util.sql.DMLType;

public final class EventFactory {

    public static RowChangedEvent dml(long timestamp, long serverId, String binlogFile, long binlogPosition, String database, String table, boolean begin, boolean commit, DMLType dmlType) {
        RowChangedEvent rowChangedEvent = new RowChangedEvent(timestamp, serverId, binlogFile, binlogPosition);
        rowChangedEvent.setDatabase(database);
        rowChangedEvent.setTable(table);
        rowChangedEvent.setTransactionBegin(begin);
        rowChangedEvent.setTransactionCommit(commit);
        rowChangedEvent.setDmlType(dmlType);
        return rowChangedEvent;
    }

    public static DdlEvent ddl(long timestamp, long serverId, String binlogFile, long binlogPosition, String database, String table) {
        DdlEvent ddlEvent = new DdlEvent(timestamp, serverId, binlogFile, binlogPosition);
        ddlEvent.setDatabase(database);
        ddlEvent.setTable(table);
        return ddlEvent;
    }
}
