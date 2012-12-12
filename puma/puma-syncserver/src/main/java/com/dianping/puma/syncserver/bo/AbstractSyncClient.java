package com.dianping.puma.syncserver.bo;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.puma.api.Configuration;
import com.dianping.puma.api.ConfigurationBuilder;
import com.dianping.puma.api.EventListener;
import com.dianping.puma.api.PumaClient;
import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.sync.BinlogInfo;
import com.dianping.puma.core.sync.DatabaseConfig;
import com.dianping.puma.core.sync.InstanceConfig;
import com.dianping.puma.core.sync.SyncConfig;
import com.dianping.puma.core.sync.TableConfig;
import com.dianping.puma.syncserver.conf.Config;
import com.dianping.puma.syncserver.mysql.MysqlExecutor;

public abstract class AbstractSyncClient {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractSyncClient.class);
    /**  */
    protected SyncConfig sync;
    protected Configuration configuration;
    protected PumaClient pumaClient;
    protected MysqlExecutor mysqlExecutor;
    protected BinlogInfo curBinlogInfo = new BinlogInfo();
    protected String pumaServerHost = Config.getInstance().getPumaServerHost();
    protected int pumaServerPort = Config.getInstance().getPumaServerPort();

    public AbstractSyncClient(SyncConfig sync, BinlogInfo startedBinlogInfo) {
        this.sync = sync;
        //初始化PumaClient
        this._init(startedBinlogInfo);
    }

    /**
     * 事件到达回调函数
     * 
     * @param event 事件
     * @throws Exception
     */
    protected abstract void onEvent(ChangedEvent event) throws Exception;

    public void setSync(SyncConfig sync) {
        if (this.sync != null) {//修改sync(修改sync，只允许新增<database>或<table>级别的标签)
            //对比新旧sync，求出新增的<database>或<table>配置(如果新增*行，也要求出具体的database和table)
            List<DatabaseConfig> addedDatabases = _compare(this.sync, sync);
            LOG.info("sync xml changed database config:" + addedDatabases);
            this.sync = sync;
        } else {
            this.sync = sync;
        }
        LOG.info("SyncClient modify.");
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

    public BinlogInfo stop() {
        this.pumaClient.stop();
        return this.curBinlogInfo;
    }

    public void start() {
        //启动
        LOG.info("starting PumaClient...");
        pumaClient.start();
        LOG.info("started PumaClient.");
    }

    private void _init(BinlogInfo startedBinlogInfo) {
        //1 初始化mysqlExecutor
        LOG.info("initing MysqlExecutor...");
        mysqlExecutor = new MysqlExecutor(sync.getDest().getHost(), sync.getDest().getUsername(), sync.getDest().getPassword());
        mysqlExecutor.setSync(sync);
        //2 初始化PumaClient
        LOG.info("initing PumaClient...");
        ConfigurationBuilder configBuilder = new ConfigurationBuilder();
        configBuilder.ddl(sync.getSrc().getDdl());
        configBuilder.dml(sync.getSrc().getDml());
        configBuilder.host(pumaServerHost);
        configBuilder.port(pumaServerPort);
        configBuilder.serverId(sync.getSrc().getServerId());
        configBuilder.name(sync.getSrc().getName());
        configBuilder.target(sync.getSrc().getTarget());
        configBuilder.transaction(sync.getSrc().getTransaction());
        if (startedBinlogInfo != null) {
            configBuilder.binlog(startedBinlogInfo.getBinlogFile());
            configBuilder.binlogPos(startedBinlogInfo.getBinlogPosition());
        }
        _parseSourceDatabaseTables(sync, configBuilder);
        configuration = configBuilder.build();
        LOG.info("PumaClient's config is: " + configuration);
        pumaClient = new PumaClient(configuration);
        //注册监听器
        pumaClient.register(new EventListener() {
            @Override
            public void onSkipEvent(ChangedEvent event) {
                LOG.info("onSkipEvent: " + event);
            }

            @Override
            public boolean onException(ChangedEvent event, Exception e) {
                LOG.error(e.getMessage(), e);
                return true;
            }

            @Override
            public void onEvent(ChangedEvent event) throws Exception {
                //动态更新binlog和binlogPos
                curBinlogInfo.setBinlogPosition(event.getBinlogPos());
                curBinlogInfo.setBinlogFile(event.getBinlog());
                //执行子类的具体操作
                AbstractSyncClient.this.onEvent(event);
            }
        });
    }

    /**
     * 设置同步源的数据库和表
     */
    private void _parseSourceDatabaseTables(SyncConfig sync, ConfigurationBuilder configBuilder) {
        InstanceConfig instance = sync.getInstance();
        List<DatabaseConfig> databases = instance.getDatabases();
        if (databases != null) {
            for (DatabaseConfig database : databases) {
                //解析database
                String databaseFrom = database.getFrom();
                //解析table
                List<TableConfig> tables = database.getTables();
                if (tables != null) {
                    //如果table中有一个是*，则只需要设置一个*；否则，添加所有table配置
                    List<String> tableFroms = new ArrayList<String>();
                    boolean star = false;
                    for (TableConfig table : tables) {
                        if (StringUtils.equals(table.getFrom(), "*")) {
                            star = true;
                            break;
                        } else {
                            tableFroms.add(table.getFrom());
                        }
                    }
                    if (star) {
                        configBuilder.tables(databaseFrom, "*");
                    } else {
                        for (String tableFrom : tableFroms) {
                            configBuilder.tables(databaseFrom, tableFrom);
                        }
                    }
                }
            }
        }
    }

    public BinlogInfo getCurBinlogInfo() {
        return curBinlogInfo;
    }

}
