package com.dianping.puma.core.sync.dao.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dianping.puma.core.sync.dao.MongoClient;
import com.dianping.puma.core.sync.model.config.MysqlPumaServerConfig;
import com.google.code.morphia.dao.BasicDAO;

@Service("mysqlPumaServerConfigDao")
public class MysqlPumaServerConfigDao extends BasicDAO<MysqlPumaServerConfig, String> {

    @Autowired
    public MysqlPumaServerConfigDao(MongoClient mongoClient) {
        super(mongoClient.getDatastore());
    }
}
