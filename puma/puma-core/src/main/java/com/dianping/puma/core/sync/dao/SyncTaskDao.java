package com.dianping.puma.core.sync.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dianping.puma.core.sync.SyncTask;
import com.google.code.morphia.dao.BasicDAO;

@Service("syncTaskDao")
public class SyncTaskDao extends BasicDAO<SyncTask, String> {

    @Autowired
    public SyncTaskDao(MongoClient mongoClient) {
        super(mongoClient.getDatastore());
    }
}
