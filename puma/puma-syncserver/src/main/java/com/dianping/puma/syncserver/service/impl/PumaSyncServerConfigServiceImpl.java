package com.dianping.puma.syncserver.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dianping.puma.core.sync.dao.config.PumaSyncServerConfigDao;
import com.dianping.puma.core.sync.model.config.PumaSyncServerConfig;
import com.dianping.puma.syncserver.service.PumaSyncServerConfigService;
import com.google.code.morphia.query.Query;

@Service("pumaSyncServerConfigService")
public class PumaSyncServerConfigServiceImpl implements PumaSyncServerConfigService {
    @Autowired
    PumaSyncServerConfigDao pumaSyncServerConfigDao;

    @Override
    public PumaSyncServerConfig find(String host) {
        Query<PumaSyncServerConfig> q = pumaSyncServerConfigDao.getDatastore().createQuery(PumaSyncServerConfig.class);
        q.field("host").equal(host);
        return pumaSyncServerConfigDao.findOne(q);
    }

}
