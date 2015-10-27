package com.dianping.puma.storage.channel;

import java.util.List;

public final class ChannelFactory {

    private ChannelFactory() {
    }

    public static DefaultReadChannel newReadChannel(String database) {
        return new DefaultReadChannel(database);
    }

    public static DefaultReadChannel newReadChannel(String database, List<String> tables, boolean dml, boolean ddl, boolean transaction) {
        return new DefaultReadChannel(database, tables, dml, ddl, transaction);
    }

    public static DefaultWriteChannel newWriteChannel(String database) {
        return new DefaultWriteChannel(database);
    }
}
