package com.dianping.puma.admin.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.dianping.puma.admin.bo.SyncXml;
import com.google.code.morphia.dao.BasicDAO;

@Service("syncXmlDao")
public class SyncXmlDao extends BasicDAO<SyncXml, String> {

    @Autowired
    public SyncXmlDao(@Qualifier("mongoClient") MongoClient mongoClient) {
        super(mongoClient.getDatastore());
    }
}
