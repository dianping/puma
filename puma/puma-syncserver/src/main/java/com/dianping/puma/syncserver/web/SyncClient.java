package com.dianping.puma.syncserver.web;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.dianping.puma.api.Configuration;
import com.dianping.puma.api.ConfigurationBuilder;
import com.dianping.puma.api.EventListener;
import com.dianping.puma.api.PumaClient;
import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.event.DdlEvent;
import com.dianping.puma.core.event.RowChangedEvent;
import com.dianping.puma.core.sync.Database;
import com.dianping.puma.core.sync.Instance;
import com.dianping.puma.core.sync.Sync;
import com.dianping.puma.core.sync.Table;

public class SyncClient {
    private Sync sync;
    private Configuration configuration;
    private PumaClient pumaClient;
    private String binlog;

    private long binlogPos;

    public void setSync(Sync sync) {
        if (this.sync != null) {//修改sync(修改sync，只允许新增<database>或<table>级别的标签)
            //对比新旧sync，求出新增的<database>或<table>配置(如果新增*行，也要求出具体的database和table)
            List<Database> newDatabases = _compare(this.sync, sync);
            //对新增的<database>或<table>配置，进行dump
            DumpClient dumpClient = new DumpClient();
            dumpClient.setSrc(this.sync.getSrc());
            dumpClient.setDest(this.sync.getDest());
            dumpClient.setDatabases(newDatabases);
            Long dumpBinlogPos = dumpClient.dump();
            //终止当前的PumaClient，记录当前binlogPos
            pumaClient.stop();
            Long curBinlogPos = this.binlogPos;
            //新建临时的PumaClient，对newDatabases进行追赶，起点为dumpBinlogPos，终点为curBinlogPos
            PumaClient pumaClientForPursue = _createPumaClientForPursue(dumpBinlogPos, curBinlogPos);
            pumaClientForPursue.start();
            //使用新的sync，重新创建并启动新的PumaClient
            this.sync = sync;
            this.binlogPos = curBinlogPos;
            this.start();
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
     * 对比新旧sync，求出新增的database或table配置(table也属于database下，故返回的都是database)
     */
    private List<Database> _compare(Sync oldSync, Sync newSync) {
        // TODO Auto-generated method stub
        return null;
    }

    public PumaClient getPumaClient() {
        return pumaClient;
    }

    public void start() {
        ConfigurationBuilder configBuilder = new ConfigurationBuilder();
        configBuilder.ddl(sync.getDdl());
        configBuilder.dml(sync.getDml());
        configBuilder.host(sync.getSrc().getHost());
        configBuilder.port(sync.getSrc().getPort());
        configBuilder.serverId(sync.getServerId());
        configBuilder.name(sync.getName());
        configBuilder.target(sync.getTarget());
        configBuilder.transaction(sync.getTransaction());
        configBuilder.binlog(binlog);
        configBuilder.binlogPos(binlogPos);
        //设置tables
        _parseSourceDatabaseTables(sync, configBuilder);//configBuilder.tables("DianPing", "*");

        //启动PumaClient
        configuration = configBuilder.build();
        pumaClient = new PumaClient(configuration);
        pumaClient.register(new EventListener() {//TODO 建立dest的mysql连接

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
                        System.out.println("********************Received " + event);
                        binlogPos = event.getBinlogPos();
                        _sync(event);
                    }
                });
        pumaClient.start();
    }

    /**
     * 设置同步源的数据库和表
     */
    private void _parseSourceDatabaseTables(Sync sync, ConfigurationBuilder configBuilder) {
        Instance instance = sync.getInstance();
        List<Database> databases = instance.getDatabases();
        if (databases != null) {
            for (Database database : databases) {
                //解析database
                String databaseFrom = database.getFrom();
                //解析table
                List<Table> tables = database.getTables();
                if (tables != null) {
                    //如果table中有一个是*，则只需要设置一个*；否则，添加所有table配置
                    List<String> tableFroms = new ArrayList<String>();
                    boolean star = false;
                    for (Table table : tables) {
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

    /**
     * 收到一条ChangedEvent，进行同步
     */
    private void _sync(ChangedEvent event) {
        if (event instanceof DdlEvent) {

        } else if (event instanceof RowChangedEvent) {

        }
    }

    public Sync getSync() {
        return sync;
    }

    public String getBinlog() {
        return binlog;
    }

    public void setBinlog(String binlog) {
        this.binlog = binlog;
    }

    public long getBinlogPos() {
        return binlogPos;
    }

    public void setBinlogPos(long binlogPos) {
        this.binlogPos = binlogPos;
    }

}
