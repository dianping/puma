package com.dianping.puma.admin.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dianping.puma.admin.bo.SyncXml;
import com.dianping.puma.core.sync.dao.MongoClient;
import com.google.code.morphia.dao.BasicDAO;

@Service("syncXmlDao")
public class SyncXmlDao extends BasicDAO<SyncXml, String> {

    @Autowired
    public SyncXmlDao(MongoClient mongoClient) {
        super(mongoClient.getDatastore());
    }
}
