package com.dianping.puma.core.sync.dao.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dianping.puma.core.sync.dao.MongoClient;
import com.dianping.puma.core.sync.model.config.MysqlHost;
import com.google.code.morphia.dao.BasicDAO;

@Service("mysqlConfigDao")
public class MysqlHostDao extends BasicDAO<MysqlHost, String> {

    @Autowired
    public MysqlHostDao(MongoClient mongoClient) {
        super(mongoClient.getDatastore());
    }
}
