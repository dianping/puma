package com.dianping.puma.admin.dao;

import com.dianping.puma.admin.bo.SyncXml;
import com.dianping.puma.core.sync.dao.MongoClient;
import com.google.code.morphia.dao.BasicDAO;

public class SyncXmlDao extends BasicDAO<SyncXml, String> {

    public SyncXmlDao(MongoClient mongoClient) {
        super(mongoClient.getDatastore());
    }
}
