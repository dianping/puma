package com.dianping.puma.core.sync.dao.action;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dianping.puma.core.sync.dao.MongoBaseDao;
import com.dianping.puma.core.sync.dao.MongoClient;
import com.dianping.puma.core.sync.model.action.DumpActionState;

@Service("dumpActionStateDao")
public class DumpActionStateDao extends MongoBaseDao<DumpActionState> {

    @Autowired
    public DumpActionStateDao(MongoClient mongoClient) {
        super(mongoClient.getDatastore());
    }
}
