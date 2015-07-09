package com.dianping.puma.syncserver.job.executor;

import com.dianping.puma.api.Configuration;
import com.dianping.puma.api.ConfigurationBuilder;
import com.dianping.puma.api.EventListener;
import com.dianping.puma.api.PumaClient;
import com.dianping.puma.biz.entity.old.SyncTask;
import com.dianping.puma.biz.entity.TaskState;
import com.dianping.puma.biz.sync.model.mapping.DatabaseMapping;
import com.dianping.puma.biz.sync.model.mapping.MysqlMapping;
import com.dianping.puma.biz.sync.model.mapping.TableMapping;
import com.dianping.puma.core.constant.Status;
import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.syncserver.config.SyncServerConfig;
import com.dianping.puma.syncserver.job.binlogmanage.BinlogManager;
import com.dianping.puma.syncserver.job.executor.exception.TEException;
import com.dianping.puma.syncserver.job.transform.Transformer;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class SyncTaskExecutor implements TaskExecutor<SyncTask> {

    protected static final Logger LOG = LoggerFactory.getLogger(SyncTaskExecutor.class);

    private boolean inited = false;

    private boolean stopped = true;

    private TEException teException = null;

    private Transformer transformer;

    protected SyncTask task;

    protected BinlogManager binlogManager;

    protected PumaClient pumaClient;

    protected Status status;

    protected String pumaTaskName;

    protected String pumaServerHost;

    protected int pumaServerPort;

    protected String pumaClientServerName;

    protected long pumaClientServerId;

    private AtomicLong delay = new AtomicLong(0L);

    private AtomicLong updates = new AtomicLong(0L);

    private AtomicLong inserts = new AtomicLong(0L);

    private AtomicLong deletes = new AtomicLong(0L);

    private AtomicLong ddls = new AtomicLong(0L);

    public SyncTaskExecutor() {
    }

    @Override
    public void init() {
        if (inited) {
            return;
        }

        binlogManager.init();
        transformer.init();
        pumaClient = createPumaClient();

        inited = true;
    }

    @Override
    public void destroy() {
        if (!inited) {
            return;
        }

        transformer.destroy();
        binlogManager.destroy();

        inited = false;
    }

    @Override
    public void start() {
        if (!stopped) {
            return;
        }

        stopped = false;

        binlogManager.start();
        transformer.start();
        pumaClient.start();
    }

    @Override
    public void stop() {
        if (stopped) {
            return;
        }

        stopped = true;

        pumaClient.stop();
        transformer.stop();
        binlogManager.stop();
    }

    @Override
    public SyncTask getTask() {
        return task;
    }

    public void asyncThrow() throws TEException {
        if (teException != null) {
            throw teException;
        }
    }

    private void execute(ChangedEvent event) throws TEException {
        try {
            transformer.transform(event);
        } catch (Exception e) {
            throw TEException.translate(e);
        }
    }

    private PumaClient createPumaClient() {
        LOG.info("Creating puma client...");
        ConfigurationBuilder configBuilder = new ConfigurationBuilder();
        configBuilder.host(pumaServerHost);
        configBuilder.port(pumaServerPort);
        configBuilder.name(pumaClientServerName);
        configBuilder.serverId(pumaClientServerId);
        configBuilder.target(pumaTaskName);
        configBuilder.dml(true);
        configBuilder.ddl(true);
        configBuilder.transaction(true);
        configBuilder.binlog(binlogManager.getBinlogInfo().getBinlogFile());
        configBuilder.binlogPos(binlogManager.getBinlogInfo().getBinlogPosition());
        _parseSourceDatabaseTables(task.getMysqlMapping(), configBuilder);
        Configuration configuration = configBuilder.build();
        LOG.info("Puma client connecting settings: {}.", configuration.toString());

        final PumaClient pumaClient = new PumaClient(configuration);
        //pumaClient.getSeqFileHolder().saveSeq(-3);

        pumaClient.register(new EventListener() {

            @Override
            public void onEvent(ChangedEvent event) {
                LOG.info("Receive event({}).", event.toString());

                status = Status.RUNNING;
                execute(event);
            }

			/*
            @Override
			public boolean onException(ChangedEvent event, Exception e) {
				if (!(e instanceof TEException)) {
					// Don't know how to deal, stop executor.
					teException = new TEException(-1, e.getMessage(), e.getCause());
					stop();
					return false;
				} else {
					// Standard exception, find solution in error code handler map.
					TEException ge = (TEException) e;
					Map<Integer, String> errorCodeHandlerMap = task.getErrorCodeHandlerNameMap();
					if (errorCodeHandlerMap == null) {
						// No error code handler map exists, stop executor.
						teException = ge;
						stop();
						return false;
					} else {
						String handlerName = errorCodeHandlerMap.get(ge.getErrorCode());
						if (handlerName == null) {
							// No error code handler name exists, stop executor.
							teException = ge;
							stop();
							return false;
						} else {
							Handler handler = HandlerContainer.getInstance().getHandler(handlerName);
							if (handler == null) {
								// No error code handler exists, stop executor.
								teException = ge;
								stop();
								return false;
							} else {
								// Handle the error.
								return false;
							}
						}
					}
				}
			}

			@Override
			public void onConnectException(Exception e) {
				String msg = String.format("Puma client connection exception in sync task(%s).", task.getName());
				teException = TEException.translate(e);
				LOG.error(msg, teException);
				Cat.logError(msg, teException);

				status = Status.RECONNECTING;
				//pumaClient.getSeqFileHolder().saveSeq(binlogManager.getSeq());

				// Sleep 60s.
				try {
					Thread.sleep(60 * 5 * 1000);
				} catch (InterruptedException e1) {
				}
			}

			@Override
			public void onConnected() {
				status = Status.CONNECTED;
			}

			@Override
			public void onSkipEvent(ChangedEvent event) {
			}*/
        });

        return pumaClient;
    }

		/*
        private boolean handleError(ChangedEvent event, Handler handler, Exception e) {
			boolean ignoreFailEvent = false;
			try {
				LOG.info("Invoke handler(" + handler.getName() + "), event : " + event);
				HandleContext context = new HandleContext();
				//context.setMysqlExecutor(mysqlExecutor);
				context.setChangedEvent(event);
				context.setTask(task);
				context.setExecutor(AbstractTaskExecutor.this);
				context.setException(e);
				context.setLastEvents(lastEvents);
				HandleResult handleResult = handler.handle(context);
				ignoreFailEvent = handleResult.isIgnoreFailEvent();
			} catch (RuntimeException re) {
				LOG.warn("Unexpected RuntimeException on handler(" + handler.getName()
						+ "), ignoreFailEvent keep false.", re);
			}
			return ignoreFailEvent;
		}*/

    /**
     * 设置同步源的数据库和表
     */
    private void _parseSourceDatabaseTables(MysqlMapping mysqlMapping, ConfigurationBuilder configBuilder) {
        List<DatabaseMapping> databases = mysqlMapping.getDatabases();
        if (databases != null) {
            for (DatabaseMapping database : databases) {
                // 解析database
                String databaseFrom = database.getFrom();
                // 解析table
                List<TableMapping> tables = database.getTables();
                if (tables != null) {
                    // 如果table中有一个是*，则只需要设置一个*；否则，添加所有table配置
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

    public void setTask(SyncTask task) {
        this.task = task;
    }

    public void setBinlogManager(BinlogManager binlogManager) {
        this.binlogManager = binlogManager;
    }

    public void setTransformer(Transformer transformer) {
        this.transformer = transformer;
    }

    public void setPumaTaskName(String pumaTaskName) {
        this.pumaTaskName = pumaTaskName;
    }

    public void setPumaServerPort(int pumaServerPort) {
        this.pumaServerPort = pumaServerPort;
    }

    public void setPumaServerHost(String pumaServerHost) {
        this.pumaServerHost = pumaServerHost;
    }

    public void setPumaClientServerName(String pumaClientServerName) {
        this.pumaClientServerName = pumaClientServerName;
    }

    public void setPumaClientServerId(long pumaClientServerId) {
        this.pumaClientServerId = pumaClientServerId;
    }

    public Status getStatus() {
        return status;
    }

    public TaskState getTaskState() {
        TaskState state = new TaskState();
        state.setStatus(this.status);
        state.setName(this.task.getName());
        state.setServerName(SyncServerConfig.getInstance().getSyncServerName());
        return state;
    }

    public BinlogManager getBinlogManager() {
        return binlogManager;
    }

    public AtomicLong getDelay() {
        return delay;
    }

    public AtomicLong getDdls() {
        return ddls;
    }

    public AtomicLong getUpdates() {
        return updates;
    }

    public AtomicLong getInserts() {
        return inserts;
    }

    public AtomicLong getDeletes() {
        return deletes;
    }
}
