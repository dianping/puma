package com.dianping.puma.admin.service.impl;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dianping.puma.admin.bo.SyncXml;
import com.dianping.puma.admin.config.PropertiesConfig;
import com.dianping.puma.admin.dao.SyncXmlDao;
import com.dianping.puma.admin.service.SyncConfigService;
import com.dianping.puma.admin.util.MysqlMetaInfoFetcher;
import com.dianping.puma.core.sync.BinlogInfo;
import com.dianping.puma.core.sync.ColumnConfig;
import com.dianping.puma.core.sync.DatabaseConfig;
import com.dianping.puma.core.sync.DumpConfig;
import com.dianping.puma.core.sync.DumpConfig.DumpDest;
import com.dianping.puma.core.sync.DumpConfig.DumpSrc;
import com.dianping.puma.core.sync.InstanceConfig;
import com.dianping.puma.core.sync.SyncConfig;
import com.dianping.puma.core.sync.SyncDest;
import com.dianping.puma.core.sync.SyncTask;
import com.dianping.puma.core.sync.TableConfig;
import com.dianping.puma.core.sync.dao.SyncConfigDao;
import com.dianping.puma.core.sync.dao.SyncTaskDao;
import com.google.code.morphia.Key;
import com.google.code.morphia.query.Query;
import com.google.code.morphia.query.QueryResults;

@Service("syncConfigService")
public class SyncConfigServiceImpl implements SyncConfigService {
    @Autowired
    SyncConfigDao syncConfigDao;

    @Autowired
    SyncXmlDao syncXmlDao;

    @Autowired
    SyncTaskDao syncTaskDao;

    @Override
    public ObjectId saveSyncConfig(SyncConfig syncConfig, String syncXmlString) {
        if (this.existsBySrcAndDest(syncConfig.getSrc().getServerId(), syncConfig.getSrc().getTarget(), syncConfig.getDest()
                .getHost())) {
            throw new IllegalArgumentException("创建失败，已有相同的配置存在。(src.serverId=" + syncConfig.getSrc().getServerId() + ",src,target="
                    + syncConfig.getSrc().getTarget() + ",dest.host=" + syncConfig.getDest() + ")");
        }
        //验证仅有一个databaseConfig
        if (syncConfig.getInstance().getDatabases() == null || syncConfig.getInstance().getDatabases().size() == 0
                || syncConfig.getInstance().getDatabases().size() > 1) {
            throw new IllegalArgumentException("创建失败，<database>配置必须有且仅能有一个！");
        }
        //验证table
        if (syncConfig.getInstance().getDatabases().get(0).getTables() == null
                || syncConfig.getInstance().getDatabases().get(0).getTables().size() == 0) {
            throw new IllegalArgumentException("创建失败，<table>配置必须至少有一个！");
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
    public void modifySyncConfig(SyncConfig newSyncConfig, String syncXmlString) {
        //检验是否合法
        SyncConfig oldSyncConfig = syncConfigDao.getDatastore().getByKey(SyncConfig.class,
                new Key<SyncConfig>(SyncConfig.class, newSyncConfig.getId()));
        this._compare(oldSyncConfig, newSyncConfig);
        //保存
        syncConfigDao.save(newSyncConfig);
        SyncXml syncXml = new SyncXml();
        syncXml.setId(newSyncConfig.getId());
        syncXml.setXml(syncXmlString);
        this._saveSyncXml(syncXml);
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
        if (oldSync.getSrc().getServerId() != newSync.getSrc().getServerId()) {
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

    @Override
    public SyncConfig findSyncConfig(ObjectId objectId) {
        return this.syncConfigDao.getDatastore().getByKey(SyncConfig.class, new Key<SyncConfig>(SyncConfig.class, objectId));
    }

    @Override
    public DumpConfig convertSyncConfigToDumpConfig(SyncConfig syncConfig) throws SQLException {
        DumpConfig dumpConfig = new DumpConfig();
        //dumpSrc
        long serverId = syncConfig.getSrc().getServerId();
        DumpSrc dumpSrc = PropertiesConfig.getInstance().getDumpConfigSrc(serverId);
        if (dumpSrc == null) {
            throw new IllegalArgumentException("serverId 对应的 mysql信息不存在，请注意该映射关系是需要在config文件作配置的。");
        }
        dumpConfig.setSrc(dumpSrc);
        SyncDest syncDest = syncConfig.getDest();
        //dumpDest
        DumpDest dumpDest = new DumpDest();
        dumpDest.setHost(syncDest.getHost());
        dumpDest.setUsername(syncDest.getUsername());
        dumpDest.setPassword(syncDest.getPassword());
        dumpConfig.setDest(dumpDest);
        //dumpDatabaseConfigs 遍历SyncConfig的DatabaseConfig，支持db和table名称改变，字段名称不支持改变。
        InstanceConfig instance = syncConfig.getInstance();
        List<DatabaseConfig> databaseConfigs = instance.getDatabases();
        List<DatabaseConfig> dumpDatabaseConfigs = new ArrayList<DatabaseConfig>();
        dumpConfig.setDatabaseConfigs(dumpDatabaseConfigs);
        for (DatabaseConfig databaseConfig : databaseConfigs) {
            String databaseConfigFrom = databaseConfig.getFrom();
            String databaseConfigTo = databaseConfig.getTo();
            List<TableConfig> dumpTableConfigs = new ArrayList<TableConfig>();
            //遍历table配置
            List<TableConfig> tableConfigs = databaseConfig.getTables();
            for (TableConfig tableConfig : tableConfigs) {
                String tableConfigFrom = tableConfig.getFrom();
                String tableConfigTo = tableConfig.getTo();
                //如果是from=*,to=*，则需要从数据库获取实际的表（排除已经列出的table配置）
                if (StringUtils.equals(tableConfigFrom, "*") && StringUtils.equals(tableConfigTo, "*")) {
                    //访问数据库，得到该数据库下的所有表名(*配置是在最后的，所以排除已经列出的table配置就是排除dumpTableConfigs)
                    MysqlMetaInfoFetcher mysqlExecutor = new MysqlMetaInfoFetcher(dumpSrc.getHost(), dumpSrc.getUsername(),
                            dumpSrc.getPassword());
                    List<String> tableNames;
                    try {
                        tableNames = mysqlExecutor.getTables(databaseConfigFrom);
                    } finally {
                        mysqlExecutor.close();
                    }
                    getRidOf(tableNames, dumpTableConfigs);
                    for (String tableName : tableNames) {
                        TableConfig dumpTableConfig = new TableConfig();
                        dumpTableConfig.setFrom(tableName);
                        dumpTableConfig.setTo(tableName);
                        dumpTableConfigs.add(dumpTableConfig);
                    }
                } else {//如果“table下的字段没有被重命名,partOf为false”，那么该table可以被dump
                    if (shouldDump(tableConfig)) {
                        TableConfig dumpTableConfig = new TableConfig();
                        dumpTableConfig.setFrom(tableConfig.getFrom());
                        dumpTableConfig.setTo(tableConfig.getTo());
                        dumpTableConfigs.add(dumpTableConfig);
                    }
                }
            }
            //database需要dump(如果下属table没有需要dump则该database也不需要)
            if (dumpTableConfigs.size() > 0) {
                DatabaseConfig dumpDatabaseConfig = new DatabaseConfig();
                dumpDatabaseConfig.setFrom(databaseConfigFrom);
                dumpDatabaseConfig.setTo(databaseConfigTo);
                dumpDatabaseConfig.setTables(dumpTableConfigs);
                dumpDatabaseConfigs.add(dumpDatabaseConfig);
            }
        }

        return dumpConfig;
    }

    /**
     * 从tableNames中去掉已经存在dumpTableConfigs(以TableConfig.getFrom()判断)中的表名
     */
    private void getRidOf(List<String> tableNames, List<TableConfig> dumpTableConfigs) {
        Collection<String> dumpTableNames = new ArrayList<String>();
        for (TableConfig tableConfig : dumpTableConfigs) {
            dumpTableNames.add(tableConfig.getFrom());
        }
        tableNames.removeAll(dumpTableNames);
    }

    /**
     * 如果“table下的字段没有被重命名,partOf为false”，那么该table可以被dump
     */
    private boolean shouldDump(TableConfig tableConfig) {
        if (tableConfig.isPartOf()) {
            return false;
        }
        List<ColumnConfig> columnConfigs = tableConfig.getColumns();
        for (ColumnConfig columnConfig : columnConfigs) {
            if (!StringUtils.equalsIgnoreCase(columnConfig.getFrom(), columnConfig.getTo())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void modifySyncConfig(ObjectId syncConfigId, BinlogInfo binlogInfo) {
        SyncConfig syncConfig = this.syncConfigDao.getDatastore().getByKey(SyncConfig.class,
                new Key<SyncConfig>(SyncConfig.class, syncConfigId));
        //更新binloginfo
        syncConfig.getSrc().setBinlogInfo(binlogInfo);
        //保存
        syncConfigDao.save(syncConfig);
    }

    @Override
    public void removeSyncConfig(ObjectId id) {
        Query<SyncConfig> q = syncConfigDao.getDatastore().createQuery(SyncConfig.class);
        q.field("_id").equal(id);
        syncConfigDao.deleteByQuery(q);
        Query<SyncXml> q2 = syncXmlDao.getDatastore().createQuery(SyncXml.class);
        q2.field("_id").equal(id);
        syncXmlDao.deleteByQuery(q2);

    }

    @Override
    public ObjectId saveSyncTask(SyncTask syncTask) {
        Key<SyncTask> key = syncTaskDao.save(syncTask);
        return (ObjectId) key.getId();
    }
}
