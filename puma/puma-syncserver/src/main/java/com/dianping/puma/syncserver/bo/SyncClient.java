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
import com.dianping.puma.core.sync.DatabaseConfig;
import com.dianping.puma.core.sync.InstanceConfig;
import com.dianping.puma.core.sync.SyncConfig;
import com.dianping.puma.core.sync.TableConfig;
import com.dianping.puma.syncserver.conf.Config;
import com.dianping.puma.syncserver.mysql.MysqlExecutor;
import com.dianping.puma.syncserver.web.SyncController;

public class SyncClient {
    private static final Logger LOG = LoggerFactory.getLogger(SyncController.class);

    private SyncConfig sync;
    private Configuration configuration;
    private PumaClient pumaClient;
    private MysqlExecutor mysqlExecutor;
    private String binlog;
    private String pumaServerHost = Config.getInstance().getPumaServerHost();
    private int pumaServerPort = Config.getInstance().getPumaServerPort();

    private long binlogPos;

    public void setSync(SyncConfig sync) {
        if (this.sync != null) {//修改sync(修改sync，只允许新增<database>或<table>级别的标签)
            //对比新旧sync，求出新增的<database>或<table>配置(如果新增*行，也要求出具体的database和table)
            List<DatabaseConfig> addedDatabases = _compare(this.sync, sync);
            LOG.info("sync xml changed database config:" + addedDatabases);
            //            //对新增的<database>或<table>配置，进行dump
            //            DumpClient dumpClient = new DumpClient();
            //            dumpClient.setSrc(this.sync.getSrc());
            //            dumpClient.setDest(this.sync.getDest());
            //            dumpClient.setDatabases(addedDatabases);
            //            Long dumpBinlogPos = dumpClient.dump();
            Long dumpBinlogPos = null;//dump结束后，dumpBinlogPos作为参数传递进来，表示需要追赶
            //（1）将使得dumpBinlogPos和curBinlogPos一致
            //（若dumpBinlogPos < curBinlogPos，则addedDatabases追赶）
            //（若dumpBinlogPos > curBinlogPos，则当前继续，直到一致。）
            //终止当前的PumaClient，记录当前binlogPos
            pumaClient.stop();
            Long curBinlogPos = this.binlogPos;
            //新建临时的PumaClient，对newDatabases进行追赶，起点为dumpBinlogPos，终点为curBinlogPos
            PumaClient pumaClientForPursue = _createPumaClientForPursue(dumpBinlogPos, curBinlogPos);
            //            pumaClientForPursue.start();

            //（2）dumpBinlogPos和curBinlogPos一致后，设置PumaClient的sync，重启PumaClient
            //使用新的sync，重新创建并启动新的PumaClient
            this.sync = sync;
            this.binlogPos = curBinlogPos;
            //            this.start();
        } else {
            this.sync = sync;
        }
    }

    /**
     * 新建临时的PumaClient，对newDatabases进行追赶，起点为dumpBinlogPos，终点为curBinlogPos
     */
    private PumaClient _createPumaClientForPursue(Long dumpBinlogPos, Long curBinlogPos) {
        // TODO Auto-generated method stub
        return null;
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
        if (!oldSync.getName().equals(newSync.getName())) {
            throw new IllegalArgumentException("name不一致！");
        }
        if (!oldSync.getServerId().equals(newSync.getServerId())) {
            throw new IllegalArgumentException("serverId不一致！");
        }
        if (!oldSync.getTarget().equals(newSync.getTarget())) {
            throw new IllegalArgumentException("target不一致！");
        }
        //对比instance
        InstanceConfig oldInstanceConfig = oldSync.getInstance();
        InstanceConfig newInstanceConfig = newSync.getInstance();
        List<DatabaseConfig> databaseConfig = oldInstanceConfig.compare(newInstanceConfig);
        return databaseConfig;
    }

    public PumaClient getPumaClient() {
        return pumaClient;
    }

    public void start() {
        //初始化PumaClient
        ConfigurationBuilder configBuilder = new ConfigurationBuilder();
        configBuilder.ddl(sync.getDdl());
        configBuilder.dml(sync.getDml());
        configBuilder.host(pumaServerHost);
        configBuilder.port(pumaServerPort);
        configBuilder.serverId(sync.getServerId());
        configBuilder.name(sync.getName());
        configBuilder.target(sync.getTarget());
        configBuilder.transaction(sync.getTransaction());
        configBuilder.binlog(binlog);
        configBuilder.binlogPos(binlogPos);
        _parseSourceDatabaseTables(sync, configBuilder);//configBuilder.tables("DianPing", "*");
        configuration = configBuilder.build();
        System.out.println(configuration);
        pumaClient = new PumaClient(configuration);
        //初始化mysqlExecutor
        mysqlExecutor = new MysqlExecutor(sync.getDest().getUrl(), sync.getDest().getUsername(), sync.getDest().getPassword());
        mysqlExecutor.setSync(sync);
        //注册监听器
        pumaClient.register(new EventListener() {
            @Override
            public void onSkipEvent(ChangedEvent event) {
                System.out.println(">>>>>>>>>>>>>>>>>>Skip " + event);
            }

            @Override
            public boolean onException(ChangedEvent event, Exception e) {
                System.out.println("-------------Exception " + e);
                return true;
            }

            @Override
            public void onEvent(ChangedEvent event) throws Exception {
                //                System.out.println("********************Received " + event);
                //动态更新binlog和binlogPos
                binlogPos = event.getBinlogPos();
                binlog = event.getBinlog();
                //执行同步
                mysqlExecutor.execute(event);
            }
        });

        //启动
        pumaClient.start();
        LOG.info("SyncClient started.");
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

    public SyncConfig getSync() {
        return sync;
    }

    public String getBinlog() {
        return binlog;
    }

    public long getBinlogPos() {
        return binlogPos;
    }

}