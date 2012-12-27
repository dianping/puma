package com.dianping.puma.core.sync.dao.action;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dianping.puma.core.sync.dao.MongoClient;
import com.dianping.puma.core.sync.model.action.SyncTaskAction;
import com.google.code.morphia.dao.BasicDAO;

@Service("syncTaskActionDao")
public class SyncTaskActionDao extends BasicDAO<SyncTaskAction, String> {

    @Autowired
    public SyncTaskActionDao(MongoClient mongoClient) {
        super(mongoClient.getDatastore());
    }
}
