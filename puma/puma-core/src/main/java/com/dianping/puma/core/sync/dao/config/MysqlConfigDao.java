package com.dianping.puma.core.sync.dao.config;

import com.dianping.puma.core.sync.dao.MongoClient;
import com.dianping.puma.core.sync.model.config.MysqlConfig;
import com.google.code.morphia.dao.BasicDAO;

public class MysqlConfigDao extends BasicDAO<MysqlConfig, String> {

    public MysqlConfigDao(MongoClient mongoClient) {
        super(mongoClient.getDatastore());
    }
}
