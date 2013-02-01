package com.dianping.puma.syncserver.job.executor;

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
    protected Configuration configuration;
    protected PumaClient pumaClient;
    protected MysqlExecutor mysqlExecutor;
    protected String pumaServerHost;
    protected int pumaServerPort;
    protected String target;
    protected TaskExecutorStatus status;

    private long sleepTime = 0;

    public AbstractTaskExecutor(T abstractTask, String pumaServerHost, int pumaServerPort, String target) {
        this.abstractTask = abstractTask;
        this.pumaServerHost = pumaServerHost;
        this.pumaServerPort = pumaServerPort;
        this.target = target;
        this.status = new TaskExecutorStatus();
        status.setTaskId(abstractTask.getId());
        status.setType(abstractTask.getType());
        //初始化PumaClient
        this.init();
    }

    /**
     * 事件到达回调函数
     * 
     * @param event 事件
     * @throws Exception
     */
    protected abstract void onEvent(ChangedEvent event) throws Exception;

    public void setAbstractTask(T abstractTask) {
        this.abstractTask = abstractTask;
    }

    @Override
    public T getTask() {
        return abstractTask;
    }

    @Override
    public void pause(String detail) {
        this.pumaClient.stop();
        this.status.setStatus(TaskExecutorStatus.Status.SUSPPENDED);
        this.status.setDetail(detail);
    }

    @Override
    public void succeed() {
        this.pumaClient.stop();
        this.status.setStatus(TaskExecutorStatus.Status.SUCCEED);
        this.status.setDetail(null);
    }

    @Override
    public void fail(String detail) {
        this.pumaClient.stop();
        this.status.setStatus(TaskExecutorStatus.Status.FAILED);
        this.status.setDetail(detail);
    }

    @Override
    public void start() {
        //启动
        pumaClient.start();
        this.status.setDetail(null);
        this.status.setStatus(TaskExecutorStatus.Status.RUNNING);
    }

    @Override
    public TaskExecutorStatus getTaskExecutorStatus() {
        return status;
    }

    private void init() {
        BinlogInfo startedBinlogInfo = abstractTask.getBinlogInfo();
        //1 初始化mysqlExecutor
        LOG.info("initing MysqlExecutor...");
        mysqlExecutor = new MysqlExecutor(abstractTask.getDestMysqlHost().getHost(), abstractTask.getDestMysqlHost().getUsername(),
                abstractTask.getDestMysqlHost().getPassword());
        mysqlExecutor.setMysqlMapping(abstractTask.getMysqlMapping());
        //2 初始化PumaClient
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
        configuration = configBuilder.build();
        LOG.info("PumaClient's config is: " + configuration);
        pumaClient = new PumaClient(configuration);
        if (startedBinlogInfo != null) {
            pumaClient.getSeqFileHolder().saveSeq(SubscribeConstant.SEQ_FROM_BINLOGINFO);
        }
        //注册监听器
        pumaClient.register(new EventListener() {

            private boolean dataChange = false;

            private DefaultPullStrategy defaultPullStrategy = new DefaultPullStrategy(500, 10000);

            private void recordBinlog(ChangedEvent event) {
                //动态更新binlog和binlogPos
                if (event != null) {
                    BinlogInfo binlogInfo = new BinlogInfo();
                    binlogInfo.setBinlogFile(event.getBinlog());
                    binlogInfo.setBinlogPosition(event.getBinlogPos());
                    status.setBinlogInfo(binlogInfo);
                }
            }

            @Override
            public void onSkipEvent(ChangedEvent event) {
                recordBinlog(event);
                LOG.info("onSkipEvent: " + event);
            }

            @Override
            public boolean onException(ChangedEvent event, Exception e) {
                recordBinlog(event);
                fail(e.getMessage());
                return false;
            }

            @Override
            public void onEvent(ChangedEvent event) throws Exception {
                LOG.info("********************Received " + event);
                if (!abstractTask.getBinlogInfo().isSkipToNextPos()) {
                    if (containDatabase(event.getDatabase())) {
                        if (event instanceof RowChangedEvent) {
                            if (((RowChangedEvent) event).isTransactionBegin()) {
                            } else if (((RowChangedEvent) event).isTransactionCommit()) {
                                if (dataChange) {
                                    //提交事务
                                    mysqlExecutor.commit();
                                    dataChange = false;
                                    //保存binlog信息到数据库
                                    BinlogInfo binlogInfo = new BinlogInfo();
                                    binlogInfo.setSkipToNextPos(true);
                                    binlogInfo.setBinlogFile(event.getBinlog());
                                    binlogInfo.setBinlogPosition(event.getBinlogPos());
                                    SystemStatusContainer.instance.recordBinlog(abstractTask.getType(), abstractTask.getId(),
                                            binlogInfo);
                                }
                            } else {
                                //执行子类的具体操作
                                AbstractTaskExecutor.this.onEvent(event);
                                dataChange = true;
                            }
                        }
                    }
                }

                //动态更新binlog和binlogPos
                recordBinlog(event);
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
        });
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

}
