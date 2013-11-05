package com.dianping.puma.syncserver.job.executor;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections.buffer.CircularFifoBuffer;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.lion.client.ConfigCache;
import com.dianping.lion.client.LionException;
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
import com.dianping.puma.syncserver.job.executor.failhandler.HandleContext;
import com.dianping.puma.syncserver.job.executor.failhandler.HandleResult;
import com.dianping.puma.syncserver.job.executor.failhandler.Handler;
import com.dianping.puma.syncserver.job.executor.failhandler.HandlerContainer;
import com.dianping.puma.syncserver.monitor.SystemStatusContainer;
import com.dianping.puma.syncserver.mysql.MysqlExecutor;

public abstract class AbstractTaskExecutor<T extends AbstractTask> implements TaskExecutor<T>, SpeedControllable {
    private static final Logger  LOG              = LoggerFactory.getLogger(AbstractTaskExecutor.class);

    protected T                  abstractTask;
    protected PumaClient         pumaClient;
    protected MysqlExecutor      mysqlExecutor;
    protected String             pumaServerHost;
    protected int                pumaServerPort;
    protected String             target;
    protected TaskExecutorStatus status;
    /** 标识对目标数据库的会话。是否已经开始了事务（如果是，可能需要commmit或rollback否则由于数据库是可重复读级别，会一直锁住数据库。当开始insert/update/delete操作，无论执行是否成功，都已经开始事务） */
    private boolean              transactionStart = false;

    private long                 sleepTime        = 0;

    private CircularFifoBuffer   lastEvents       = new CircularFifoBuffer(10);

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
        mysqlExecutor = new MysqlExecutor(abstractTask.getDestMysqlHost().getHost(), abstractTask.getDestMysqlHost().getUsername(), abstractTask.getDestMysqlHost().getPassword());
        mysqlExecutor.setMysqlMapping(abstractTask.getMysqlMapping());
    }

    /**
     * 事件到达回调函数
     * 
     * @param event 事件
     * @throws Exception
     */
    protected abstract void execute(ChangedEvent event) throws SQLException;

    /**
     * 更新sql thread的binlog信息，和保存binlog信息到数据库
     */
    protected void binlogOfSqlThreadChanged(ChangedEvent event) {
        //动态更新binlog和binlogPos
        if (event != null) {
            BinlogInfo binlogInfo = new BinlogInfo();
            binlogInfo.setBinlogFile(event.getBinlog());
            binlogInfo.setBinlogPosition(event.getBinlogPos());
            binlogInfo.setSkipToNextPos(true);
            status.setBinlogInfo(binlogInfo);
            abstractTask.setBinlogInfo(binlogInfo);
            //保存binlog信息到数据库
            saveBinlogToDB(binlogInfo);
        }
    }

    protected void binlogOfIOThreadChanged(ChangedEvent event) {
        //动态更新binlog和binlogPos
        if (event != null) {
            BinlogInfo binlogInfo = new BinlogInfo();
            binlogInfo.setBinlogFile(event.getBinlog());
            binlogInfo.setBinlogPosition(event.getBinlogPos());
            status.setBinlogInfoOfIOThread(binlogInfo);
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
            if (transactionStart) {
                mysqlExecutor.rollback();
                transactionStart = false;
            }
        } catch (SQLException e) {
            LOG.error(e.getMessage(), e);
        }
        if (this.pumaClient != null) {
            this.pumaClient.stop();
        }
        this.status.setStatus(TaskExecutorStatus.Status.SUSPPENDED);
        this.status.setDetail(detail);
        LOG.info("TaskExecutor[" + this.getTask().getPumaClientName() + "] paused... cause:" + detail);
    }

    @Override
    public void stop(String detail) {
        try {
            if (transactionStart) {
                mysqlExecutor.rollback();
                transactionStart = false;
            }
        } catch (SQLException e) {
            LOG.error(e.getMessage(), e);
        }
        if (this.pumaClient != null) {
            this.pumaClient.stop();
        }
        this.status.setStatus(TaskExecutorStatus.Status.SUCCEED);
        this.status.setDetail(detail);
        LOG.info("TaskExecutor[" + this.getTask().getPumaClientName() + "] stop... cause:" + detail);
    }

    @Override
    public void succeed() {
        try {
            if (transactionStart) {
                mysqlExecutor.rollback();
                transactionStart = false;
            }
        } catch (SQLException e) {
            LOG.error(e.getMessage(), e);
        }
        if (this.pumaClient != null) {
            this.pumaClient.stop();
        }
        this.status.setStatus(TaskExecutorStatus.Status.SUCCEED);
        this.status.setDetail(null);
        LOG.info("TaskExecutor[" + this.getTask().getPumaClientName() + "] succeeded...");
    }

    private void fail(String detail) {
        try {
            if (transactionStart) {
                mysqlExecutor.rollback();
                transactionStart = false;
            }
        } catch (SQLException e) {
            LOG.error(e.getMessage(), e);
        }
        if (this.pumaClient != null) {
            this.pumaClient.stop();
        }
        this.status.setStatus(TaskExecutorStatus.Status.FAILED);
        this.status.setDetail(detail);
        LOG.info("TaskExecutor[" + this.getTask().getPumaClientName() + "] failed... cause:" + detail);
    }

    @Override
    public void start() {
        //读取binlog位置，创建PumaClient，设置PumaCleint的config，再启动
        if (this.pumaClient != null) {
            this.pumaClient.stop();
        }
        pumaClient = createPumaClient();
        pumaClient.start();
        this.status.setDetail(null);
        this.status.setStatus(TaskExecutorStatus.Status.RUNNING);
        LOG.info("TaskExecutor[" + this.getTask().getPumaClientName() + "] started...");
    }

    @Override
    public TaskExecutorStatus getTaskExecutorStatus() {
        return status;
    }

    private PumaClient createPumaClient() {
        final BinlogInfo startedBinlogInfo = abstractTask.getBinlogInfo();
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

        //读取本地文件，获取seq
        Long seq = null;
        File file = new File("/data/appdatas/puma-syncserver/" + abstractTask.getPumaClientName() + "/seq");
        if (file.exists()) {
            try {
                seq = NumberUtils.toLong(StringUtils.trim(FileUtils.readFileToString(file)));
                LOG.info("PumaClient[" + abstractTask.getPumaClientName() + "] Read from file, Seq is:" + seq);
                FileUtils.deleteQuietly(file);
            } catch (Exception e1) {
                LOG.warn("File error, igore this seq settiing.", e1);
            }
        }
        if (seq == null && startedBinlogInfo != null) {
            seq = SubscribeConstant.SEQ_FROM_BINLOGINFO;
        }
        if (seq == null) {
            seq = SubscribeConstant.SEQ_FROM_LATEST;
        }
        LOG.info("PumaClient[" + abstractTask.getPumaClientName() + "] Seq is:" + seq);
        pumaClient.getSeqFileHolder().saveSeq(seq);

        //注册监听器
        pumaClient.register(new EventListener() {

            private DefaultPullStrategy defaultPullStrategy = new DefaultPullStrategy(500, 10000);
            /** 记录一个收到多少个commit事件 */
            private int                 commitBinlogCount   = 0;
            /** 对于PumaClient记录的binlog，需要在一开始skip */
            private boolean             skipToNextPos       = startedBinlogInfo.isSkipToNextPos();

            @Override
            public void onSkipEvent(ChangedEvent event) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("onSkipEvent: " + event);
                }
            }

            @Override
            public boolean onException(ChangedEvent event, Exception e) {
                //针对策略，调用策略的处理
                boolean ignoreFailEvent = false;
                if (e instanceof SQLException) {
                    SQLException se = (SQLException) e;
                    Integer errorCode = se.getErrorCode();
                    Map<Integer, String> errorCodeHandlerMap = abstractTask.getErrorCodeHandlerNameMap();
                    if (errorCodeHandlerMap != null) {
                        String handlerName = errorCodeHandlerMap.get(errorCode);
                        if (handlerName != null) {
                            Handler handler = HandlerContainer.getInstance().getHandler(handlerName);
                            if (handler != null) {
                                try {
                                    LOG.info("Invoke handler(" + handler.getName() + "), event : " + event);
                                    HandleContext context = new HandleContext();
                                    context.setMysqlExecutor(mysqlExecutor);
                                    context.setChangedEvent(event);
                                    context.setTask(abstractTask);
                                    HandleResult handleResult = handler.handle(context);
                                    ignoreFailEvent = handleResult.isIgnoreFailEvent();
                                } catch (RuntimeException re) {
                                    LOG.warn("Unexpected RuntimeException on handler(" + handler.getName() + "), ignoreFailEvent keep false.", re);
                                }
                            }
                        }
                    }
                }
                if (!ignoreFailEvent) {
                    fail(abstractTask.getSrcMysqlName() + "->" + abstractTask.getDestMysqlName() + ":" + e.getMessage() + ". Event=" + event);
                    LOG.info("Print last 10 row change events: " + lastEvents.toString());
                }
                return ignoreFailEvent;
            }

            @Override
            public void onEvent(ChangedEvent event) throws Exception {
                //                if (LOG.isDebugEnabled()) {
                //                    LOG.debug("********************Received " + event);
                //                }
                if (!skipToNextPos) {
                    if (event instanceof RowChangedEvent) {
                        //------------- (1) 【事务开始事件】--------------
                        if (((RowChangedEvent) event).isTransactionBegin()) {
                        } else if (((RowChangedEvent) event).isTransactionCommit()) {
                            //--------- (2) 【事务提交事件】--------------
                            if (containDatabase(event.getDatabase()) && transactionStart) {
                                //提交事务(datachange了，则该commit肯定是属于当前做了数据操作的事务的，故mysqlExecutor.commit();)
                                mysqlExecutor.commit();
                                transactionStart = false;
                                //遇到commit事件，操作数据库了，更新sqlbinlog和保存binlog到数据库
                                binlogOfSqlThreadChanged(event);
                                commitBinlogCount = 0;
                            } else {
                                //只要累计遇到的commit事件1000个(无论是否属于抓取的database)，都更新sqlbinlog和保存binlog到数据库，为的是即使当前task更新不频繁，也不要让它的binlog落后太多
                                if (++commitBinlogCount > getSaveCommitCount()) {
                                    binlogOfSqlThreadChanged(event);
                                    commitBinlogCount = 0;
                                }
                            }
                            //实时更新iobinlog位置(该io binlog位置也必须都是commmit事件的位置，这样的位置才是一个合理状态的位置，否则如果是一半事务的binlog位置，那么从该binlog位置订阅将是错误的状态)
                            binlogOfIOThreadChanged(event);

                        } else if (containDatabase(event.getDatabase())) {
                            //--------- (3) 【数据操作事件】--------------
                            //可执行的event，保存到内存，出错时打印出来
                            lastEvents.add(event);
                            //标识事务开始
                            transactionStart = true;
                            //执行子类的具体操作
                            AbstractTaskExecutor.this.execute(event);
                        }
                    }
                } else {
                    skipToNextPos = false;
                    LOG.info("********************skip this event(because skipToNextPos is true) : " + event);
                }

                //速度调控
                if (sleepTime > 0) {
                    try {
                        TimeUnit.MILLISECONDS.sleep(sleepTime);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }

            }

            @Override
            public void onConnectException(Exception e) {
                status.setStatus(TaskExecutorStatus.Status.RECONNECTING);
                String detail = abstractTask.getSrcMysqlName() + "->" + abstractTask.getDestMysqlName() + ":PumaClient connected failed, reconnecting...";
                status.setDetail(detail);
                LOG.error(detail, e);
                defaultPullStrategy.fail(true);
            }

            @Override
            public void onConnected() {
                status.setStatus(TaskExecutorStatus.Status.RUNNING);
                status.setDetail("PumaClient connected.");
                LOG.info("PumaClient[" + getTask().getPumaClientName() + "] connected.");
            }
        });

        return pumaClient;
    }

    private void saveBinlogToDB(BinlogInfo binlogInfo) {
        SystemStatusContainer.instance.recordBinlog(abstractTask.getType(), abstractTask.getId(), binlogInfo);
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
        LOG.info("TaskExecutor[" + this.getTask().getPumaClientName() + "] speedUp. sleepTime:" + sleepTime + "ms.");
    }

    @Override
    public void speedDown() {
        if (sleepTime < 3000) {
            sleepTime += 500;
        }
        LOG.info("TaskExecutor[" + this.getTask().getPumaClientName() + "] speedDown. sleepTime:" + sleepTime + "ms.");
    }

    @Override
    public void resetSpeed() {
        sleepTime = 0;
        LOG.info("TaskExecutor[" + this.getTask().getPumaClientName() + "] resetSpeed. sleepTime:" + sleepTime + "ms.");
    }

    @Override
    public String toString() {
        return "AbstractTaskExecutor [abstractTask=" + abstractTask + ", pumaClient=" + pumaClient + ", mysqlExecutor=" + mysqlExecutor + ", pumaServerHost=" + pumaServerHost + ", pumaServerPort="
                + pumaServerPort + ", target=" + target + ", status=" + status + ", transactionStart=" + transactionStart + ", sleepTime=" + sleepTime + ", lastEvents=" + lastEvents + "]";
    }

    private int getSaveCommitCount() {
        int count = 50000;//默认是5万
        try {
            Integer t = ConfigCache.getInstance().getIntProperty("puma.syncserver.saveCommitCount");
            if (t != null) {
                count = t.intValue();
            }
        } catch (LionException e) {
            LOG.error(e.getMessage(), e);
        }
        return count;
    }

}
