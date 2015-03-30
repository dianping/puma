package com.dianping.puma.core.service.impl;

import com.dianping.puma.core.dao.ShardSyncTaskDao;
import com.dianping.puma.core.entity.ShardSyncTask;
import com.dianping.puma.core.service.ShardSyncTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("shardSyncTaskService")
public class ShardSyncTaskServiceImpl implements ShardSyncTaskService {

    @Autowired
    ShardSyncTaskDao syncTaskDao;

    public ShardSyncTask find(String name) {
        return syncTaskDao.find(name);
    }

    public List<ShardSyncTask> findBySyncServerName(String syncServerName) {
        return syncTaskDao.findBySyncServerName(syncServerName);
    }

    public List<ShardSyncTask> findAll() {
        return syncTaskDao.findAll();
    }

    public void create(ShardSyncTask syncTask) {
        syncTaskDao.create(syncTask);
    }

    public void remove(String name) {
        syncTaskDao.remove(name);
    }

    public List<ShardSyncTask> find(int offset, int limit) {
        return syncTaskDao.find(offset, limit);
    }
}
