package com.dianping.puma.core.event;

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

    public static RowChangedEvent ddl(long timestamp, long serverId, String binlogFile, long binlogPosition, String database, String table) {
        return null;
    }
}
