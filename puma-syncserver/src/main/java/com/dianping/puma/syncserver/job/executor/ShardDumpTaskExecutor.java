package com.dianping.puma.syncserver.job.executor;

import com.dianping.lion.EnvZooKeeperConfig;
import com.dianping.lion.client.ConfigCache;
import com.dianping.lion.client.LionException;
import com.dianping.puma.core.entity.ShardDumpTask;
import com.dianping.puma.core.model.state.TaskState;
import com.dianping.puma.core.sync.model.taskexecutor.TaskExecutorStatus;
import com.dianping.puma.syncserver.config.SyncServerConfig;
import com.dianping.puma.syncserver.util.ProcessBuilderWrapper;
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
import com.dianping.zebra.shard.router.RouterTarget;
import com.dianping.zebra.shard.router.TargetedSql;
import com.dianping.zebra.shard.router.rule.*;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.sql.Connection;
import java.sql.SQLException;
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
public class ShardDumpTaskExecutor implements TaskExecutor<ShardDumpTask, TaskState> {
    private static final Logger logger = LoggerFactory.getLogger(ShardDumpTaskExecutor.class);

    private static final Pattern JDBC_URL_PATTERN = Pattern.compile("jdbc:mysql://([^:]+):(\\d+)/([^\\?]+).*");

    protected final ShardDumpTask task;

    protected final TaskExecutorStatus status;

    protected final String dumpOutputDir;

    protected final String uuid;

    protected boolean switchOn;

    protected ConfigCache configCache;

    protected ConfigService configService;

    protected String originGroupDataSource;

    protected TableShardRuleConfig tableShardRuleConfig;

    protected RouterRule routerRule;

    protected DataSourceConfig originDataSourceConfig;

    protected DataSourceRouter router;

    protected Map<String, DataSourceConfig> targetDataSourceConfigMap = new HashMap<String, DataSourceConfig>();

    protected Map<String, BufferedWriter> bufferedWriterMap = new HashMap<String, BufferedWriter>();

    protected Map<String, DataSource> dataSourceMap = new HashMap<String, DataSource>();

    public ShardDumpTaskExecutor(ShardDumpTask task) {
        checkNotNull(task, "task");
        checkNotNull(task.getRuleName(), "task.ruleName");
        checkNotNull(task.getTableName(), "task.tableName");
        this.task = task;
        this.uuid = UUID.randomUUID().toString();
        this.dumpOutputDir = SyncServerConfig.getInstance() == null ? "/tmp/" : SyncServerConfig.getInstance().getTempDir() + "/dump/" + uuid + "/";
        this.status = new TaskExecutorStatus();

        try {
            this.configCache = ConfigCache.getInstance(EnvZooKeeperConfig.getZKAddress());
        } catch (LionException e) {
            throw new RuntimeException("Lion Init Failed");
        }
    }

    public void init() {
        checkNotNull(configCache, "configCache");
        initConfigService();
        initAndConvertConfig();
        initRouterConfig();
        initDataSourceConfig();
        initRouter();
    }

    protected void initRouter() {
        DataSourceRouterImpl routerImplForRouting = new DataSourceRouterImpl();
        routerImplForRouting.setRouterRule(routerRule);
        routerImplForRouting.setDataSourcePool(dataSourceMap);
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

    protected String mysqldump() throws IOException, InterruptedException {
        List<String> cmdlist = new ArrayList<String>();
        cmdlist.add("mysqldump");

        Matcher matcher = JDBC_URL_PATTERN.matcher(originDataSourceConfig.getJdbcUrl());
        checkArgument(matcher.matches(), originDataSourceConfig.getJdbcUrl());

        String ip = matcher.group(1);
        String port = matcher.group(2);
        String ds = matcher.group(3);

        cmdlist.add("--host=" + ip);
        cmdlist.add("--port=" + port);
        cmdlist.add("--user=" + originDataSourceConfig.getUsername());
        cmdlist.add("--password=" + originDataSourceConfig.getPassword());
        cmdlist.addAll(task.getOptions());

        String outputFileName = getDumpFile(originGroupDataSource);
        cmdlist.add("--result-file=" + outputFileName);
        cmdlist.add(ds);
        cmdlist.add(task.getTableName());

        logger.info("start dumping " + ds + " ...");
        String output = executeByProcessBuilder(cmdlist);
        return output;
    }

    protected String getDumpFile(String databaseName) {
        return dumpOutputDir + databaseName + ".dump.sql";
    }


    private String mysqlload(String id, DataSourceConfig config) throws ExecuteException, IOException, InterruptedException {
        Matcher matcher = JDBC_URL_PATTERN.matcher(config.getJdbcUrl());
        checkArgument(matcher.matches(), config.getJdbcUrl());

        String ip = matcher.group(1);
        String port = matcher.group(2);
        String ds = matcher.group(3);

        List<String> cmdlist = new ArrayList<String>();
        cmdlist.add("mysql -f --default-character-set=utf8");
        cmdlist.add("'--database=" + ds + "'");
        cmdlist.add("'--user=" + config.getUsername() + "'");
        cmdlist.add("'--host=" + ip + "'");
        cmdlist.add("'--port=" + port + "'");
        cmdlist.add("'--password=" + config.getPassword() + "'");
        cmdlist.add("< '" + getDumpFile(id) + "'");
        logger.info("start loading " + id + " ...");
        return executeByProcessBuilder(Lists.newArrayList("sh", "-c", StringUtils.join(cmdlist, " ")));
    }

    protected String executeByProcessBuilder(List<String> cmd) throws IOException, InterruptedException {
        logger.info("execute shell script, cmd is: " + StringUtils.join(cmd, ' '));
        ProcessBuilderWrapper pbd = new ProcessBuilderWrapper(cmd);
        logger.info("Command has terminated with status: " + pbd.getStatus());
        logger.info("Output:\n" + pbd.getInfos());
        return pbd.getErrors();
    }

    protected void initDataSourceConfig() {
        if (!switchOn && !Strings.isNullOrEmpty(originGroupDataSource)) {
            DefaultDataSourceConfigManager configManager = new DefaultDataSourceConfigManager(this.originGroupDataSource, this.configService);
            configManager.init();
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
            configManager.init();
            DataSourceConfig config = findSingleMasterDataSourceConfig(configManager.getGroupDataSourceConfig());
            this.targetDataSourceConfigMap.put(entity.getKey(), config);
//            this.dataSourceMap.put(entity.getKey(), null);
        }
    }

    protected void initRouterConfig() {
        RouterRuleConfig routerRuleConfig = new RouterRuleConfig();
        routerRuleConfig.setTableShardConfigs(Lists.newArrayList(tableShardRuleConfig));
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
        Iterator<TableShardDimensionConfig> iterator = tableShardRuleConfig.getDimensionConfigs().iterator();
        while (iterator.hasNext()) {
            if (!iterator.next().isMaster()) {
                iterator.remove();
            }
        }
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
        try {
            mysqldump();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        try {
            processOriginDumpFile(getDumpFile(originGroupDataSource));
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            loadDumpFile();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //分割

        //执行
    }

    protected void loadDumpFile() throws IOException, InterruptedException {
        for (String key : bufferedWriterMap.keySet()) {
            DataSourceConfig config = targetDataSourceConfigMap.get(key);
            mysqlload(key, config);
        }
    }

    protected BufferedWriter getWriter(String ds) throws FileNotFoundException, UnsupportedEncodingException {
        if (!bufferedWriterMap.containsKey(ds)) {
            bufferedWriterMap.put(ds, createWriter(getDumpFile(ds)));
        }
        return bufferedWriterMap.get(ds);
    }

    protected BufferedWriter createWriter(String path) throws FileNotFoundException, UnsupportedEncodingException {
        File f = new File(path);
        if (f.exists()) {
            f.delete();
        }
        FileOutputStream fstream = new FileOutputStream(f);
        return new BufferedWriter(new OutputStreamWriter(fstream, "utf8"));
    }

    protected void processOriginDumpFile(String filePath) throws IOException {
        FileInputStream fstream = null;
        BufferedReader br = null;
        try {
            fstream = new FileInputStream(filePath);
            br = new BufferedReader(new InputStreamReader(fstream, "utf8"));

            String line;
            while ((line = br.readLine()) != null) {
                if (!line.startsWith("INSERT INTO")) {
                    continue;
                }

                distrubuteSql(line);
            }

            for (BufferedWriter writer : bufferedWriterMap.values()) {
                writer.flush();
                writer.close();
            }
        } finally {
            if (fstream != null) {
                fstream.close();
            }
            if (br != null) {
                br.close();
            }
        }
    }

    protected void distrubuteSql(String sql) throws IOException {
        RouterTarget target = router.getTarget(sql, new ArrayList<Object>());
        for (TargetedSql targetedSql : target.getTargetedSqls()) {
            for (String sqlStr : targetedSql.getSqls()) {
                getWriter(targetedSql.getDataSourceName()).write(sqlStr);
                getWriter(targetedSql.getDataSourceName()).newLine();
            }
        }
    }

    @Override
    public void pause(String detail) {

    }

    @Override
    public void succeed() {

    }

    @Override
    public TaskExecutorStatus getTaskExecutorStatus() {
        return this.status;
    }

    @Override
    public ShardDumpTask getTask() {
        return null;
    }

    @Override
    public TaskState getTaskState() {
        return null;
    }

    @Override
    public void setTaskState(TaskState taskState) {

    }

    @Override
    public void stop(String detail) {

    }

    public void setConfigCache(ConfigCache configCache) {
        this.configCache = configCache;
    }
}
