package com.dianping.puma.core.sync.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dianping.puma.core.sync.model.action.SyncTaskAction;

@Service("syncTaskDao")
public class SyncTaskDao extends MongoBaseDao<SyncTaskAction> {

    @Autowired
    public SyncTaskDao(MongoClient mongoClient) {
        super(mongoClient.getDatastore());
    }
}
