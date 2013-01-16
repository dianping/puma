package com.dianping.puma.core.sync.dao.action;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dianping.puma.core.sync.dao.MongoBaseDao;
import com.dianping.puma.core.sync.dao.MongoClient;
import com.dianping.puma.core.sync.model.action.SyncTaskAction;

@Service("syncTaskActionDao")
public class SyncTaskActionDao extends MongoBaseDao<SyncTaskAction> {

    @Autowired
    public SyncTaskActionDao(MongoClient mongoClient) {
        super(mongoClient.getDatastore());
    }
}
