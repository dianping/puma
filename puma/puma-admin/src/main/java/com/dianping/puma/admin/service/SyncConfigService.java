package com.dianping.puma.admin.service;

import java.util.List;

import org.bson.types.ObjectId;

import com.dianping.puma.admin.bo.SyncXml;
import com.dianping.puma.core.sync.SyncConfig;

public interface SyncConfigService {

    public ObjectId saveSyncConfig(SyncConfig syncConfig);

    public List<SyncConfig> findSyncConfig(int offset, int limit);

    SyncXml loadSyncXml(ObjectId objectId);

    ObjectId modifySyncXml(SyncXml syncXml);

    ObjectId modifySyncConfig(SyncConfig syncConfig);

    ObjectId saveSyncXml(SyncXml syncXml);

}
