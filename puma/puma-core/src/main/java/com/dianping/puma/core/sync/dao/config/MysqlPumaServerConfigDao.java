package com.dianping.puma.core.sync.dao.config;

import com.dianping.puma.core.sync.dao.MongoClient;
import com.dianping.puma.core.sync.model.config.MysqlPumaServerConfig;
import com.google.code.morphia.dao.BasicDAO;

public class MysqlPumaServerConfigDao extends BasicDAO<MysqlPumaServerConfig, String> {

    public MysqlPumaServerConfigDao(MongoClient mongoClient) {
        super(mongoClient.getDatastore());
    }
}
