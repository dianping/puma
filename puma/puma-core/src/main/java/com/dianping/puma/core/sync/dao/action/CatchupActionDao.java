package com.dianping.puma.core.sync.dao.action;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dianping.puma.core.sync.dao.MongoClient;
import com.dianping.puma.core.sync.model.action.CatchupAction;
import com.google.code.morphia.dao.BasicDAO;

@Service("catupActionDao")
public class CatupActionDao extends BasicDAO<CatchupAction, String> {

    @Autowired
    public CatupActionDao(MongoClient mongoClient) {
        super(mongoClient.getDatastore());
    }
}
