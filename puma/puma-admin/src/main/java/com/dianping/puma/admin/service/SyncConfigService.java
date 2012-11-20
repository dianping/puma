package com.dianping.puma.admin.service;

import org.bson.types.ObjectId;

import com.dianping.puma.core.sync.SyncConfig;

public interface SyncConfigService {

    public ObjectId save(SyncConfig syncConfig);
    public SyncConfig find();

}
