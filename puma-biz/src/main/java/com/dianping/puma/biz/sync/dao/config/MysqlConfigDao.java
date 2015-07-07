package com.dianping.puma.biz.sync.dao.config;

import com.dianping.puma.biz.dao.morphia.helper.MongoClient;
import com.dianping.puma.biz.sync.model.config.MysqlConfig;
import com.google.code.morphia.dao.BasicDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("mysqlConfigDao")
public class MysqlConfigDao extends BasicDAO<MysqlConfig, String> {

    @Autowired
    public MysqlConfigDao(MongoClient mongoClient) {
        super(mongoClient.getDatastore());
    }
}
