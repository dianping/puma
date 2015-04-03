package com.dianping.puma.core.service.impl;

import com.dianping.puma.core.dao.ShardDumpTaskDao;
import com.dianping.puma.core.entity.ShardDumpTask;
import com.dianping.puma.core.service.ShardDumpTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("shardDumpTaskService")
public class ShardDumpTaskServiceImpl implements ShardDumpTaskService {

    @Autowired
    ShardDumpTaskDao shardDumpTaskDao;

    public ShardDumpTask find(String name) {
        return shardDumpTaskDao.find(name);
    }

    public List<ShardDumpTask> findBySyncServerName(String syncServerName) {
        return shardDumpTaskDao.findBySyncServerName(syncServerName);
    }

    public List<ShardDumpTask> findAll() {
        return shardDumpTaskDao.findAll();
    }

    public void create(ShardDumpTask syncTask) {
        shardDumpTaskDao.create(syncTask);
    }

    public void remove(String name) {
        shardDumpTaskDao.remove(name);
    }

    public List<ShardDumpTask> find(int offset, int limit) {
        return shardDumpTaskDao.find(offset, limit);
    }

    public void update(ShardDumpTask shardDumpTask) {
        shardDumpTaskDao.update(shardDumpTask);
    }
}
