package com.dianping.puma.admin.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.dianping.puma.core.sync.SyncConfig;
import com.google.code.morphia.dao.BasicDAO;

@Service("syncConfigDao")
public class SyncConfigDao extends BasicDAO<SyncConfig, String> {
    
    @Autowired     
    public SyncConfigDao(@Qualifier("mongoClient") MongoClient mongoClient) {
        super(mongoClient.getDatastore());
    }
}
