package com.dianping.puma.core.dao;

import com.dianping.puma.core.entity.ShardDumpTask;

import java.util.List;

public interface ShardDumpTaskDao {

    ShardDumpTask find(String name);

    List<ShardDumpTask> findBySyncServerName(String syncServerName);

    List<ShardDumpTask> findAll();

    void create(ShardDumpTask syncTask);

    void remove(String name);

    List<ShardDumpTask> find(int offset, int limit);

    void update(ShardDumpTask shardDumpTask);
}
