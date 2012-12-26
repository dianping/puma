package com.dianping.puma.core.sync.dao.action;

import com.dianping.puma.core.sync.dao.MongoClient;
import com.dianping.puma.core.sync.model.action.DumpAction;
import com.google.code.morphia.dao.BasicDAO;

public class DumpActionDao extends BasicDAO<DumpAction, String> {

    public DumpActionDao(MongoClient mongoClient) {
        super(mongoClient.getDatastore());
    }
}
