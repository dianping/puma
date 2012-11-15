package com.dianping.puma.syncserver.holder;

import java.util.concurrent.ConcurrentHashMap;

import org.bson.types.ObjectId;

import com.dianping.puma.syncserver.bo.AbstractSyncClient;

public class SyncClientHolder {

    private static ConcurrentHashMap<ObjectId, AbstractSyncClient> syncCleintMap = new ConcurrentHashMap<ObjectId, AbstractSyncClient>();

    public static AbstractSyncClient putIfAbsent(ObjectId key, AbstractSyncClient value) {
        return syncCleintMap.putIfAbsent(key, value);
    }

    public static AbstractSyncClient get(ObjectId key) {
        return syncCleintMap.get(key);
    }

    public static boolean contain(ObjectId key) {
        return syncCleintMap.containsKey(key);
    }

}
