package com.dianping.puma.admin.service.impl;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dianping.puma.admin.bo.SyncXml;
import com.dianping.puma.admin.dao.SyncConfigDao;
import com.dianping.puma.admin.dao.SyncXmlDao;
import com.dianping.puma.admin.service.SyncConfigService;
import com.dianping.puma.core.sync.DatabaseConfig;
import com.dianping.puma.core.sync.InstanceConfig;
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
    public ObjectId saveSyncConfig(SyncConfig syncConfig, String syncXmlString) {
        if (this.existsBySrcAndDest(syncConfig.getSrc().getServerId(), syncConfig.getSrc().getTarget(), syncConfig.getDest()
                .getHost())) {
            throw new IllegalArgumentException("创建失败，已有相同的配置存在。(src.serverId=" + syncConfig.getSrc().getServerId() + ",src,target="
                    + syncConfig.getSrc().getTarget() + ",dest.host=" + syncConfig.getDest() + ")");
        }
        //保存syncConfig和syncXml
        Key<SyncConfig> key = syncConfigDao.save(syncConfig);
        ObjectId id = (ObjectId) key.getId();
        SyncXml syncXml = new SyncXml();
        syncXml.setId(id);
        syncXml.setXml(syncXmlString);
        this._saveSyncXml(syncXml);
        return id;
    }

    private ObjectId _saveSyncXml(SyncXml syncXml) {
        Key<SyncXml> key = syncXmlDao.save(syncXml);
        return (ObjectId) key.getId();
    }

    @Override
    public ObjectId modifySyncConfig(ObjectId id, SyncConfig newSyncConfig, String syncXmlString) {
        //检验是否合法
        SyncConfig oldSyncConfig = syncConfigDao.getDatastore().getByKey(SyncConfig.class,
                new Key<SyncConfig>(SyncConfig.class, id));
        this._compare(oldSyncConfig, newSyncConfig);
        //保存
        newSyncConfig.setId(id);
        Key<SyncConfig> key = syncConfigDao.save(newSyncConfig);
        SyncXml syncXml = new SyncXml();
        syncXml.setId(id);
        syncXml.setXml(syncXmlString);
        this._saveSyncXml(syncXml);
        return (ObjectId) key.getId();
    }

    /**
     * 修改sync <br>
     * 对比新旧sync，求出新增的database或table配置(table也属于database下，故返回的都是database)<br>
     * 同时做验证：只允许新增database或table配置
     */
    private List<DatabaseConfig> _compare(SyncConfig oldSync, SyncConfig newSync) {
        //首先验证基础属性（dest，name，serverId，target）是否一致
        if (!oldSync.getDest().equals(newSync.getDest())) {
            throw new IllegalArgumentException("dest不一致！");
        }
        if (!oldSync.getSrc().getName().equals(newSync.getSrc().getName())) {
            throw new IllegalArgumentException("name不一致！");
        }
        if (!oldSync.getSrc().getServerId().equals(newSync.getSrc().getServerId())) {
            throw new IllegalArgumentException("serverId不一致！");
        }
        if (!oldSync.getSrc().getTarget().equals(newSync.getSrc().getTarget())) {
            throw new IllegalArgumentException("target不一致！");
        }
        //对比instance
        InstanceConfig oldInstanceConfig = oldSync.getInstance();
        InstanceConfig newInstanceConfig = newSync.getInstance();
        List<DatabaseConfig> databaseConfig = oldInstanceConfig.compare(newInstanceConfig);
        return databaseConfig;
    }

    private boolean existsBySrcAndDest(Long serverId, String target, String host) {
        Query<SyncConfig> q = syncConfigDao.getDatastore().createQuery(SyncConfig.class);
        q.field("dest.host").equal(host);
        q.field("src.serverId").equal(serverId);
        q.field("src.target").equal(target);

        return syncConfigDao.exists(q);
    }

    @Override
    public List<SyncConfig> findSyncConfigs(int offset, int limit) {
        Query<SyncConfig> q = syncConfigDao.getDatastore().createQuery(SyncConfig.class);
        q.offset(offset);
        q.limit(limit);
        QueryResults<SyncConfig> result = syncConfigDao.find(q);
        return result.asList();
    }

    @Override
    public Long countSyncConfigs() {
        Query<SyncConfig> q = syncConfigDao.getDatastore().createQuery(SyncConfig.class);
        return syncConfigDao.count(q);
    }

    @Override
    public SyncXml findSyncXml(ObjectId objectId) {
        return syncXmlDao.getDatastore().getByKey(SyncXml.class, new Key<SyncXml>(SyncXml.class, objectId));
    }
}
