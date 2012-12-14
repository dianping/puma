package com.dianping.puma.admin.service;

import java.sql.SQLException;
import java.util.List;

import org.bson.types.ObjectId;

import com.dianping.puma.admin.bo.SyncXml;
import com.dianping.puma.core.sync.BinlogInfo;
import com.dianping.puma.core.sync.DumpConfig;
import com.dianping.puma.core.sync.SyncConfig;

public interface SyncConfigService {

    public ObjectId saveSyncConfig(SyncConfig syncConfig, String syncXmlString);

    void modifySyncConfig(SyncConfig syncConfig, String syncXmlString);

    /**
     * 仅更新SyncConfig的binlog信息
     */
    void modifySyncConfig(ObjectId id, BinlogInfo binlogInfo);

    /**
     * 删除相应的SyncConfig和SyncXml
     */
    void removeSyncConfig(ObjectId id);

    public List<SyncConfig> findSyncConfigs(int offset, int limit);

    SyncXml findSyncXml(ObjectId objectId);

    SyncConfig findSyncConfig(ObjectId objectId);

    Long countSyncConfigs();

    /**
     * 将syncConfig转化成dumpConfig(包括对*转化成具体table；去除无法dump的table)
     * 
     * @throws SQLException
     */
    DumpConfig convertSyncConfigToDumpConfig(SyncConfig syncConfig) throws SQLException;

}
