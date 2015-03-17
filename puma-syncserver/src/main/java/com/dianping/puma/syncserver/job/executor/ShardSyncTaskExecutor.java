package com.dianping.puma.syncserver.job.executor;

import com.dianping.lion.EnvZooKeeperConfig;
import com.dianping.lion.client.ConfigCache;
import com.dianping.lion.client.LionException;
import com.dianping.puma.core.sync.model.task.ShardSyncTask;
import com.dianping.puma.core.sync.model.taskexecutor.TaskExecutorStatus;
import com.dianping.zebra.config.LionKey;
import com.dianping.zebra.group.jdbc.GroupDataSource;
import com.dianping.zebra.group.router.RouterType;
import com.dianping.zebra.shard.config.RouterRuleConfig;
import com.dianping.zebra.shard.config.TableShardDimensionConfig;
import com.dianping.zebra.shard.config.TableShardRuleConfig;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.gson.Gson;

import javax.sql.DataSource;
import java.util.Map;

/**
 * Dozer @ 2015-02
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public class ShardSyncTaskExecutor implements TaskExecutor<ShardSyncTask> {
    private final ShardSyncTask task;

    private ConfigCache configService;

    private Map<String, DataSource> dataSourcePool;

    private TableShardRuleConfig tableShardRuleConfig;

    private String originGroupDataSource;

    private volatile boolean switchOn;

    public ShardSyncTaskExecutor(ShardSyncTask task) {
        Preconditions.checkNotNull(task, "task");
        Preconditions.checkNotNull(task.getRuleName(), "task.ruleName");
        Preconditions.checkNotNull(task.getTableName(), "task.tableName");
        this.task = task;

        try {
            this.configService = ConfigCache.getInstance(EnvZooKeeperConfig.getZKAddress());
        } catch (LionException e) {
            throw new RuntimeException("Lion Init Failed");
        }
    }

    public void init() throws LionException {
        Preconditions.checkNotNull(configService, "configService");
        initAndConvertConfig();
        initDs();
        initPumaClient();
        //todo:init pumaclient
        //todo:init
    }

    protected void initPumaClient(){
        if(!switchOn && !Strings.isNullOrEmpty(originGroupDataSource)){
            //初始化迁移程序
        }

        for (TableShardDimensionConfig dimensionConfig : tableShardRuleConfig.getDimensionConfigs()) {
            if (dimensionConfig.isMaster()) {
                //初始化主维度
            }
        }
    }

    protected void initDs() {
        //没有启用切换，说明需要同步老库，那么需要初始化主维度
        boolean needMaster = !switchOn && !Strings.isNullOrEmpty(originGroupDataSource);

        for (TableShardDimensionConfig dimensionConfig : tableShardRuleConfig.getDimensionConfigs()) {
            if (dimensionConfig.isMaster() && !needMaster) {
                continue;
            }

            String[] dbs = dimensionConfig.getDbIndexes().split(",");
            for (String jdbcRef : dbs) {
                initGroupDataSource(jdbcRef);
            }
        }

        if(needMaster) {
            initGroupDataSource(originGroupDataSource);
        }
    }

    private void initGroupDataSource(String jdbcRef) {
        GroupDataSource ds = new GroupDataSource(jdbcRef);
        ds.setRouterType(RouterType.FAIL_OVER.getRouterType());
        ds.init();
        dataSourcePool.put(jdbcRef, ds);
    }

    protected void initAndConvertConfig() throws LionException {
        RouterRuleConfig tempRouterRuleConfig = new Gson().fromJson(configService.getProperty(LionKey.getShardConfigKey(task.getRuleName())), RouterRuleConfig.class);
        this.originGroupDataSource = configService.getProperty(LionKey.getShardOriginDatasourceKey(task.getRuleName()));
        String switchOnStr = configService.getProperty(LionKey.getShardSiwtchOnKey(task.getRuleName()));
        this.switchOn = switchOnStr == null || "true".equals(switchOnStr);
        findTableRuleConfig(tempRouterRuleConfig);
    }

    protected void findTableRuleConfig(RouterRuleConfig tempRouterRuleConfig) {
        for (TableShardRuleConfig tableConfig : tempRouterRuleConfig.getTableShardConfigs()) {
            if (task.getTableName().equals(tableConfig.getTableName())) {
                this.tableShardRuleConfig = tableConfig;
                return;
            }
        }
        Preconditions.checkNotNull(this.tableShardRuleConfig, "tableShardRuleConfig");
    }

    @Override
    public void start() {

    }

    @Override
    public void pause(String detail) {

    }

    @Override
    public void succeed() {

    }

    @Override
    public TaskExecutorStatus getTaskExecutorStatus() {
        return null;
    }

    @Override
    public ShardSyncTask getTask() {
        return task;
    }

    @Override
    public void stop(String detail) {

    }

    public void setConfigService(ConfigCache configService) {
        this.configService = configService;
    }
}
