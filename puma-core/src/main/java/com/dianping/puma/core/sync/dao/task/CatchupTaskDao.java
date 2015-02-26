package com.dianping.puma.core.sync.dao.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dianping.puma.core.dao.morphia.MongoBaseDao;
import com.dianping.puma.core.dao.morphia.MongoClient;
import com.dianping.puma.core.sync.model.task.CatchupTask;

@Service
public class CatchupTaskDao extends MongoBaseDao<CatchupTask> {

    @Autowired
    public CatchupTaskDao(MongoClient mongoClient) {
        super(mongoClient.getDatastore());
    }
}
