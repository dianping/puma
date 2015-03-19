package com.dianping.puma.syncserver.job.executor;

import com.dianping.lion.EnvZooKeeperConfig;
import com.dianping.lion.client.ConfigCache;
import com.dianping.lion.client.LionException;
import com.dianping.puma.api.ConfigurationBuilder;
import com.dianping.puma.api.EventListener;
import com.dianping.puma.api.PumaClient;
import com.dianping.puma.core.constant.SubscribeConstant;
import com.dianping.puma.core.entity.PumaServer;
import com.dianping.puma.core.entity.PumaTask;
import com.dianping.puma.core.entity.SrcDBInstance;
import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.event.RowChangedEvent;
import com.dianping.puma.core.service.PumaServerService;
import com.dianping.puma.core.service.PumaTaskService;
import com.dianping.puma.core.service.SrcDBInstanceService;
import com.dianping.puma.core.sync.model.task.ShardSyncTask;
import com.dianping.puma.core.sync.model.taskexecutor.TaskExecutorStatus;
import com.dianping.puma.syncserver.mysql.SqlBuildUtil;
import com.dianping.zebra.config.LionKey;
import com.dianping.zebra.group.config.datasource.entity.DataSourceConfig;
import com.dianping.zebra.group.config.datasource.entity.GroupDataSourceConfig;
import com.dianping.zebra.group.jdbc.GroupDataSource;
import com.dianping.zebra.group.router.RouterType;
import com.dianping.zebra.shard.config.RouterRuleConfig;
import com.dianping.zebra.shard.config.TableShardDimensionConfig;
import com.dianping.zebra.shard.config.TableShardRuleConfig;
import com.dianping.zebra.shard.router.DataSourceRouter;
import com.dianping.zebra.shard.router.DataSourceRouterImpl;
import com.dianping.zebra.shard.router.RouterTarget;
import com.dianping.zebra.shard.router.TargetedSql;
import com.dianping.zebra.shard.router.rule.*;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
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
    public static final int INSERT = RowChangedEvent.INSERT;
    public static final int DELETE = RowChangedEvent.DELETE;
    public static final int UPDATE = RowChangedEvent.UPDATE;
    public static final int REPLACE_INTO = 3;
    private static final int UPDTAE_TO_NULL = 4;
    public static final int SELECT = 5;

    private final ShardSyncTask task;

    private ConfigCache configService;

    private final Map<String, DataSource> dataSourcePool = new ConcurrentHashMap<String, DataSource>();

    private List<PumaClient> pumaClientList = new ArrayList<PumaClient>();

    protected Processer processer = new Processer();

    protected TableShardRuleConfig tableShardRuleConfig;

    protected String originGroupDataSource;

    protected boolean switchOn;

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
    }

    protected void startPumaClient() {
        for (PumaClient client : pumaClientList) {
            client.register(processer);
            client.start();
        }
    }

    class Processer implements EventListener {
        @Override
        public void onEvent(ChangedEvent event) throws Exception {
            if (!(event instanceof RowChangedEvent)) {
                return;
            }

            RowChangedEvent rowEvent = (RowChangedEvent) event;

            rowEvent.setTable(task.getTableName());

            String tempSql = rowChangedEventToSql(rowEvent);
            List<Object> args = rowChangedEventToArgs(rowEvent);

            if (Strings.isNullOrEmpty(tempSql)) {
                return;
            }

            RouterTarget routerTarget = router.getTarget(tempSql, args);

            for (TargetedSql targetedSql : routerTarget.getTargetedSqls()) {
                JdbcTemplate jdbcTemplate = new JdbcTemplate(targetedSql.getDataSource());
                for (String sql : targetedSql.getSqls()) {
                    try {
                        jdbcTemplate.execute(sql);
                    } catch (Exception exp) {
                        //todo:
                    }
                }
            }
        }

        protected List<Object> rowChangedEventToArgs(RowChangedEvent event) {
            int actionType = event.getActionType();
            Map<String, RowChangedEvent.ColumnInfo> columnMap = event.getColumns();
            List<Object> args = new ArrayList<Object>();
            switch (actionType) {
                case REPLACE_INTO:
                    for (Map.Entry<String, RowChangedEvent.ColumnInfo> columnName2ColumnInfo : columnMap.entrySet()) {
                        args.add(columnName2ColumnInfo.getValue().getNewValue());
                    }
                    for (Map.Entry<String, RowChangedEvent.ColumnInfo> columnName2ColumnInfo : columnMap.entrySet()) {
                        if (!columnName2ColumnInfo.getValue().isKey()) {
                            args.add(columnName2ColumnInfo.getValue().getNewValue());
                        }
                    }
                    break;
                case INSERT:
                    for (Map.Entry<String, RowChangedEvent.ColumnInfo> columnName2ColumnInfo : columnMap.entrySet()) {
                        args.add(columnName2ColumnInfo.getValue().getNewValue());
                    }
                    break;
                case UPDATE:
                    for (Map.Entry<String, RowChangedEvent.ColumnInfo> columnName2ColumnInfo : columnMap.entrySet()) {
                        args.add(columnName2ColumnInfo.getValue().getNewValue());
                    }
                    for (Map.Entry<String, RowChangedEvent.ColumnInfo> columnName2ColumnInfo : columnMap.entrySet()) {
                        args.add(columnName2ColumnInfo.getValue().getOldValue());
                    }
                    break;
                case DELETE:
                    for (Map.Entry<String, RowChangedEvent.ColumnInfo> columnName2ColumnInfo : columnMap.entrySet()) {
                        args.add(columnName2ColumnInfo.getValue().getOldValue());
                    }
                    break;
                case UPDTAE_TO_NULL:
                    for (Map.Entry<String, RowChangedEvent.ColumnInfo> columnName2ColumnInfo : columnMap.entrySet()) {
                        if (!columnName2ColumnInfo.getValue().isKey()) { //primery key 不能update to null
                            args.add(columnName2ColumnInfo.getValue().getNewValue());
                        }
                    }
                    for (Map.Entry<String, RowChangedEvent.ColumnInfo> columnName2ColumnInfo : columnMap.entrySet()) {
                        args.add(columnName2ColumnInfo.getValue().getOldValue());
                    }
                    break;
                case SELECT:
                    //ignore
                    break;
            }
            return args;
        }

        protected String rowChangedEventToSql(RowChangedEvent event) {
            String sql = null;
            int actionType = event.getActionType();
            switch (actionType) {
                case INSERT:
                    sql = SqlBuildUtil.buildInsertSql(event);
                    break;
                case UPDATE:
                    sql = SqlBuildUtil.buildUpdateSql(event);
                    break;
                case DELETE:
                    sql = SqlBuildUtil.buildDeleteSql(event);
                    break;
                case UPDTAE_TO_NULL:
                    sql = SqlBuildUtil.buildUpdateToNullSql(event);
                    break;
                case REPLACE_INTO:
                    sql = SqlBuildUtil.buildReplaceSql(event);
                    break;
                case SELECT:
                    //ignore
                    break;
            }
            return sql;
        }

        @Override
        public boolean onException(ChangedEvent event, Exception e) {
            return false;
        }

        @Override
        public void onConnectException(Exception e) {

        }

        @Override
        public void onConnected() {

        }

        @Override
        public void onSkipEvent(ChangedEvent event) {

        }
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
        this.router.init();
    }

    protected void initPumaClientsAndDataSources() {
        if (!switchOn && !Strings.isNullOrEmpty(originGroupDataSource)) {
            GroupDataSource ds = initGroupDataSource(originGroupDataSource);
            initPumaClient(ds.getConfig(), SubscribeConstant.SEQ_FROM_LATEST, Sets.newHashSet(task.getTableName()), "migrate");
        }

        TableShardRule tableShardRule = routerRule.getTableShardRules().get(task.getTableName());
        for (DimensionRule dimensionRule : tableShardRule.getDimensionRules()) {
            DimensionRuleImpl dimensionRuleImpl = (DimensionRuleImpl) dimensionRule;
            if (dimensionRuleImpl == null || !dimensionRuleImpl.isMaster()) {
                continue;
            }

            initPumaClientsAndDataSources(dimensionRuleImpl.getDataSourceProvider().getAllDBAndTables(), "master");

            int index = 0;
            for (DimensionRule rule : dimensionRuleImpl.getWhiteListRules()) {
                initPumaClientsAndDataSources(rule.getAllDBAndTables(), "white" + String.valueOf(index++));
            }
        }
    }

    protected void initPumaClientsAndDataSources(Map<String, Set<String>> all, String name) {
        for (Map.Entry<String, Set<String>> entity : all.entrySet()) {
            if (dataSourcePool.containsKey(entity.getKey())) {
                continue;
            }
            GroupDataSource groupDataSource = initGroupDataSource(entity.getKey());
            initPumaClient(groupDataSource.getConfig(), SubscribeConstant.SEQ_FROM_LATEST, entity.getValue(), name);
        }
    }

    protected PumaClient initPumaClient(GroupDataSourceConfig config, long seq, Set<String> tables, String name) {
        DataSourceConfig dsConfig = findTheOnlyWriteDataSourceConfig(config);

        Matcher matcher = JDBC_URL_PATTERN.matcher(dsConfig.getJdbcUrl());
        checkArgument(matcher.matches(), dsConfig.getJdbcUrl());

        String ip = matcher.group(1);
        String ds = matcher.group(2);

        List<SrcDBInstance> dbs = srcDBInstanceService.findByIp(ip);
        checkArgument(dbs.size() >= 1, ip);
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

        configBuilder.name(String.format("ShardSyncTask-%s-%s-%s", task.getId(), ds, name));

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

    protected DataSourceConfig findTheOnlyWriteDataSourceConfig(GroupDataSourceConfig config) {
        checkNotNull(config, "ds");
        List<DataSourceConfig> dataSourceConfigs = FluentIterable.from(config.getDataSourceConfigs().values()).filter(new Predicate<DataSourceConfig>() {
            @Override
            public boolean apply(DataSourceConfig dataSourceConfig) {
                return dataSourceConfig.isCanWrite();
            }
        }).toList();

        checkArgument(dataSourceConfigs.size() == 1, config.toString());
        return dataSourceConfigs.get(0);
    }

    protected GroupDataSource initGroupDataSource(String jdbcRef) {
        if (dataSourcePool.containsKey(jdbcRef)) {
            return (GroupDataSource) dataSourcePool.get(dataSourcePool);
        }
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
                for (TableShardDimensionConfig dimension : this.tableShardRuleConfig.getDimensionConfigs()) {
                    dimension.setTableName(task.getTableName());
                }
                return;
            }
        }
        checkNotNull(this.tableShardRuleConfig, "tableShardRuleConfig");
    }

    @Override
    public void start() {
        startPumaClient();
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
