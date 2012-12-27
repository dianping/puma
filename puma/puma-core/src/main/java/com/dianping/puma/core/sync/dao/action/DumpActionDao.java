package com.dianping.puma.core.sync.dao.action;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dianping.puma.core.sync.dao.MongoClient;
import com.dianping.puma.core.sync.model.action.DumpAction;
import com.google.code.morphia.dao.BasicDAO;

@Service("dumpActionDao")
public class DumpActionDao extends BasicDAO<DumpAction, String> {

    @Autowired
    public DumpActionDao(MongoClient mongoClient) {
        super(mongoClient.getDatastore());
    }
}
