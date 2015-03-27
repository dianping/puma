package com.dianping.puma.syncserver.job.executor;

import com.dianping.cat.Cat;
import com.dianping.lion.EnvZooKeeperConfig;
import com.dianping.lion.client.ConfigCache;
import com.dianping.lion.client.LionException;
import com.dianping.puma.api.ConfigurationBuilder;
import com.dianping.puma.api.EventListener;
import com.dianping.puma.api.PumaClient;
import com.dianping.puma.core.constant.Status;
import com.dianping.puma.core.constant.SubscribeConstant;
import com.dianping.puma.core.entity.*;
import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.event.RowChangedEvent;
import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.core.model.state.ShardSyncTaskState;
import com.dianping.puma.core.service.PumaServerService;
import com.dianping.puma.core.service.PumaTaskService;
import com.dianping.puma.core.service.SrcDBInstanceService;
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
import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.*;
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
public class ShardSyncTaskExecutor implements TaskExecutor<BaseSyncTask, ShardSyncTaskState> {
    private static final Logger logger = LoggerFactory.getLogger(ShardSyncTaskExecutor.class);

    private final ShardSyncTask task;

    protected ShardSyncTaskState status;

    private ConfigCache configCache;

    private final Map<String, DataSource> dataSourcePool = new ConcurrentHashMap<String, DataSource>();

    private List<PumaClient> pumaClientList = new ArrayList<PumaClient>();

    protected TableShardRuleConfig tableShardRuleConfigOrigin;

    protected TableShardRuleConfig tableShardRuleConfigForRouting;

    protected String originGroupDataSource;

    protected boolean switchOn;

    private PumaServerService pumaServerService;

    private PumaTaskService pumaTaskService;

    private SrcDBInstanceService srcDBInstanceService;

    protected RouterRule routerRuleOrigin;

    protected RouterRule routerRuleForRouting;

    protected DataSourceRouter routerForMigrate;

    protected DataSourceRouter routerForRouting;

    private static final Pattern JDBC_URL_PATTERN = Pattern.compile("jdbc:mysql://([^:]+):\\d+/([^\\?]+).*");

    public ShardSyncTaskExecutor(ShardSyncTask task) {
        checkNotNull(task, "task");
        checkNotNull(task.getRuleName(), "task.ruleName");
        checkNotNull(task.getTableName(), "task.tableName");
        this.task = task;

        this.status = new ShardSyncTaskState();
        this.status.setStatus(Status.INITIALIZING);

        try {
            this.configCache = ConfigCache.getInstance(EnvZooKeeperConfig.getZKAddress());
        } catch (LionException e) {
            throw new RuntimeException("Lion Init Failed");
        }
    }

    public void init() {
        checkNotNull(configCache, "configCache");
        initAndConvertConfig();
        initRouterConfig();
        initPumaClientsAndDataSources();
        initRouter();
    }

    class Processor implements EventListener {
        protected volatile int tryTimes = 0;

        protected final int MAX_TRY_TIMES = 2;

        private final String name;

        public Processor(String name) {
            this.name = name;
        }

        @Override
        public void onEvent(ChangedEvent event) throws Exception {
            tryTimes++;
            onEventInternal(event);
            status.setBinlogInfo(new BinlogInfo(event.getBinlog(), event.getBinlogPos()));
            tryTimes = 0;
        }

        protected void onEventInternal(ChangedEvent event) {
            if (!(event instanceof RowChangedEvent)) {
                return;
            }
            RowChangedEvent rowEvent = (RowChangedEvent) event;

            DataSourceRouter targetRouter = task.getTableName().equals(rowEvent.getTable()) ? routerForMigrate : routerForRouting;

            rowEvent.setTable(task.getTableName());
            rowEvent.setDatabase("");

            String tempSql = rowChangedEventToSql(rowEvent);
            List<Object> args = rowChangedEventToArgs(rowEvent);

            if (Strings.isNullOrEmpty(tempSql)) {
                return;
            }

            RouterTarget routerTarget = targetRouter.getTarget(tempSql, args);

            for (TargetedSql targetedSql : routerTarget.getTargetedSqls()) {
                JdbcTemplate jdbcTemplate = new JdbcTemplate(targetedSql.getDataSource());
                for (String sql : targetedSql.getSqls()) {
                    int rows = jdbcTemplate.update(sql, args.toArray());
                    if (rows != 1) {
                        throw new EmptyResultDataAccessException("error effective dated row:" + rows, 1);
                    }
                }
            }
        }

        protected List<Object> rowChangedEventToArgs(RowChangedEvent event) {
            int actionType = event.getActionType();
            Map<String, RowChangedEvent.ColumnInfo> columnMap = event.getColumns();
            List<Object> args = new ArrayList<Object>();
            switch (actionType) {
                case RowChangedEvent.INSERT:
                    for (Map.Entry<String, RowChangedEvent.ColumnInfo> columnName2ColumnInfo : columnMap.entrySet()) {
                        args.add(columnName2ColumnInfo.getValue().getNewValue());
                    }
                    break;
                case RowChangedEvent.UPDATE:
                    for (Map.Entry<String, RowChangedEvent.ColumnInfo> columnName2ColumnInfo : columnMap.entrySet()) {
                        args.add(columnName2ColumnInfo.getValue().getNewValue());
                    }
                    for (Map.Entry<String, RowChangedEvent.ColumnInfo> columnName2ColumnInfo : columnMap.entrySet()) {
                        if (columnName2ColumnInfo.getValue().getOldValue() != null) {
                            args.add(columnName2ColumnInfo.getValue().getOldValue());
                        }
                    }
                    break;
                case RowChangedEvent.DELETE:
                    for (Map.Entry<String, RowChangedEvent.ColumnInfo> columnName2ColumnInfo : columnMap.entrySet()) {
                        if (columnName2ColumnInfo.getValue().getOldValue() != null) {
                            args.add(columnName2ColumnInfo.getValue().getOldValue());
                        }
                    }
                    break;
            }
            return args;
        }

        protected String rowChangedEventToSql(RowChangedEvent event) {
            String sql = null;
            int actionType = event.getActionType();
            switch (actionType) {
                case RowChangedEvent.INSERT:
                    sql = SqlBuildUtil.buildSql(event, "/sql_template_shard/insertSql.vm");
                    break;
                case RowChangedEvent.UPDATE:
                    sql = SqlBuildUtil.buildSql(event, "/sql_template_shard/updateSql.vm");
                    break;
                case RowChangedEvent.DELETE:
                    sql = SqlBuildUtil.buildSql(event, "/sql_template_shard/deleteSql.vm");
                    break;
            }
            return sql;
        }

        @Override
        public boolean onException(ChangedEvent event, Exception e) {
            if (tryTimes >= MAX_TRY_TIMES) {
                logException(event, e);
                return true;
            }

            if (e instanceof DuplicateKeyException) {
                logException(event, e);
                return true;
            } else if (e instanceof EmptyResultDataAccessException) {
                logException(event, e);
                if (event instanceof RowChangedEvent) {
                    RowChangedEvent rowChangedEvent = (RowChangedEvent) event;
                    if (rowChangedEvent.getActionType() == RowChangedEvent.UPDATE) {
                        rowChangedEvent.setActionType(RowChangedEvent.INSERT);
                        return false;
                    }
                }
                return true;
            } else {
                logException(event, e);
                return false;
            }
        }

        public void logException(ChangedEvent event, Exception exp) {
            String msg = String.format("Name: %s Event: %s TryTimes: %s", this.name, event.toString(), tryTimes);
            Cat.logError(msg, exp);
            logger.error(msg, exp);
        }

        @Override
        public void onConnectException(Exception e) {
            Cat.logError("Client Connect Error:" + this.name, e);
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
        routerRuleConfig.setTableShardConfigs(Lists.newArrayList(tableShardRuleConfigOrigin));
        this.routerRuleOrigin = RouterRuleBuilder.build(routerRuleConfig);

        RouterRuleConfig routerRuleConfigForRouting = new RouterRuleConfig();
        routerRuleConfigForRouting.setTableShardConfigs(Lists.newArrayList(tableShardRuleConfigForRouting));
        this.routerRuleForRouting = RouterRuleBuilder.build(routerRuleConfigForRouting);
    }

    protected void initRouter() {
        DataSourceRouterImpl routerImplForRouting = new DataSourceRouterImpl();
        routerImplForRouting.setRouterRule(routerRuleForRouting);
        routerImplForRouting.setDataSourcePool(dataSourcePool);
        this.routerForRouting = routerImplForRouting;
        this.routerForRouting.init();

        DataSourceRouterImpl routerForMigrate = new DataSourceRouterImpl();
        routerForMigrate.setRouterRule(routerRuleOrigin);
        routerForMigrate.setDataSourcePool(dataSourcePool);
        this.routerForMigrate = routerForMigrate;
        this.routerForMigrate.init();
    }

    protected void initPumaClientsAndDataSources() {
        if (!switchOn && !Strings.isNullOrEmpty(originGroupDataSource)) {
            GroupDataSource ds = initGroupDataSource(originGroupDataSource);
            initPumaClient(ds.getConfig(), Sets.newHashSet(task.getTableName()), "migrate");
        }

        TableShardRule tableShardRule = routerRuleOrigin.getTableShardRules().get(task.getTableName());
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
            initPumaClient(groupDataSource.getConfig(), entity.getValue(), name);
        }
    }

    protected PumaClient initPumaClient(GroupDataSourceConfig config, Set<String> tables, String name) {
        DataSourceConfig dsConfig = findTheOnlyWriteDataSourceConfig(config);

        Matcher matcher = JDBC_URL_PATTERN.matcher(dsConfig.getJdbcUrl());
        checkArgument(matcher.matches(), dsConfig.getJdbcUrl());

        String ip = matcher.group(1);
        String ds = matcher.group(2);

        List<SrcDBInstance> dbs = srcDBInstanceService.findByIp(ip);
        checkArgument(dbs.size() >= 1, ip);
        SrcDBInstance db = dbs.get(0);

        List<PumaTask> pumaTasks = pumaTaskService.findBySrcDBInstanceName(db.getName());
        checkArgument(pumaTasks.size() >= 1, "no puma task for db %s", db.getName());
        PumaTask pumaTask = pumaTasks.get(0);

        PumaServer pumaServer = pumaServerService.find(pumaTask.getPumaServerName());
        checkNotNull(pumaServer, "puma server %s not exists", pumaTask.getPumaServerName());

        ConfigurationBuilder configBuilder = new ConfigurationBuilder();

        configBuilder.dml(true)
                .ddl(false)
                .transaction(false)
                .port(pumaServer.getPort())
                .host(pumaServer.getHost())
                .target(pumaTask.getName());

        String fullName = String.format("%s-%s-%s", task.getName(), ds, name);
        configBuilder.name(fullName);

        for (String tb : tables) {
            configBuilder.tables(ds, tb);
        }

        PumaClient client = new PumaClient(configBuilder.build());

        if (client.getSeqFileHolder().getSeq() == SubscribeConstant.SEQ_FROM_OLDEST) {
            client.getSeqFileHolder().saveSeq(SubscribeConstant.SEQ_FROM_LATEST);
        }

        client.register(new Processor(fullName));

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

    protected void initAndConvertConfig() {
        try {
            RouterRuleConfig tempRouterRuleConfig = new Gson().fromJson(configCache.getProperty(LionKey.getShardConfigKey(task.getRuleName())), RouterRuleConfig.class);
            this.originGroupDataSource = configCache.getProperty(LionKey.getShardOriginDatasourceKey(task.getRuleName()));
            String switchOnStr = configCache.getProperty(LionKey.getShardSiwtchOnKey(task.getRuleName()));
            this.switchOn = switchOnStr == null || "true".equals(switchOnStr);
            findTableRuleConfig(tempRouterRuleConfig);
            convertRuleConfigForRouting();
        } catch (LionException e) {
            throw new RuntimeException(e);
        }

    }

    protected void convertRuleConfigForRouting() {
        this.tableShardRuleConfigForRouting = SerializationUtils.clone(this.tableShardRuleConfigOrigin);
        Iterator<TableShardDimensionConfig> iterator = this.tableShardRuleConfigForRouting.getDimensionConfigs().iterator();
        while (iterator.hasNext() && iterator.next().isMaster()) {
            iterator.remove();
        }
        for (TableShardDimensionConfig config : this.tableShardRuleConfigForRouting.getDimensionConfigs()) {
            config.setMaster(true);
        }
    }

    protected void findTableRuleConfig(RouterRuleConfig tempRouterRuleConfig) {
        for (TableShardRuleConfig tableConfig : tempRouterRuleConfig.getTableShardConfigs()) {
            if (task.getTableName().equals(tableConfig.getTableName())) {
                this.tableShardRuleConfigOrigin = tableConfig;
                for (TableShardDimensionConfig dimension : this.tableShardRuleConfigOrigin.getDimensionConfigs()) {
                    dimension.setTableName(task.getTableName());
                }
                return;
            }
        }
        checkNotNull(this.tableShardRuleConfigOrigin, "tableShardRuleConfig");
    }

    @Override
    public void start() {
        for (PumaClient client : pumaClientList) {
            client.start();
        }
        this.status.setStatus(Status.RUNNING);
    }

    @Override
    public void pause(String detail) {
        for (PumaClient client : pumaClientList) {
            client.stop();
        }
        this.status.setStatus(Status.SUSPENDED);
    }

    @Override
    public void succeed() {

    }

    @Override
    public TaskExecutorStatus getTaskExecutorStatus() {
        return null;
    }

    @Override
    public BaseSyncTask getTask() {
        return this.task;
    }

    @Override
    public ShardSyncTaskState getTaskState() {
        return this.status;
    }

    @Override
    public void setTaskState(ShardSyncTaskState status) {
        this.status = status;
    }

    @Override
    public void stop(String detail) {
        for (PumaClient client : pumaClientList) {
            client.stop();
        }
        this.status.setStatus(Status.STOPPED);
    }

    public void setConfigCache(ConfigCache configCache) {
        this.configCache = configCache;
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
