package com.dianping.puma.core.service;

import com.dianping.puma.core.entity.ShardSyncTask;

import java.util.List;

public interface ShardSyncTaskService {

    ShardSyncTask find(String name);

    List<ShardSyncTask> findBySyncServerName(String syncServerName);

    List<ShardSyncTask> findAll();

    void create(ShardSyncTask syncTask);

    void remove(String name);

    List<ShardSyncTask> find(int offset, int limit);
}
