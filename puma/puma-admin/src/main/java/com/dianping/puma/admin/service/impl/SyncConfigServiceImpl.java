package com.dianping.puma.admin.service.impl;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dianping.puma.admin.dao.MongoClient;
import com.dianping.puma.admin.dao.SyncConfigDao;
import com.dianping.puma.admin.service.SyncConfigService;
import com.dianping.puma.core.sync.SyncConfig;
import com.google.code.morphia.Datastore;
import com.google.code.morphia.Key;
import com.google.code.morphia.query.Query;

@Service("syncConfigService")
public class SyncConfigServiceImpl implements SyncConfigService {
    @Autowired
    SyncConfigDao syncConfigDao;

    public ObjectId save(SyncConfig syncConfig) {
        Key<SyncConfig> key = syncConfigDao.save(syncConfig);
        return (ObjectId) key.getId();
    }

    public SyncConfig find() {
        Query<SyncConfig> q = syncConfigDao.getDatastore().createQuery(SyncConfig.class).field("dest.username").equal("binlog");

        SyncConfig syncConfig = syncConfigDao.findOne(q);
        return syncConfig;
    }

}
