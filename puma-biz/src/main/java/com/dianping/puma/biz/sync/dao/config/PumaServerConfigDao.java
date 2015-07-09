package com.dianping.puma.biz.sync.dao.config;

import com.dianping.puma.biz.olddao.morphia.helper.MongoClient;
import com.dianping.puma.biz.sync.model.config.PumaServerConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.code.morphia.dao.BasicDAO;

@Service("pumaServerConfigDao")
public class PumaServerConfigDao extends BasicDAO<PumaServerConfig, String> {

    @Autowired
    public PumaServerConfigDao(MongoClient mongoClient) {
        super(mongoClient.getDatastore());
    }
}
