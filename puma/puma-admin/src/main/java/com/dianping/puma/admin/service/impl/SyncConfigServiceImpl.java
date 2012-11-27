package com.dianping.puma.admin.service.impl;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dianping.puma.admin.bo.SyncXml;
import com.dianping.puma.admin.dao.SyncConfigDao;
import com.dianping.puma.admin.dao.SyncXmlDao;
import com.dianping.puma.admin.service.SyncConfigService;
import com.dianping.puma.core.sync.SyncConfig;
import com.google.code.morphia.Key;
import com.google.code.morphia.query.Query;
import com.google.code.morphia.query.QueryResults;

@Service("syncConfigService")
public class SyncConfigServiceImpl implements SyncConfigService {
    @Autowired
    SyncConfigDao syncConfigDao;

    @Autowired
    SyncXmlDao syncXmlDao;

    @Override
    public ObjectId saveSyncConfig(SyncConfig syncConfig) {
        if (this.existsBySrcAndDest(syncConfig.getSrc().getServerId(), syncConfig.getSrc().getTarget(), syncConfig.getDest()
                .getHost())) {
            throw new IllegalArgumentException("创建失败，已有相同的配置存在。(src.serverId=" + syncConfig.getSrc().getServerId() + ",src,target="
                    + syncConfig.getSrc().getTarget() + ",dest.host=" + syncConfig.getDest() + ")");
        }
        Key<SyncConfig> key = syncConfigDao.save(syncConfig);
        return (ObjectId) key.getId();
    }

    @Override
    public ObjectId saveSyncXml(SyncXml syncXml) {
        Key<SyncXml> key = syncXmlDao.save(syncXml);
        return (ObjectId) key.getId();
    }

    @Override
    public ObjectId modifySyncConfig(SyncConfig syncConfig) {
        if (this.existsBySrcAndDest(syncConfig.getSrc().getServerId(), syncConfig.getSrc().getTarget(), syncConfig.getDest()
                .getHost())) {
            throw new IllegalArgumentException("创建失败，已有相同的配置存在。(src.serverId=" + syncConfig.getSrc().getServerId() + ",src,target="
                    + syncConfig.getSrc().getTarget() + ",dest.host=" + syncConfig.getDest() + ")");
        }
        Key<SyncConfig> key = syncConfigDao.save(syncConfig);
        return (ObjectId) key.getId();
    }

    @Override
    public ObjectId modifySyncXml(SyncXml syncXml) {
        Key<SyncXml> key = syncXmlDao.save(syncXml);
        return (ObjectId) key.getId();
    }

    private boolean existsBySrcAndDest(Long serverId, String target, String host) {
        Query<SyncConfig> q = syncConfigDao.getDatastore().createQuery(SyncConfig.class);
        q.field("dest.host").equal(host);
        q.field("src.serverId").equal(serverId);
        q.field("src.target").equal(target);

        return syncConfigDao.exists(q);
    }

    @Override
    public List<SyncConfig> findSyncConfig(int offset, int limit) {
        Query<SyncConfig> q = syncConfigDao.getDatastore().createQuery(SyncConfig.class).field("dest.username").equal("binlog");
        q.offset(offset);
        q.limit(limit);
        QueryResults<SyncConfig> result = syncConfigDao.find(q);
        return result.asList();
    }

    @Override
    public SyncXml loadSyncXml(ObjectId objectId) {
        return syncXmlDao.getDatastore().getByKey(SyncXml.class, new Key<SyncXml>(SyncXml.class, objectId));
    }
}
