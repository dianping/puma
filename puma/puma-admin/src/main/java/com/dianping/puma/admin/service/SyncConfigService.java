package com.dianping.puma.admin.service;

import java.util.List;

import org.bson.types.ObjectId;

import com.dianping.puma.admin.bo.SyncXml;
import com.dianping.puma.core.sync.SyncConfig;

public interface SyncConfigService {

    public ObjectId saveSyncConfig(SyncConfig syncConfig, String syncXmlString);

    public List<SyncConfig> findSyncConfigs(int offset, int limit);

    SyncXml findSyncXml(ObjectId objectId);

    SyncConfig findSyncConfig(ObjectId objectId);

    ObjectId modifySyncConfig(ObjectId id, SyncConfig syncConfig, String syncXmlString);

    Long countSyncConfigs();

}
