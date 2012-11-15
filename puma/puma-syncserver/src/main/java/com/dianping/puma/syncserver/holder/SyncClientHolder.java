package com.dianping.puma.syncserver.holder;

import java.util.concurrent.ConcurrentHashMap;

import com.dianping.puma.syncserver.bo.AbstractSyncClient;

public class SyncClientHolder {

    private static ConcurrentHashMap<Long, AbstractSyncClient> syncCleintMap = new ConcurrentHashMap<Long, AbstractSyncClient>();

    public static AbstractSyncClient putIfAbsent(Long key, AbstractSyncClient value) {
        return syncCleintMap.putIfAbsent(key, value);
    }

    public static AbstractSyncClient get(Long key) {
        return syncCleintMap.get(key);
    }

    public static boolean contain(Long key) {
        return syncCleintMap.containsKey(key);
    }

}
