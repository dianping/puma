package com.dianping.puma.core.sync.dao;

import com.dianping.puma.core.sync.SyncTask;
import com.google.code.morphia.dao.BasicDAO;

public class SyncTaskDao extends BasicDAO<SyncTask, String> {

    public SyncTaskDao(MongoClient mongoClient) {
        super(mongoClient.getDatastore());
    }
}
