package com.dianping.puma.api;

import com.dianping.puma.api.impl.*;

import java.util.List;

/**
 * 使用示例：
 * PumaClient client = new PumaClientConfig()
 * .setClientName("your-client-name")
 * .setDatabase("database")
 * .setTables(Lists.newArrayList("table0", "table1"))
 * .buildClusterPumaClient();
 * <p/>
 * while(!Thread.currentThread().isInterrupted()) {
 * try {
 * BinlogMessage binlogMessage = client.get(10, 1, TimeUnit.SECOND);
 * // 处理数据
 * client.ack(binlogMessage.getBinlogInfo());
 * } catch(Exception e) {
 * // 这里的异常主要是用来打点的，便于及时发现
 * }
 * }
 */
public class PumaClientConfig {

    private boolean enableEventLog = false;

    private String clientName;

    private String database;

    private List<String> tables;

    private boolean dml = true;

    private boolean ddl = false;

    private boolean transaction = false;

    private PumaServerRouter router;

    private String serverHost;

    private List<String> serverHosts;

    /**
     * 客户端名称（如果多台机器启动的 clientName 相同，那么只会有一个能读取到数据，其余会一直等待）
     *
     * @param clientName
     * @return
     */
    public PumaClientConfig setClientName(String clientName) {
        this.clientName = clientName;
        return this;
    }

    /**
     * 设置Puma客户端需要监听的数据库名称。每个客户端只能监听一个数据库。
     *
     * @param database
     * @return
     */
    public PumaClientConfig setDatabase(String database) {
        this.database = database;
        return this;
    }

    /**
     * 设置Puma客户端需要监听的数据库表的名称列表。每个客户端可以监听一个数据库下的任意多张表。
     *
     * @param tables
     * @return
     */
    public PumaClientConfig setTables(List<String> tables) {
        this.tables = tables;
        return this;
    }

    /**
     * 设置Puma客户端是否需要所监听库表的DML（Data Manipulation Language）事件。
     *
     * @param dml
     * @return
     */
    public PumaClientConfig setDml(boolean dml) {
        this.dml = dml;
        return this;
    }

    /**
     * 设置Puma客户端是否需要所监听库表的DDL（Data Definition Language）事件。
     *
     * @param ddl
     * @return
     */
    public PumaClientConfig setDdl(boolean ddl) {
        this.ddl = ddl;
        return this;
    }

    /**
     * 设置Puma客户端是否需要所监听库表的Transaction（begin，commit）事件。
     *
     * @param transaction
     * @return
     */
    public PumaClientConfig setTransaction(boolean transaction) {
        this.transaction = transaction;
        return this;
    }


    public PumaClientConfig setRouter(PumaServerRouter router) {
        this.router = router;
        return this;
    }

    public PumaClientConfig setServerHost(String serverHost) {
        this.serverHost = serverHost;
        return this;
    }

    public PumaClientConfig setServerHosts(List<String> serverHosts) {
        this.serverHosts = serverHosts;
        return this;
    }

    public boolean isEnableEventLog() {
        return enableEventLog;
    }

    public PumaClientConfig setEnableEventLog(boolean enableEventLog) {
        this.enableEventLog = enableEventLog;
        return this;
    }

    public String getClientName() {
        return clientName;
    }

    public String getDatabase() {
        return database;
    }

    public List<String> getTables() {
        return tables;
    }

    public boolean isDml() {
        return dml;
    }

    public boolean isDdl() {
        return ddl;
    }

    public boolean isTransaction() {
        return transaction;
    }

    public PumaServerRouter getRouter() {
        return router;
    }

    public String getServerHost() {
        return serverHost;
    }

    public List<String> getServerHosts() {
        return serverHosts;
    }

    public SimplePumaClient buildSimplePumaClient() {
        return new SimplePumaClient(this);
    }

    /**
     * 根据PumaClientConfig的配置创建具备ha功能的Puma客户端。
     *
     * @return
     */
    public PumaClient buildClusterPumaClient() {
        return buildLionClusterPumaClient();
    }

    public PumaClient buildLionClusterPumaClient() {
        ConfigPumaServerMonitor monitor = new ConfigPumaServerMonitor(database, tables);
        router = new RoundRobinPumaServerRouter(monitor);
        return new ClusterPumaClient(this);
    }

    public PumaClient buildFixedClusterPumaClient() {
        FixedPumaServerMonitor monitor = new FixedPumaServerMonitor(serverHosts);
        router = new RoundRobinPumaServerRouter(monitor);
        return new ClusterPumaClient(this);
    }
}
