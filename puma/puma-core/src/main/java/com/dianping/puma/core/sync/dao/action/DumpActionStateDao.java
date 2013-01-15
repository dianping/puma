package com.dianping.puma.core.sync.dao.action;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dianping.puma.core.sync.dao.MongoClient;
import com.dianping.puma.core.sync.model.action.DumpActionState;
import com.google.code.morphia.dao.BasicDAO;

@Service("dumpActionStateDao")
public class DumpActionStateDao extends BasicDAO<DumpActionState, String> {

    @Autowired
    public DumpActionStateDao(MongoClient mongoClient) {
        super(mongoClient.getDatastore());
    }
}
