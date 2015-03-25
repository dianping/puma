package com.dianping.puma.syncserver.job.executor;

import com.dianping.lion.EnvZooKeeperConfig;
import com.dianping.lion.client.ConfigCache;
import com.dianping.lion.client.LionException;
import com.dianping.puma.core.entity.ShardDumpTask;
import com.dianping.puma.core.sync.model.taskexecutor.TaskExecutorStatus;
import com.dianping.zebra.config.ConfigService;
import com.dianping.zebra.config.LionKey;
import com.dianping.zebra.group.config.DefaultDataSourceConfigManager;
import com.dianping.zebra.group.config.datasource.entity.DataSourceConfig;
import com.dianping.zebra.group.config.datasource.entity.GroupDataSourceConfig;
import com.dianping.zebra.shard.config.RouterRuleConfig;
import com.dianping.zebra.shard.config.TableShardDimensionConfig;
import com.dianping.zebra.shard.config.TableShardRuleConfig;
import com.dianping.zebra.shard.router.DataSourceRouter;
import com.dianping.zebra.shard.router.DataSourceRouterImpl;
import com.dianping.zebra.shard.router.rule.*;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import sun.plugin.dom.exception.InvalidAccessException;

import javax.sql.DataSource;
import java.beans.PropertyChangeListener;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Dozer @ 2015-02
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public class ShardDumpTaskExecutor implements TaskExecutor<ShardDumpTask> {
    protected final ShardDumpTask task;

    protected final TaskExecutorStatus status;

    protected boolean switchOn;

    protected ConfigCache configCache;

    protected ConfigService configService;

    protected String originGroupDataSource;

    protected TableShardRuleConfig tableShardRuleConfigOrigin;

    protected RouterRule routerRule;

    protected DataSourceConfig originDataSourceConfig;

    protected DataSourceRouter router;

    protected Map<String, DataSourceConfig> targetDataSourceConfigMap = new HashMap<String, DataSourceConfig>();

    protected Map<String, DataSource> dataSourcePool = new HashMap<String, DataSource>();

    public ShardDumpTaskExecutor(ShardDumpTask task) {
        checkNotNull(task, "task");
        checkNotNull(task.getRuleName(), "task.ruleName");
        checkNotNull(task.getTableName(), "task.tableName");
        this.task = task;

        this.status = new TaskExecutorStatus();

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
        initDataSourceConfig();
        initRouter();
    }

    protected void initRouter() {
        DataSourceRouterImpl routerImplForRouting = new DataSourceRouterImpl();
        routerImplForRouting.setRouterRule(routerRule);
        routerImplForRouting.setDataSourcePool(dataSourcePool);
        this.router = routerImplForRouting;
        this.router.init();
    }

    protected void initConfigService() {
        this.configService = new ConfigService() {
            @Override
            public void init() {

            }

            @Override
            public String getProperty(String key) {
                try {
                    return configCache.getProperty(key);
                } catch (LionException e) {
                    return null;
                }
            }

            @Override
            public void addPropertyChangeListener(PropertyChangeListener listener) {

            }
        };
    }

    protected DataSourceConfig findSingleMasterDataSourceConfig(GroupDataSourceConfig groupDataSourceConfig) {
        DataSourceConfig dataSourceConfig = null;
        for (DataSourceConfig config : groupDataSourceConfig.getDataSourceConfigs().values()) {
            if (config.isCanWrite()) {
                if (dataSourceConfig != null) {
                    throw new RuntimeException("two write ds in:" + groupDataSourceConfig.toString());
                }
                dataSourceConfig = config;
            }
        }
        checkNotNull(dataSourceConfig, "no write ds in:" + groupDataSourceConfig.toString());
        return dataSourceConfig;
    }

    protected void initDataSourceConfig() {
        if (!switchOn && !Strings.isNullOrEmpty(originGroupDataSource)) {
            DefaultDataSourceConfigManager configManager = new DefaultDataSourceConfigManager(this.originGroupDataSource, this.configService);
            this.originDataSourceConfig = findSingleMasterDataSourceConfig(configManager.getGroupDataSourceConfig());
        }

        TableShardRule tableShardRule = routerRule.getTableShardRules().get(task.getTableName());
        for (DimensionRule dimensionRule : tableShardRule.getDimensionRules()) {
            DimensionRuleImpl dimensionRuleImpl = (DimensionRuleImpl) dimensionRule;
            if (dimensionRuleImpl == null || !dimensionRuleImpl.isMaster()) {
                continue;
            }

            initDataSourceConfig(dimensionRuleImpl.getDataSourceProvider().getAllDBAndTables());

            for (DimensionRule rule : dimensionRuleImpl.getWhiteListRules()) {
                initDataSourceConfig(rule.getAllDBAndTables());
            }
        }
    }

    protected void initDataSourceConfig(Map<String, Set<String>> allDbAndTables) {
        for (Map.Entry<String, Set<String>> entity : allDbAndTables.entrySet()) {
            if (targetDataSourceConfigMap.containsKey(entity.getKey())) {
                continue;
            }
            DefaultDataSourceConfigManager configManager = new DefaultDataSourceConfigManager(entity.getKey(), this.configService);
            DataSourceConfig config = findSingleMasterDataSourceConfig(configManager.getGroupDataSourceConfig());
            this.targetDataSourceConfigMap.put(entity.getKey(), config);
            initDataSourcePool(entity.getKey(), config);
        }
    }

    protected void initDataSourcePool(String key, DataSourceConfig config) {
        dataSourcePool.put(key, new DataSourceWrap(config));
    }

    protected void initRouterConfig() {
        RouterRuleConfig routerRuleConfig = new RouterRuleConfig();
        routerRuleConfig.setTableShardConfigs(Lists.newArrayList(tableShardRuleConfigOrigin));
        this.routerRule = RouterRuleBuilder.build(routerRuleConfig);
    }

    protected void initAndConvertConfig() {
        try {
            RouterRuleConfig tempRouterRuleConfig = new Gson().fromJson(configCache.getProperty(LionKey.getShardConfigKey(task.getRuleName())), RouterRuleConfig.class);
            this.originGroupDataSource = configCache.getProperty(LionKey.getShardOriginDatasourceKey(task.getRuleName()));
            String switchOnStr = configCache.getProperty(LionKey.getShardSiwtchOnKey(task.getRuleName()));
            this.switchOn = switchOnStr == null || "true".equals(switchOnStr);
            findTableRuleConfig(tempRouterRuleConfig);
            removeNotMasterDimension();
        } catch (LionException e) {
            throw new RuntimeException(e);
        }
    }

    protected void removeNotMasterDimension() {
        Iterator<TableShardDimensionConfig> iterator = tableShardRuleConfigOrigin.getDimensionConfigs().iterator();
        while (iterator.hasNext()) {
            if (!iterator.next().isMaster()) {
                iterator.remove();
            }
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
        checkNotNull(this.tableShardRuleConfigOrigin, "tableShardRuleConfigOrigin");
    }

    static class DataSourceWrap implements DataSource {

        private final DataSourceConfig config;

        public DataSourceWrap(DataSourceConfig config) {
            this.config = config;
        }

        public DataSourceConfig getConfig() {
            return config;
        }

        @Override
        public Connection getConnection() throws SQLException {
            throw new InvalidAccessException("datasource");
        }

        @Override
        public Connection getConnection(String username, String password) throws SQLException {
            throw new InvalidAccessException("datasource");
        }

        @Override
        public PrintWriter getLogWriter() throws SQLException {
            throw new InvalidAccessException("datasource");
        }

        @Override
        public void setLogWriter(PrintWriter out) throws SQLException {
            throw new InvalidAccessException("datasource");
        }

        @Override
        public void setLoginTimeout(int seconds) throws SQLException {
            throw new InvalidAccessException("datasource");
        }

        @Override
        public int getLoginTimeout() throws SQLException {
            throw new InvalidAccessException("datasource");
        }

        @Override
        public <T> T unwrap(Class<T> iface) throws SQLException {
            return (T) iface;
        }

        @Override
        public boolean isWrapperFor(Class<?> iface) throws SQLException {
            return iface.getClass().equals(iface);
        }
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
    public ShardDumpTask getTask() {
        return null;
    }

    @Override
    public void stop(String detail) {

    }

    public void setConfigCache(ConfigCache configCache) {
        this.configCache = configCache;
    }
}
