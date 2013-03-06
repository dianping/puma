package com.dianping.puma.core.sync.dao.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dianping.puma.core.sync.dao.MongoClient;
import com.dianping.puma.core.sync.model.config.MysqlConfig;
import com.google.code.morphia.dao.BasicDAO;

@Service("mysqlConfigDao")
public class MysqlConfigDao extends BasicDAO<MysqlConfig, String> {

    @Autowired
    public MysqlConfigDao(MongoClient mongoClient) {
        super(mongoClient.getDatastore());
    }
}
