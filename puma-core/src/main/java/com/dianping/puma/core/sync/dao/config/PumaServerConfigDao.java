package com.dianping.puma.core.sync.dao.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dianping.puma.core.dao.morphia.MongoClient;
import com.dianping.puma.core.sync.model.config.PumaServerConfig;
import com.google.code.morphia.dao.BasicDAO;

@Service("pumaServerConfigDao")
public class PumaServerConfigDao extends BasicDAO<PumaServerConfig, String> {

    @Autowired
    public PumaServerConfigDao(MongoClient mongoClient) {
        super(mongoClient.getDatastore());
    }
}
