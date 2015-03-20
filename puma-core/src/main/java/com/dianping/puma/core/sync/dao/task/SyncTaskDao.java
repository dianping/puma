package com.dianping.puma.core.sync.dao.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dianping.puma.core.dao.morphia.MongoBaseDao;
import com.dianping.puma.core.dao.morphia.MongoClient;
import com.dianping.puma.core.sync.model.task.SyncTask;


public class SyncTaskDao extends MongoBaseDao<SyncTask> {

    @Autowired
    public SyncTaskDao(MongoClient mongoClient) {
        super(mongoClient.getDatastore());
    }
}
