package com.dianping.puma.syncserver.job.executor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.dianping.puma.core.entity.AbstractBaseSyncTask;
import com.dianping.puma.core.entity.DstDBInstance;
import com.dianping.puma.core.entity.PumaServer;
import com.dianping.puma.core.entity.PumaTask;
import com.dianping.puma.core.model.state.BaseSyncTaskState;
import com.dianping.puma.syncserver.job.binlogmanage.BinlogManager;
import com.dianping.puma.syncserver.job.binlogmanage.MapDBBinlogManager;
import com.dianping.puma.syncserver.job.executor.exception.GException;
import com.dianping.puma.syncserver.job.executor.status.Status;
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
	private GException gException;

	protected T task;

	protected PumaTask pumaTask;

	protected PumaServer pumaServer;

	protected DstDBInstance dstDBInstance;

	protected BinlogManager binlogManager;

	protected PumaClient pumaClient;

	protected Status status;

	public AbstractTaskExecutor() {}

	protected abstract void execute(ChangedEvent event) throws GException;

	public void setTask(T task) {
		this.task = task;
	}

	@Override
	public T getTask() {
		return task;
	}

	@Override
	public void start() {
		stopped = false;
		gException = null;

		// 1. Start binlogManager.
		MapDBBinlogManager mapDBBinlogManager = new MapDBBinlogManager();
		mapDBBinlogManager.setName(task.getName());
		binlogManager = mapDBBinlogManager;
		binlogManager.start();

		// 2. Start detailed modules.
		doStart();

		// 3. Start the puma client.
		createPumaClient();
		pumaClient.start();
	}

	@Override
	public void stop() {
		doStop();

		pumaClient.stop();

		binlogManager.stop();

		stopped = true;
	}

	protected abstract void doStart();

	protected abstract void doStop();

	private PumaClient createPumaClient() {
		LOG.info("Creating puma client...");
		ConfigurationBuilder configBuilder = new ConfigurationBuilder();
		configBuilder.host(pumaServer.getHost());
		configBuilder.port(pumaServer.getPort());
		configBuilder.name(task.getPumaClientName());
		configBuilder.serverId(task.getPumaClientServerId());
		configBuilder.target(pumaTask.getName());
		configBuilder.dml(true);
		configBuilder.ddl(true);
		configBuilder.transaction(false);
		_parseSourceDatabaseTables(task.getMysqlMapping(), configBuilder);
		Configuration configuration = configBuilder.build();
		LOG.info("Puma client connecting settings: {}.", configuration.toString());

		final PumaClient pumaClient = new PumaClient(configuration);
		pumaClient.register(new EventListener() {

			@Override
			public void onEvent(ChangedEvent event) throws Exception {
				execute(event);
			}

			@Override
			public boolean onException(ChangedEvent event, Exception e) {
				if (!(e instanceof GException)) {
					// Don't know how to deal, stop executor.
					gException = new GException(-1, e.getMessage(), e.getCause());
					stop();
					return false;
				} else {
					// Standard exception, find solution in error code handler map.
					GException ge = (GException) e;
					Map<Integer, String> errorCodeHandlerMap = task.getErrorCodeHandlerNameMap();
					if (errorCodeHandlerMap == null) {
						// No error code handler map exists, stop executor.
						gException = ge;
						stop();
						return false;
					} else {
						String handlerName = errorCodeHandlerMap.get(ge.getErrorCode());
						if (handlerName == null) {
							// No error code handler name exists, stop executor.
							gException = ge;
							stop();
							return false;
						} else {
							Handler handler = HandlerContainer.getInstance().getHandler(handlerName);
							if (handler == null) {
								// No error code handler exists, stop executor.
								gException = ge;
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

				// Sleep 60s.
				try {
					Thread.sleep(60000L);
				} catch (InterruptedException e1) {
					gException = new GException(-1, e1.getMessage(), e1.getCause());
					stop();
				}
			}

			@Override
			public void onConnected() {
				LOG.info("Puma client({}) connected.", task.getName());
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
				", gException=" + gException +
				", task=" + task +
				", pumaTask=" + pumaTask +
				", pumaServer=" + pumaServer +
				", dstDBInstance=" + dstDBInstance +
				", binlogManager=" + binlogManager +
				", pumaClient=" + pumaClient +
				", status=" + status +
				'}';
	}

	public void setPumaTask(PumaTask pumaTask) {
		this.pumaTask = pumaTask;
	}

	public void setPumaServer(PumaServer pumaServer) {
		this.pumaServer = pumaServer;
	}

	public void setDstDBInstance(DstDBInstance dstDBInstance) {
		this.dstDBInstance = dstDBInstance;
	}

	public void setBinlogManager(BinlogManager binlogManager) {
		this.binlogManager = binlogManager;
	}
}
