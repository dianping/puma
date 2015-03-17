package com.dianping.puma.syncserver.job.executor;

import com.dianping.lion.EnvZooKeeperConfig;
import com.dianping.lion.client.ConfigCache;
import com.dianping.lion.client.LionException;
import com.dianping.puma.api.ConfigurationBuilder;
import com.dianping.puma.api.PumaClient;
import com.dianping.puma.core.constant.SubscribeConstant;
import com.dianping.puma.core.entity.PumaServer;
import com.dianping.puma.core.entity.PumaTask;
import com.dianping.puma.core.entity.SrcDBInstance;
import com.dianping.puma.core.service.PumaServerService;
import com.dianping.puma.core.service.PumaTaskService;
import com.dianping.puma.core.service.SrcDBInstanceService;
import com.dianping.puma.core.sync.model.task.ShardSyncTask;
import com.dianping.puma.core.sync.model.taskexecutor.TaskExecutorStatus;
import com.dianping.zebra.config.LionKey;
import com.dianping.zebra.group.config.datasource.entity.DataSourceConfig;
import com.dianping.zebra.group.config.datasource.entity.GroupDataSourceConfig;
import com.dianping.zebra.group.jdbc.GroupDataSource;
import com.dianping.zebra.group.router.RouterType;
import com.dianping.zebra.shard.config.RouterRuleConfig;
import com.dianping.zebra.shard.config.TableShardDimensionConfig;
import com.dianping.zebra.shard.config.TableShardRuleConfig;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Dozer @ 2015-02
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public class ShardSyncTaskExecutor implements TaskExecutor<ShardSyncTask> {
    private final ShardSyncTask task;

    private ConfigCache configService;

    private Map<String, GroupDataSource> dataSourcePool;

    private List<PumaClient> pumaClientList = new ArrayList<PumaClient>();

    private TableShardRuleConfig tableShardRuleConfig;

    private String originGroupDataSource;

    private volatile boolean switchOn;

    private PumaServerService pumaServerService;

    private PumaTaskService pumaTaskService;

    private SrcDBInstanceService srcDBInstanceService;


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
        initPumaClients();
        //todo:init pumaclient
        //todo:init
    }

    protected void initPumaClients() {
        if (!switchOn && !Strings.isNullOrEmpty(originGroupDataSource)) {
            Preconditions.checkNotNull(dataSourcePool.get(originGroupDataSource), "originGroupDataSource");
            initPumaClient(dataSourcePool.get(originGroupDataSource).getConfig(), SubscribeConstant.SEQ_FROM_LATEST, String.format("%s-%s-%s-New-to-Shard", task.getRuleName(), task.getTableName(), task.getId()));
            initPumaClient(dataSourcePool.get(originGroupDataSource).getConfig(), SubscribeConstant.SEQ_FROM_OLDEST, String.format("%s-%s-%s-Old-to-Shard", task.getRuleName(), task.getTableName(), task.getId()));
        }

        for (TableShardDimensionConfig dimensionConfig : tableShardRuleConfig.getDimensionConfigs()) {
            if (dimensionConfig.isMaster()) {
                String[] dbs = dimensionConfig.getDbIndexes().split(",");
                for (String jdbcRef : dbs) {
                    Preconditions.checkNotNull(dataSourcePool.get(jdbcRef), jdbcRef);
                    initPumaClient(dataSourcePool.get(jdbcRef).getConfig(), SubscribeConstant.SEQ_FROM_OLDEST, String.format("%s-%s-%s-Master-to-Other", task.getRuleName(), task.getTableName(), task.getId()));
                }
            }
        }
    }

    protected void initPumaClient(GroupDataSourceConfig config, long seq, String name) {
        Preconditions.checkNotNull(config, "ds");

        List<DataSourceConfig> dataSourceConfigs = FluentIterable.from(config.getDataSourceConfigs().values()).filter(new Predicate<DataSourceConfig>() {
            @Override
            public boolean apply(DataSourceConfig dataSourceConfig) {
                return dataSourceConfig.isCanWrite();
            }
        }).toList();

        Preconditions.checkArgument(dataSourceConfigs.size() == 1, config.toString());

        DataSourceConfig dsConfig = dataSourceConfigs.get(0);

        String ip = dsConfig.getJdbcUrl().replace("jdbc:mysql://", "");
        ip = ip.substring(0, ip.indexOf(":"));
        Preconditions.checkArgument(ip.matches("\\d+\\.\\d+\\.\\d+\\.\\d+"), dsConfig.getJdbcUrl());


        List<SrcDBInstance> dbs = srcDBInstanceService.findByIp(ip);
        Preconditions.checkArgument(dbs.size() == 1, ip);
        SrcDBInstance db = dbs.get(0);

        List<PumaTask> pumaTasks = pumaTaskService.findBySrcDBInstanceId(db.getId());
        Preconditions.checkArgument(pumaTasks.size() >= 1, "no puma task for db %s", db.getId());
        PumaTask task = pumaTasks.get(0);

        PumaServer pumaServer = pumaServerService.find(task.getPumaServerId());
        Preconditions.checkNotNull(pumaServer, "puma server %s not exists", task.getPumaServerId());

        ConfigurationBuilder configBuilder = new ConfigurationBuilder();
//        configBuilder.dml(true).ddl(false).transaction(false).tables()//todo:解析table
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

        if (needMaster) {
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

    public void setPumaServerService(PumaServerService pumaServerService) {
        this.pumaServerService = pumaServerService;
    }

    public void setPumaTaskService(PumaTaskService pumaTaskService) {
        this.pumaTaskService = pumaTaskService;
    }

    public void setSrcDBInstanceService(SrcDBInstanceService srcDBInstanceService) {
        this.srcDBInstanceService = srcDBInstanceService;
    }
}
