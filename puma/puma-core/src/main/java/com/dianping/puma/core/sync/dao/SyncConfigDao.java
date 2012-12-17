package com.dianping.puma.core.sync.dao;

import com.dianping.puma.core.sync.SyncConfig;
import com.google.code.morphia.dao.BasicDAO;

public class SyncConfigDao extends BasicDAO<SyncConfig, String> {

    public SyncConfigDao(MongoClient mongoClient) {
        super(mongoClient.getDatastore());
    }
}
