package com.dianping.puma.core.sync.dao.config;

import com.dianping.puma.core.sync.dao.MongoClient;
import com.dianping.puma.core.sync.model.config.MysqlHost;
import com.google.code.morphia.dao.BasicDAO;

public class MysqlHostDao extends BasicDAO<MysqlHost, String> {

    public MysqlHostDao(MongoClient mongoClient) {
        super(mongoClient.getDatastore());
    }
}
