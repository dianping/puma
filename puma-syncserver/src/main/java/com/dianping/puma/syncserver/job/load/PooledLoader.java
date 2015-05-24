package com.dianping.puma.syncserver.job.load;

import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.monitor.EventMonitor;
import com.dianping.puma.syncserver.job.binlogmanage.BinlogManager;
import com.dianping.puma.syncserver.job.load.exception.LoadException;
import com.dianping.puma.syncserver.job.load.pool.*;
import com.dianping.puma.syncserver.job.load.row.BatchRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PooledLoader implements Loader {

	private static final Logger LOG = LoggerFactory.getLogger(PooledLoader.class);

	/**
	 * Pooled loader stopped or not, default true.
	 */
	private boolean stopped = true;

	/**
	 * Pooled loader exception, default null.
	 */
	private LoadException loadException = null;

	/**
	 * Pooled loader main loop thread. Read rows from batch row pool and put it into batch exec pool.
	 */
	private Thread loopThread;

	/**
	 * Pooled loader batch exec pool, for executing events.
	 */
	private BatchExecPool batchExecPool;

	/**
	 * Pooled loader batch exec pool size, default 1.
	 */
	private int batchExecPoolSize = 1;

	/**
	 * Pooled loader batch row pool, for smashing events.
	 */
	private BatchRowPool batchRowPool;

	/**
	 * Pooled loader batch row pool size, default 100.
	 */
	private int batchRowPoolSize = 100;

	/**
	 * Pooled loader sql retries, default 1.
	 */
	private int retries = 1;

	/**
	 * Pooled loader title.
	 */
	private String title = "Loader-";

	/**
	 * Pooled loader name.
	 */
	private String name;

	/**
	 * Pooled loader in strong consistency or not, default true.
	 */
	private boolean strongConsistency = true;

	/**
	 * Pooled loader binlog manager.
	 */
	private BinlogManager binlogManager;

	/**
	 * Pooled loader JDBC connection host.
	 */
	private String host;

	/**
	 * Pooled loader JDBC connection username.
	 */
	private String username;

	/**
	 * Pooled loader JDBC connection password.
	 */
	private String password;

	// Monitor.
	private EventMonitor loadEventMonitor = new EventMonitor("EventCount.load", 1L);

	public PooledLoader() {}

	@Override
	public void start() {
		LOG.info("Starting loader({})...", title + name);
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

	@Override
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

	@Override
	public void asyncThrow() throws LoadException {
		if (loadException != null) {
			throw loadException;
		}
	}

	@Override
	public void load(ChangedEvent event) throws LoadException {
		// Record total event loaded.
		loadEventMonitor.record(event.genFullName(), "0");
		batchRowPool.put(event);
	}

	private void initBatchRowPool() {
		if (strongConsistency) {
			SCBatchRowPool scBatchRowPool = new SCBatchRowPool();
			scBatchRowPool.setName(name);
			scBatchRowPool.setPoolSize(batchRowPoolSize);
			batchRowPool = scBatchRowPool;
		} else {
			WCBatchRowPool wcBatchRowPool = new WCBatchRowPool();
			wcBatchRowPool.setName(name);
			wcBatchRowPool.setPoolSize(batchRowPoolSize);
			batchRowPool = wcBatchRowPool;
		}
	}

	private void initBatchExecPool() {
		if (strongConsistency) {
			SCBatchExecPool scBatchExecPool = new SCBatchExecPool();
			scBatchExecPool.setHost(host);
			scBatchExecPool.setUsername(username);
			scBatchExecPool.setPassword(password);
			scBatchExecPool.setPoolSize(1);
			scBatchExecPool.setRetires(retries);
			scBatchExecPool.setBinlogManager(binlogManager);
		} else {
			WCBatchExecPool wcBatchExecPool = new WCBatchExecPool();
			wcBatchExecPool.setHost(host);
			wcBatchExecPool.setPassword(password);
			wcBatchExecPool.setPoolSize(batchExecPoolSize);
			wcBatchExecPool.setRetires(retries);
			wcBatchExecPool.setBinlogManager(binlogManager);
		}
	}

	private void initLoopThread() {
		loopThread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					loop();
				} catch (LoadException e) {
					if (!stopped) {
						LOG.error("Pooled loader loop thread failure.", e);
						loadException = e;
						stop();
					}
				}
			}
		});
	}

	private void loop() {
		while (true) {
			// Take batch row from the batch row pool.
			BatchRow batchRow = batchRowPool.take();

			// Put batch row into the batch exec pool.
			batchExecPool.asyncThrow();
			batchExecPool.put(batchRow);
		}
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setBinlogManager(BinlogManager binlogManager) {
		this.binlogManager = binlogManager;
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

	public void setStrongConsistency(boolean strongConsistency) {
		this.strongConsistency = strongConsistency;
	}

	public void setBatchRowPoolSize(int batchRowPoolSize) {
		this.batchRowPoolSize = batchRowPoolSize;
	}

	public void setBatchExecPoolSize(int batchExecPoolSize) {
		this.batchExecPoolSize = batchExecPoolSize;
	}

	public void setRetries(int retries) {
		this.retries = retries;
	}
}
