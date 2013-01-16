package com.dianping.puma.core.sync.dao.action;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dianping.puma.core.sync.dao.MongoBaseDao;
import com.dianping.puma.core.sync.dao.MongoClient;
import com.dianping.puma.core.sync.model.action.DumpAction;

@Service("dumpActionDao")
public class DumpActionDao extends MongoBaseDao<DumpAction> {

    @Autowired
    public DumpActionDao(MongoClient mongoClient) {
        super(mongoClient.getDatastore());
    }
}
