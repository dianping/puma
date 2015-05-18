package com.dianping.puma.syncserver.job.load;

import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.monitor.EventMonitor;
import com.dianping.puma.syncserver.job.binlog.BinlogInfoManager;
import com.dianping.puma.syncserver.job.load.exception.LoadException;
import com.dianping.puma.syncserver.job.load.model.BatchExecPool;
import com.dianping.puma.syncserver.job.load.model.BatchRow;
import com.dianping.puma.syncserver.job.load.model.BatchRowPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

public class PooledLoader implements Loader {

	private static final Logger LOG = LoggerFactory.getLogger(PooledLoader.class);

	private boolean stopped = true;

	private LoadException loadException = null;

	// PooledLoader main modules.
	private Thread loopThread;
	private BatchExecPool batchExecPool;
	private BatchRowPool batchRowPool;

	private String name;

	private BinlogInfoManager binlogInfoManager;

	// JDBC connection settings.
	private String host;
	private String username;
	private String password;

	// Monitor.
	private EventMonitor loadEventMonitor = new EventMonitor("EventCount.load", 1L);

	public PooledLoader() {}

	public void start() {
		LOG.info("PooledLoader({}) is starting.", name);
		stopped = false;
		loadException = null;

		// Start monitors.
		loadEventMonitor.start();

		// Start batchExecPool.
		initBatchExecPool();
		batchExecPool.start();

		// Start batchRowPool.
		initBatchRowPool();
		batchRowPool.start();

		// Start the main loop thread.
		initLoopThread();
		loopThread.start();
	}

	public void stop() {
		LOG.info("PooledLoader({}) is stopping.", name);
		stopped = true;

		// Stop the main loop thread.
		loopThread.interrupt();
		loopThread = null;

		// Stop the batchRowPool.
		batchRowPool.stop();
		batchRowPool = null;

		// Stop the batchExecPool.
		batchExecPool.stop();
		batchExecPool = null;

		// Stop the monitor.
		loadEventMonitor.stop();
	}

	public LoadException exception() {
		return loadException;
	}

	private void initBatchExecPool() {
		batchExecPool = new BatchExecPool();
		batchExecPool.setHost(host);
		batchExecPool.setUsername(username);
		batchExecPool.setPassword(password);
		batchExecPool.setPoolSize(5);
		batchExecPool.setRetires(1);
	}

	private void initBatchRowPool() {
		batchRowPool = new BatchRowPool();
		batchRowPool.setPoolSize(30);
	}

	private void initLoopThread() {
		loopThread = new Thread(new Runnable() {
			@Override
			public void run() { loop(); }
		});
	}

	public void load(ChangedEvent event) throws LoadException {
		if (stopped) {
			LOG.error("Loader({}) is stopped for event({}).", name, event.toString());
			throw new LoadException(0, String.format("Loader(%s) is stopped for event(%s).", name, event.toString()));
		}

		try {
			loadEventMonitor.record(event.genFullName(), "0");
			batchRowPool.put(event);
		} catch (InterruptedException e) {
			if (!stopped) {
				LOG.error("Loader({}) loads event failure for event({}).", name, event.toString());
				handleException(e);
			}
		}
	}

	private void loop() {
		for (;;) {
			try {
				if (batchRowPool.hasException()) {
					handleException(batchExecPool.getException());
					break;
				}
				BatchRow batchRow = batchRowPool.take();

				if (batchExecPool.hasException()) {
					handleException(batchExecPool.getException());
					break;
				}
				batchExecPool.put(batchRow);

			} catch (InterruptedException e) {
				if (!stopped) {
					LOG.error("Loader({}) loops failure.", name);
					handleException(e);
				}
				break;
			}
		}
	}

	private void handleException(Exception e) {
		if (e instanceof InterruptedException) {
			loadException = new LoadException(1, e.getMessage(), e.getCause());
		} else if (e instanceof SQLException) {
			loadException = new LoadException(((SQLException) e).getErrorCode(), e.getMessage(), e.getCause());
		} else {
			loadException = new LoadException(-1, e.getMessage(), e.getCause());
		}

		// Stop the loader.
		stop();
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setBinlogInfoManager(BinlogInfoManager binlogInfoManager) {
		this.binlogInfoManager = binlogInfoManager;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
