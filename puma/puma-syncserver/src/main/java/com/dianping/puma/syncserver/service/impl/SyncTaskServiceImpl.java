package com.dianping.puma.syncserver.service.impl;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dianping.puma.core.sync.dao.task.SyncTaskDao;
import com.dianping.puma.core.sync.model.BinlogInfo;
import com.dianping.puma.core.sync.model.config.MysqlHost;
import com.dianping.puma.core.sync.model.mapping.ColumnMapping;
import com.dianping.puma.core.sync.model.mapping.DatabaseMapping;
import com.dianping.puma.core.sync.model.mapping.DumpMapping;
import com.dianping.puma.core.sync.model.mapping.MysqlMapping;
import com.dianping.puma.core.sync.model.mapping.TableMapping;
import com.dianping.puma.core.sync.model.task.SyncTask;
import com.dianping.puma.core.sync.model.task.TaskState;
import com.dianping.puma.core.sync.model.task.TaskState.State;
import com.dianping.puma.syncserver.service.SyncTaskService;
import com.dianping.puma.syncserver.util.MysqlMetaInfoFetcher;
import com.google.code.morphia.Key;
import com.google.code.morphia.query.Query;
import com.google.code.morphia.query.QueryResults;
import com.google.code.morphia.query.UpdateOperations;

@Service
public class SyncTaskServiceImpl implements SyncTaskService {
    @Autowired
    SyncTaskDao syncTaskDao;

    @Override
    public Long create(SyncTask syncTask) {
        //验证
        if (this.existsBySrcAndDest(syncTask.getSrcMysqlName(), syncTask.getDestMysqlName())) {
            throw new IllegalArgumentException("创建失败，已有相同的配置存在。(srcMysqlName=" + syncTask.getSrcMysqlName() + ", destMysqlName="
                    + syncTask.getDestMysqlName() + ")");
        }
        //验证仅有一个databaseConfig
        if (syncTask.getMysqlMapping().getDatabases() == null || syncTask.getMysqlMapping().getDatabases().size() == 0
                || syncTask.getMysqlMapping().getDatabases().size() > 1) {
            throw new IllegalArgumentException("创建失败，<database>配置必须有且仅能有一个！");
        }
        //验证table
        if (syncTask.getMysqlMapping().getDatabases().get(0).getTables() == null
                || syncTask.getMysqlMapping().getDatabases().get(0).getTables().size() == 0) {
            throw new IllegalArgumentException("创建失败，<table>配置必须至少有一个！");
        }
        //创建SyncTasktaskState
        TaskState taskState = new TaskState();
        taskState.setState(State.PREPARABLE);
        taskState.setDetail(State.PREPARABLE.getDesc());
        Date curDate = new Date();
        taskState.setCreateTime(curDate);
        taskState.setLastUpdateTime(curDate);
        syncTask.setTaskState(taskState);
        //开始保存
        Key<SyncTask> key = this.syncTaskDao.save(syncTask);
        this.syncTaskDao.getDatastore().ensureIndexes();
        Long id = (Long) key.getId();

        return id;
    }

    @Override
    public boolean existsBySrcAndDest(String srcMysqlName, String destMysqlName) {
        Query<SyncTask> q = syncTaskDao.getDatastore().createQuery(SyncTask.class);
        q.field("srcMysqlName").equal(srcMysqlName);
        q.field("destMysqlName").equal(destMysqlName);
        return syncTaskDao.exists(q);
    }

    @Override
    public SyncTask find(Long id) {
        return this.syncTaskDao.getDatastore().getByKey(SyncTask.class, new Key<SyncTask>(SyncTask.class, id));
    }

    @Override
    public List<SyncTask> find(int offset, int limit) {
        Query<SyncTask> q = syncTaskDao.getDatastore().createQuery(SyncTask.class);
        q.offset(offset);
        q.limit(limit);
        QueryResults<SyncTask> result = syncTaskDao.find(q);
        return result.asList();
    }

    /**
     * 对比新旧sync，求出新增的database或table配置(table也属于database下，故返回的都是database)<br>
     * 同时做验证：只允许新增database或table配置
     * 
     * @throws CloneNotSupportedException
     */
    @Override
    public MysqlMapping compare(MysqlMapping oldMysqlMapping, MysqlMapping newMysqlMapping) throws CloneNotSupportedException {
        return oldMysqlMapping.compare(newMysqlMapping);
    }

    @Override
    public DumpMapping convertMysqlMappingToDumpMapping(MysqlHost mysqlHost, MysqlMapping mysqlMapping) throws SQLException {
        DumpMapping dumpMapping = new DumpMapping();
        //dumpDatabaseMappings 遍历SyncConfig的DatabaseMapping，支持db和table名称改变，字段名称不支持改变。
        List<DatabaseMapping> databaseMappings = mysqlMapping.getDatabases();
        List<DatabaseMapping> dumpDatabaseMappings = new ArrayList<DatabaseMapping>();
        dumpMapping.setDatabaseMappings(dumpDatabaseMappings);
        for (DatabaseMapping databaseMapping : databaseMappings) {
            String databaseConfigFrom = databaseMapping.getFrom();
            String databaseConfigTo = databaseMapping.getTo();
            List<TableMapping> dumpTableConfigs = new ArrayList<TableMapping>();
            //遍历table配置
            List<TableMapping> tableConfigs = databaseMapping.getTables();
            for (TableMapping tableConfig : tableConfigs) {
                String tableConfigFrom = tableConfig.getFrom();
                String tableConfigTo = tableConfig.getTo();
                //如果是from=*,to=*，则需要从数据库获取实际的表（排除已经列出的table配置）
                if (StringUtils.equals(tableConfigFrom, "*") && StringUtils.equals(tableConfigTo, "*")) {
                    //访问数据库，得到该数据库下的所有表名(*配置是在最后的，所以排除已经列出的table配置就是排除dumpTableConfigs)
                    MysqlMetaInfoFetcher mysqlExecutor = new MysqlMetaInfoFetcher(mysqlHost.getHost(), mysqlHost.getUsername(),
                            mysqlHost.getPassword());
                    List<String> tableNames;
                    try {
                        tableNames = mysqlExecutor.getTables(databaseConfigFrom);
                    } finally {
                        mysqlExecutor.close();
                    }
                    getRidOf(tableNames, dumpTableConfigs);
                    for (String tableName : tableNames) {
                        TableMapping dumpTableConfig = new TableMapping();
                        dumpTableConfig.setFrom(tableName);
                        dumpTableConfig.setTo(tableName);
                        dumpTableConfigs.add(dumpTableConfig);
                    }
                } else {//如果“table下的字段没有被重命名,partOf为false”，那么该table可以被dump
                    if (shouldDump(tableConfig)) {
                        TableMapping dumpTableConfig = new TableMapping();
                        dumpTableConfig.setFrom(tableConfig.getFrom());
                        dumpTableConfig.setTo(tableConfig.getTo());
                        dumpTableConfigs.add(dumpTableConfig);
                    }
                }
            }
            //database需要dump(如果下属table没有需要dump则该database也不需要)
            if (dumpTableConfigs.size() > 0) {
                DatabaseMapping dumpDatabaseMapping = new DatabaseMapping();
                dumpDatabaseMapping.setFrom(databaseConfigFrom);
                dumpDatabaseMapping.setTo(databaseConfigTo);
                dumpDatabaseMapping.setTables(dumpTableConfigs);
                dumpDatabaseMappings.add(dumpDatabaseMapping);
            }
        }

        return dumpMapping;
    }

    /**
     * 从tableNames中去掉已经存在dumpTableConfigs(以TableConfig.getFrom()判断)中的表名
     */
    private void getRidOf(List<String> tableNames, List<TableMapping> dumpTableConfigs) {
        Collection<String> dumpTableNames = new ArrayList<String>();
        for (TableMapping tableConfig : dumpTableConfigs) {
            dumpTableNames.add(tableConfig.getFrom());
        }
        tableNames.removeAll(dumpTableNames);
    }

    /**
     * 如果“table下的字段没有被重命名,partOf为false”，那么该table可以被dump
     */
    private boolean shouldDump(TableMapping tableConfig) {
        if (tableConfig.isPartOf()) {
            return false;
        }
        List<ColumnMapping> columnConfigs = tableConfig.getColumns();
        for (ColumnMapping columnConfig : columnConfigs) {
            if (!StringUtils.equalsIgnoreCase(columnConfig.getFrom(), columnConfig.getTo())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void updateState(Long id, State state, Map<String, String> params) {
        UpdateOperations<SyncTask> ops = this.syncTaskDao.getDatastore().createUpdateOperations(SyncTask.class)
                .set("taskState.state", state);
        if (params != null) {
            ops.set("taskState.params", params);
        }
        ops.set("taskState.detail", state.getDesc());
        ops.set("taskState.lastUpdateTime", new Date());
        this.syncTaskDao.getDatastore().update(new Key<SyncTask>(SyncTask.class, id), ops);
    }

    @Override
    public void modify(Long id, BinlogInfo binlogInfo, MysqlMapping newMysqlMapping) {
        UpdateOperations<SyncTask> ops = this.syncTaskDao.getDatastore().createUpdateOperations(SyncTask.class)
                .set("binlogInfo", binlogInfo);
        ops.set("mysqlMapping", newMysqlMapping);
        this.syncTaskDao.getDatastore().update(new Key<SyncTask>(SyncTask.class, id), ops);
    }

}
