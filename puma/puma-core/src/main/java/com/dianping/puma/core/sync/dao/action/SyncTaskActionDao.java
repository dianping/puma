package com.dianping.puma.core.sync.dao.action;

import com.dianping.puma.core.sync.dao.MongoClient;
import com.dianping.puma.core.sync.model.action.SyncTaskAction;
import com.google.code.morphia.dao.BasicDAO;

public class SyncTaskActionDao extends BasicDAO<SyncTaskAction, String> {

    public SyncTaskActionDao(MongoClient mongoClient) {
        super(mongoClient.getDatastore());
    }
}
