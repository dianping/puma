package com.dianping.puma.syncserver.service;

import java.util.List;

import org.bson.types.ObjectId;

import com.dianping.puma.core.sync.SyncConfig;

public interface SyncConfigService {

    /**
     * 根据puma-syncserver id，查询对应的所有SyncTask，再查询对应的所有SyncConfig
     */
    public List<SyncConfig> findSyncConfigs(String pumaSyncServerId);

    /**
     * 根据task id，查询对应的SyncTask,再查询对应的SyncConfig
     */
    SyncConfig findSyncConfig(ObjectId taskId);

}
