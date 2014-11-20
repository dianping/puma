package com.dianping.puma.syncserver.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dianping.puma.core.sync.dao.config.PumaServerConfigDao;
import com.dianping.puma.core.sync.model.config.PumaServerConfig;
import com.dianping.puma.syncserver.service.PumaServerConfigService;
import com.google.code.morphia.query.Query;

@Service("pumaServerConfigService")
public class PumaServerConfigServiceImpl implements PumaServerConfigService {
    @Autowired
    PumaServerConfigDao pumaServerConfigDao;

    @Override
    public PumaServerConfig find(String mysqlName) {
        Query<PumaServerConfig> q = pumaServerConfigDao.getDatastore().createQuery(PumaServerConfig.class);
        q.field("mysqlName").equal(mysqlName);
        return pumaServerConfigDao.findOne(q);
    }

}
