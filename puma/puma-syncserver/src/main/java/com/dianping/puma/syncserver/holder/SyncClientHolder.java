package com.dianping.puma.syncserver.holder;

import java.util.concurrent.ConcurrentHashMap;

import com.dianping.puma.syncserver.bo.SyncClient;

public class SyncClientHolder {

    private static ConcurrentHashMap<Long, SyncClient> syncCleintMap = new ConcurrentHashMap<Long, SyncClient>();

    public static SyncClient putIfAbsent(Long key, SyncClient value) {
        return syncCleintMap.putIfAbsent(key, value);
    }

    public static SyncClient get(Long key) {
        return syncCleintMap.get(key);
    }
    
    public static boolean contain(Long key){
        return syncCleintMap.containsKey(key);
    }

}
