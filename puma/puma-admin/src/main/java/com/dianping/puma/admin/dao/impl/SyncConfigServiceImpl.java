package com.dianping.puma.admin.dao.impl;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;

import com.dianping.puma.admin.dao.SyncConfigService;
import com.dianping.puma.core.sync.SyncConfig;
import com.google.code.morphia.Datastore;
import com.google.code.morphia.Key;

public class SyncConfigServiceImpl implements SyncConfigService {
    @Autowired
    private MongoClient mongoClient;

    public ObjectId save(SyncConfig syncConfig) {
        Datastore ds = mongoClient.getDatastore();
        Key<SyncConfig> key =  ds.save(syncConfig);
        return (ObjectId) key.getId();
    }

}
