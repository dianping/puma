package com.dianping.puma.syncserver.holder;

import java.util.concurrent.ConcurrentHashMap;

import org.bson.types.ObjectId;

import com.dianping.puma.syncserver.job.executor.AbstractTaskExecutor;

public class SyncClientHolder {

    private static ConcurrentHashMap<ObjectId, AbstractTaskExecutor> syncCleintMap = new ConcurrentHashMap<ObjectId, AbstractTaskExecutor>();

    public static AbstractTaskExecutor putIfAbsent(ObjectId key, AbstractTaskExecutor value) {
        return syncCleintMap.putIfAbsent(key, value);
    }

    public static AbstractTaskExecutor get(ObjectId key) {
        return syncCleintMap.get(key);
    }

    public static boolean contain(ObjectId ObjectId) {
        return syncCleintMap.containsKey(ObjectId);
    }

}
