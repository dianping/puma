package com.dianping.puma.core.sync.dao.action;

import com.dianping.puma.core.sync.dao.MongoClient;
import com.dianping.puma.core.sync.model.action.CatchupAction;
import com.google.code.morphia.dao.BasicDAO;

public class CatupActionDao extends BasicDAO<CatchupAction, String> {

    public CatupActionDao(MongoClient mongoClient) {
        super(mongoClient.getDatastore());
    }
}
