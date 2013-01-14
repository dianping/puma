package com.dianping.puma.core.sync.dao.action;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dianping.puma.core.sync.dao.MongoClient;
import com.dianping.puma.core.sync.model.action.SyncTaskActionState;
import com.google.code.morphia.dao.BasicDAO;

@Service("syncTaskActionStateDao")
public class SyncTaskActionStateDao extends BasicDAO<SyncTaskActionState, String> {

    @Autowired
    public SyncTaskActionStateDao(MongoClient mongoClient) {
        super(mongoClient.getDatastore());
    }
}
