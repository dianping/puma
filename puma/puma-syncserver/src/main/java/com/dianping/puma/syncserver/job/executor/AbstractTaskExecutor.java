package com.dianping.puma.syncserver.job.executor;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.puma.api.Configuration;
import com.dianping.puma.api.ConfigurationBuilder;
import com.dianping.puma.api.EventListener;
import com.dianping.puma.api.PumaClient;
import com.dianping.puma.core.constant.SubscribeConstant;
import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.event.RowChangedEvent;
import com.dianping.puma.core.sync.model.BinlogInfo;
import com.dianping.puma.core.sync.model.mapping.DatabaseMapping;
import com.dianping.puma.core.sync.model.mapping.MysqlMapping;
import com.dianping.puma.core.sync.model.mapping.TableMapping;
import com.dianping.puma.core.sync.model.task.AbstractTask;
import com.dianping.puma.core.sync.model.taskexecutor.TaskExecutorStatus;
import com.dianping.puma.core.util.DefaultPullStrategy;
import com.dianping.puma.syncserver.monitor.SystemStatusContainer;
import com.dianping.puma.syncserver.mysql.MysqlExecutor;

public abstract class AbstractTaskExecutor<T extends AbstractTask> implements TaskExecutor<T>, SpeedControllable {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractTaskExecutor.class);

    protected T abstractTask;
    protected PumaClient pumaClient;
    protected MysqlExecutor mysqlExecutor;
    protected String pumaServerHost;
    protected int pumaServerPort;
    protected String target;
    protected TaskExecutorStatus status;
    private boolean dataChange = false;

    private long sleepTime = 0;

    public AbstractTaskExecutor(T abstractTask, String pumaServerHost, int pumaServerPort, String target) {
        this.abstractTask = abstractTask;
        this.pumaServerHost = pumaServerHost;
        this.pumaServerPort = pumaServerPort;
        this.target = target;
        this.status = new TaskExecutorStatus();
        status.setTaskId(abstractTask.getId());
        status.setType(abstractTask.getType());
        //        BinlogInfo startedBinlogInfo = abstractTask.getBinlogInfo();
        //初始化mysqlExecutor
        LOG.info("initing MysqlExecutor...");
        mysqlExecutor = new MysqlExecutor(abstractTask.getDestMysqlHost().getHost(), abstractTask.getDestMysqlHost().getUsername(),
                abstractTask.getDestMysqlHost().getPassword());
        mysqlExecutor.setMysqlMapping(abstractTask.getMysqlMapping());
    }

    /**
     * 事件到达回调函数
     * 
     * @param event 事件
     * @throws Exception
     */
    protected abstract void execute(ChangedEvent event) throws Exception;

    protected void binlogChanged(ChangedEvent event) {
        //动态更新binlog和binlogPos
        if (event != null) {
            BinlogInfo binlogInfo = new BinlogInfo();
            binlogInfo.setBinlogFile(event.getBinlog());
            binlogInfo.setBinlogPosition(event.getBinlogPos());
            status.setBinlogInfo(binlogInfo);
            abstractTask.setBinlogInfo(binlogInfo);
        }
    }

    public void setAbstractTask(T abstractTask) {
        this.abstractTask = abstractTask;
    }

    @Override
    public T getTask() {
        return abstractTask;
    }

    @Override
    public void pause(String detail) {
        try {
            if (dataChange) {
                mysqlExecutor.rollback();
            }
        } catch (SQLException e) {
            LOG.error(e.getMessage(), e);
        }
        this.pumaClient.stop();
        this.status.setStatus(TaskExecutorStatus.Status.SUSPPENDED);
        this.status.setDetail(detail);
        LOG.info("TaskExecutor[" + this.toString() + "] paused...");
    }

    @Override
    public void disconnect(String detail) {
        try {
            if (dataChange) {
                mysqlExecutor.rollback();
            }
        } catch (SQLException e) {
            LOG.error(e.getMessage(), e);
        }
        this.pumaClient.stop();
        this.status.setStatus(TaskExecutorStatus.Status.SUCCEED);
        this.status.setDetail(detail);
        LOG.info("TaskExecutor[" + this.toString() + "] disconnected...");
    }

    @Override
    public void succeed() {
        try {
            if (dataChange) {
                mysqlExecutor.rollback();
            }
        } catch (SQLException e) {
            LOG.error(e.getMessage(), e);
        }
        this.pumaClient.stop();
        this.status.setStatus(TaskExecutorStatus.Status.SUCCEED);
        this.status.setDetail(null);
        LOG.info("TaskExecutor[" + this.toString() + "] succeeded...");
    }

    @Override
    public void fail(String detail) {
        try {
            if (dataChange) {
                mysqlExecutor.rollback();
            }
        } catch (SQLException e) {
            LOG.error(e.getMessage(), e);
        }
        this.pumaClient.stop();
        this.status.setStatus(TaskExecutorStatus.Status.FAILED);
        this.status.setDetail(detail);
        LOG.info("TaskExecutor[" + this.toString() + "] failed...");
    }

    @Override
    public void start() {
        //读取binlog位置，创建PumaClient，设置PumaCleint的config，再启动
        pumaClient = createPumaClient(abstractTask.getBinlogInfo());
        pumaClient.start();
        this.status.setDetail(null);
        this.status.setStatus(TaskExecutorStatus.Status.RUNNING);
        LOG.info("TaskExecutor[" + this.toString() + "] started...");
    }

    @Override
    public TaskExecutorStatus getTaskExecutorStatus() {
        return status;
    }

    private PumaClient createPumaClient(BinlogInfo startedBinlogInfo) {
        LOG.info("initing PumaClient...");
        ConfigurationBuilder configBuilder = new ConfigurationBuilder();
        configBuilder.ddl(abstractTask.isDdl());
        configBuilder.dml(abstractTask.isDml());
        configBuilder.host(pumaServerHost);
        configBuilder.port(pumaServerPort);
        configBuilder.serverId(abstractTask.getServerId());
        configBuilder.name(abstractTask.getPumaClientName());
        configBuilder.target(target);
        configBuilder.transaction(abstractTask.isTransaction());
        if (startedBinlogInfo != null) {
            configBuilder.binlog(startedBinlogInfo.getBinlogFile());
            configBuilder.binlogPos(startedBinlogInfo.getBinlogPosition());
        }
        _parseSourceDatabaseTables(abstractTask.getMysqlMapping(), configBuilder);
        Configuration configuration = configBuilder.build();
        LOG.info("PumaClient's config is: " + configuration);
        PumaClient pumaClient = new PumaClient(configuration);
        if (startedBinlogInfo != null) {
            pumaClient.getSeqFileHolder().saveSeq(SubscribeConstant.SEQ_FROM_BINLOGINFO);
        }
        //注册监听器
        pumaClient.register(new EventListener() {

            private DefaultPullStrategy defaultPullStrategy = new DefaultPullStrategy(500, 10000);

            @Override
            public void onSkipEvent(ChangedEvent event) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("onSkipEvent: " + event);
                }
            }

            @Override
            public boolean onException(ChangedEvent event, Exception e) {
                fail(e.getMessage());
                return false;
            }

            @Override
            public void onEvent(ChangedEvent event) throws Exception {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("********************Received " + event);
                }
                if (!abstractTask.getBinlogInfo().isSkipToNextPos()) {//对于PumaClient记录的binlog，需要在一开始skip
                    if (containDatabase(event.getDatabase())) {
                        if (event instanceof RowChangedEvent) {
                            if (((RowChangedEvent) event).isTransactionBegin()) {
                            } else if (((RowChangedEvent) event).isTransactionCommit()) {
                                if (dataChange) {
                                    //提交事务
                                    mysqlExecutor.commit();
                                    dataChange = false;
                                    //遇到commit事件，才保存binlog信息到数据库
                                    BinlogInfo binlogInfo = new BinlogInfo();
                                    binlogInfo.setSkipToNextPos(true);
                                    binlogInfo.setBinlogFile(event.getBinlog());
                                    binlogInfo.setBinlogPosition(event.getBinlogPos());
                                    SystemStatusContainer.instance.recordBinlog(abstractTask.getType(), abstractTask.getId(),
                                            binlogInfo);
                                    //遇到commit事件，才更新binlog和binlogPos
                                    binlogChanged(event);

                                }
                            } else {
                                //执行子类的具体操作
                                AbstractTaskExecutor.this.execute(event);
                                dataChange = true;
                            }
                        }
                    }
                } else {
                    abstractTask.getBinlogInfo().setSkipToNextPos(false);
                }

                //速度调控
                if (sleepTime > 0) {
                    TimeUnit.MILLISECONDS.sleep(sleepTime);
                }

            }

            @Override
            public void onConnectException(Exception e) {
                status.setStatus(TaskExecutorStatus.Status.RECONNECTING);
                status.setDetail("PumaClient connected failed, reconnecting...");
                defaultPullStrategy.fail(true);
            }

            @Override
            public void onConnected() {
                status.setStatus(TaskExecutorStatus.Status.RUNNING);
                status.setDetail("PumaClient connected.");
            }
        });

        return pumaClient;
    }

    private boolean containDatabase(String database) {
        for (DatabaseMapping dbMapping : this.abstractTask.getMysqlMapping().getDatabases()) {
            if (StringUtils.equalsIgnoreCase(dbMapping.getFrom(), database)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 设置同步源的数据库和表
     */
    private void _parseSourceDatabaseTables(MysqlMapping mysqlMapping, ConfigurationBuilder configBuilder) {
        List<DatabaseMapping> databases = mysqlMapping.getDatabases();
        if (databases != null) {
            for (DatabaseMapping database : databases) {
                //解析database
                String databaseFrom = database.getFrom();
                //解析table
                List<TableMapping> tables = database.getTables();
                if (tables != null) {
                    //如果table中有一个是*，则只需要设置一个*；否则，添加所有table配置
                    List<String> tableFroms = new ArrayList<String>();
                    boolean star = false;
                    for (TableMapping table : tables) {
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

    @Override
    public void speedUp() {
        if (sleepTime >= 500) {
            sleepTime -= 500;
        }
    }

    @Override
    public void speedDown() {
        if (sleepTime < 3000) {
            sleepTime += 500;
        }
    }

    @Override
    public void resetSpeed() {
        sleepTime = 0;
    }

    @Override
    public String toString() {
        return "AbstractTaskExecutor [abstractTask=" + abstractTask + ", pumaClient=" + pumaClient + ", mysqlExecutor="
                + mysqlExecutor + ", pumaServerHost=" + pumaServerHost + ", pumaServerPort=" + pumaServerPort + ", target="
                + target + ", status=" + status + ", dataChange=" + dataChange + ", sleepTime=" + sleepTime + "]";
    }

}
