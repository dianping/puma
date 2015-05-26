package com.dianping.puma.syncserver.job.executor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.dianping.puma.core.constant.Status;
import com.dianping.puma.core.entity.*;
import com.dianping.puma.syncserver.job.binlogmanage.BinlogManager;
import com.dianping.puma.syncserver.job.executor.exception.TEException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.puma.api.Configuration;
import com.dianping.puma.api.ConfigurationBuilder;
import com.dianping.puma.api.EventListener;
import com.dianping.puma.api.PumaClient;
import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.sync.model.mapping.DatabaseMapping;
import com.dianping.puma.core.sync.model.mapping.MysqlMapping;
import com.dianping.puma.core.sync.model.mapping.TableMapping;
import com.dianping.puma.syncserver.job.executor.failhandler.Handler;
import com.dianping.puma.syncserver.job.executor.failhandler.HandlerContainer;

public abstract class AbstractTaskExecutor<T extends AbstractBaseSyncTask> implements TaskExecutor<T> {

	private static final Logger LOG = LoggerFactory.getLogger(AbstractTaskExecutor.class);

	private boolean stopped = true;

	private TEException teException;

	protected T task;

	protected BinlogManager binlogManager;

	protected PumaClient pumaClient;

	protected Status status;

	protected String pumaTaskName;

	protected String pumaServerHost;

	protected int pumaServerPort;

	protected String pumaClientServerName;

	protected long pumaClientServerId;

	public AbstractTaskExecutor() {}

	protected abstract void execute(ChangedEvent event) throws TEException;

	public void setTask(T task) {
		this.task = task;
	}

	@Override
	public void init() {

	}

	@Override
	public void start() {
		stopped = false;
		teException = null;

		// 1. Start binlogManager.
		binlogManager.start();

		// 2. Start detailed modules.
		doStart();

		// 3. Start the puma client.
		pumaClient = createPumaClient();
		pumaClient.start();
	}

	@Override
	public void stop() {
		stopped = true;

		doStop();

		pumaClient.stop();

		binlogManager.stop();
	}

	@Override
	public T getTask() {
		return task;
	}

	@Override
	public void cleanup() {
		binlogManager.cleanup();
	}

	protected abstract void doStart();

	protected abstract void doStop();

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
		pumaClient.getSeqFileHolder().saveSeq(-3);

		pumaClient.register(new EventListener() {

			@Override
			public void onEvent(ChangedEvent event) throws Exception {
				execute(event);
			}

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
				LOG.error("Puma client({}) connection exception occurs: {}.", task.getName(), e.getMessage());
				status = Status.RECONNECTING;

				// Sleep 60s.
				try {
					Thread.sleep(60000L);
				} catch (InterruptedException e1) {
					teException = new TEException(-1, e1.getMessage(), e1.getCause());
					stop();
				}
			}

			@Override
			public void onConnected() {
				LOG.info("Puma client({}) connected.", task.getName());
				status = Status.CONNECTED;
			}

			@Override
			public void onSkipEvent(ChangedEvent event) {
				LOG.info("Puma client({}) skip event({}).", task.getName(), event.toString());
			}
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

	@Override
	public String toString() {
		return "AbstractTaskExecutor{" +
				"stopped=" + stopped +
				", gException=" + teException +
				", task=" + task +
				", binlogManager=" + binlogManager +
				", pumaClient=" + pumaClient +
				", status=" + status +
				'}';
	}

	public void setBinlogManager(BinlogManager binlogManager) {
		this.binlogManager = binlogManager;
	}

	public Status getStatus() {
		return status;
	}

	public BinlogManager getBinlogManager() {
		return binlogManager;
	}



	public Exception getException() {
		return new Exception(teException.getErrorDesc());
	}
}
