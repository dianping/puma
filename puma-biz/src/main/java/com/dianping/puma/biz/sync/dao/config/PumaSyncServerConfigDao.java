package com.dianping.puma.biz.sync.dao.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dianping.puma.biz.olddao.morphia.helper.MongoClient;
import com.dianping.puma.biz.sync.model.config.PumaSyncServerConfig;
import com.google.code.morphia.dao.BasicDAO;

@Service("pumaSyncServerConfigDao")
public class PumaSyncServerConfigDao extends BasicDAO<PumaSyncServerConfig, String> {

    @Autowired
    public PumaSyncServerConfigDao(MongoClient mongoClient) {
        super(mongoClient.getDatastore());
    }
}
