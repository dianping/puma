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
import com.dianping.zebra.shard.config.TableShardRuleConfig;
import com.dianping.zebra.shard.router.DataSourceRouter;
import com.dianping.zebra.shard.router.DataSourceRouterImpl;
import com.dianping.zebra.shard.router.rule.*;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.Gson;

import javax.sql.DataSource;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Dozer @ 2015-02
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public class ShardSyncTaskExecutor implements TaskExecutor<ShardSyncTask> {
    private final ShardSyncTask task;

    private ConfigCache configService;

    private Map<String, DataSource> dataSourcePool = new HashMap<String, DataSource>();

    private List<PumaClient> pumaClientList = new ArrayList<PumaClient>();

    protected TableShardRuleConfig tableShardRuleConfig;

    protected String originGroupDataSource;

    protected volatile boolean switchOn;

    private PumaServerService pumaServerService;

    private PumaTaskService pumaTaskService;

    private SrcDBInstanceService srcDBInstanceService;

    private DataSourceRouter router;

    protected RouterRule routerRule;

    private static final Pattern JDBC_URL_PATTERN = Pattern.compile("jdbc:mysql://([^:]+):\\d+/([^\\?]+).*");

    public ShardSyncTaskExecutor(ShardSyncTask task) {
        checkNotNull(task, "task");
        checkNotNull(task.getRuleName(), "task.ruleName");
        checkNotNull(task.getTableName(), "task.tableName");
        this.task = task;

        try {
            this.configService = ConfigCache.getInstance(EnvZooKeeperConfig.getZKAddress());
        } catch (LionException e) {
            throw new RuntimeException("Lion Init Failed");
        }
    }

    public void init() throws LionException {
        checkNotNull(configService, "configService");
        initAndConvertConfig();
        initRouterConfig();
        initPumaClientsAndDataSources();
        initRouter();

        //todo:start puma client
    }

    protected void initRouterConfig() {
        RouterRuleConfig routerRuleConfig = new RouterRuleConfig();
        routerRuleConfig.setTableShardConfigs(Lists.newArrayList(tableShardRuleConfig));
        this.routerRule = RouterRuleBuilder.build(routerRuleConfig);
    }

    protected void initRouter() {
        DataSourceRouterImpl routerImpl = new DataSourceRouterImpl();
        routerImpl.setRouterRule(routerRule);
        routerImpl.setDataSourcePool(dataSourcePool);
        this.router = routerImpl;
    }

    protected void initPumaClientsAndDataSources() {
        if (!switchOn && !Strings.isNullOrEmpty(originGroupDataSource)) {
            GroupDataSource ds = initGroupDataSource(originGroupDataSource);
            initPumaClient(ds.getConfig(), SubscribeConstant.SEQ_FROM_LATEST, Sets.newHashSet(task.getTableName()));
            initPumaClient(ds.getConfig(), SubscribeConstant.SEQ_FROM_OLDEST, Sets.newHashSet(task.getTableName()));
        }

        TableShardRule tableShardRule = routerRule.getTableShardRules().get(task.getTableName());
        for (DimensionRule dimensionRule : tableShardRule.getDimensionRules()) {
            DimensionRuleImpl dimensionRuleImpl = (DimensionRuleImpl) dimensionRule;
            if (dimensionRuleImpl == null || !dimensionRuleImpl.isMaster()) {
                continue;
            }

            initPumaClientsAndDataSources(dimensionRuleImpl.getDataSourceProvider().getAllDBAndTables());
            for (DimensionRule rule : dimensionRuleImpl.getWhiteListRules()) {
                initPumaClientsAndDataSources(rule.getAllDBAndTables());
            }
        }
    }

    protected void initPumaClientsAndDataSources(Map<String, Set<String>> all) {
        for (Map.Entry<String, Set<String>> entity : all.entrySet()) {
            if (dataSourcePool.containsKey(entity.getKey())) {
                continue;
            }
            GroupDataSource groupDataSource = initGroupDataSource(entity.getKey());
            initPumaClient(groupDataSource.getConfig(), SubscribeConstant.SEQ_FROM_OLDEST, entity.getValue());
        }
    }

    protected PumaClient initPumaClient(GroupDataSourceConfig config, long seq, Set<String> tables) {
        checkNotNull(config, "ds");

        List<DataSourceConfig> dataSourceConfigs = FluentIterable.from(config.getDataSourceConfigs().values()).filter(new Predicate<DataSourceConfig>() {
            @Override
            public boolean apply(DataSourceConfig dataSourceConfig) {
                return dataSourceConfig.isCanWrite();
            }
        }).toList();

        checkArgument(dataSourceConfigs.size() == 1, config.toString());

        DataSourceConfig dsConfig = dataSourceConfigs.get(0);

        Matcher matcher = JDBC_URL_PATTERN.matcher(dsConfig.getJdbcUrl());
        checkArgument(matcher.matches(), dsConfig.getJdbcUrl());

        String ip = matcher.group(1);
        String ds = matcher.group(2);

        List<SrcDBInstance> dbs = srcDBInstanceService.findByIp(ip);
        checkArgument(dbs.size() == 1, ip);
        SrcDBInstance db = dbs.get(0);

        List<PumaTask> pumaTasks = pumaTaskService.findBySrcDBInstanceId(db.getId());
        checkArgument(pumaTasks.size() >= 1, "no puma task for db %s", db.getId());
        PumaTask pumaTask = pumaTasks.get(0);

        PumaServer pumaServer = pumaServerService.find(pumaTask.getPumaServerId());
        checkNotNull(pumaServer, "puma server %s not exists", pumaTask.getPumaServerId());

        ConfigurationBuilder configBuilder = new ConfigurationBuilder();

        configBuilder.dml(true)
                .ddl(false)
                .transaction(false)
                .port(pumaServer.getPort())
                .host(pumaServer.getHost())
                .target(pumaTask.getName());

        configBuilder.name(String.format("ShardSyncTask-%s-%s(seq:%d)", task.getId(), ds, seq));

        for (String tb : tables) {
            configBuilder.tables(ds, tb);
        }

        PumaClient client = new PumaClient(configBuilder.build());

        //如果 seq 是默认值，那说明还没初始化过，如果不是默认值，以前跑过了。
        if (client.getSeqFileHolder().getSeq() < 0l) {
            client.getSeqFileHolder().saveSeq(seq);
        }

        pumaClientList.add(client);
        return client;
    }

    private GroupDataSource initGroupDataSource(String jdbcRef) {
        GroupDataSource ds = new GroupDataSource(jdbcRef);
        ds.setRouterType(RouterType.FAIL_OVER.getRouterType());
        ds.init();
        dataSourcePool.put(jdbcRef, ds);
        return ds;
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
        checkNotNull(this.tableShardRuleConfig, "tableShardRuleConfig");
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

    public void setConfigCache(ConfigCache configCache) {
        this.configService = configCache;
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
