package com.dianping.puma.core.sync.dao.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dianping.puma.core.dao.morphia.MongoBaseDao;
import com.dianping.puma.core.dao.morphia.MongoClient;
import com.dianping.puma.core.sync.model.task.DumpTask;


public class DumpTaskDao extends MongoBaseDao<DumpTask> {

    @Autowired
    public DumpTaskDao(MongoClient mongoClient) {
        super(mongoClient.getDatastore());
    }
}
