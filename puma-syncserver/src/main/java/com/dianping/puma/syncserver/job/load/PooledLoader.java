package com.dianping.puma.syncserver.job.load;

import com.dianping.cat.Cat;
import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.monitor.EventMonitor;
import com.dianping.puma.syncserver.job.binlogmanage.BinlogManager;
import com.dianping.puma.syncserver.job.load.exception.LoadException;
import com.dianping.puma.syncserver.job.load.pool.*;
import com.dianping.puma.syncserver.job.load.row.BatchRow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicLong;

public class PooledLoader implements Loader {

	private static final Logger LOG = LoggerFactory.getLogger(PooledLoader.class);

	private boolean inited = false;

	private boolean stopped = true;

	/**
	 * Pooled loader exception, default null.
	 */
	private LoadException loadException = null;

	/**
	 * Pooled loader main loop thread. Read rows from batch row pool and put it into batch exec pool.
	 */
	private Thread mainThread;

	/**
	 * Pooled loader batch row pool, for smashing events.
	 */
	private BatchRowPool batchRowPool;

	/**
	 * Pooled loader batch row pool size, default 100.
	 */
	private int batchRowPoolSize = 100;

	/**
	 * Pooled loader batch exec pool, for executing events.
	 */
	private BatchExecPool batchExecPool;

	/**
	 * Pooled loader batch exec pool size, default 1.
	 */
	private int batchExecPoolSize = 1;

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
	private boolean consistent = true;

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

	/** Binlog event delays statistics. */
	private AtomicLong delay;

	/** Update statistics. */
	private AtomicLong updates;

	/** Insert statistics. */
	private AtomicLong inserts;

	/** Delete statistics. */
	private AtomicLong deletes;

	/** DDL statistics. */
	private AtomicLong ddls;

	// Monitor.
	private EventMonitor loadEventMonitor = new EventMonitor("EventCount.load", 1L);

	public PooledLoader() {}

	@Override
	public void init() {
		if (inited) {
			return;
		}

		initBatchRowPool();
		initBatchExecPool();
		mainThread = new Thread(new MainWorker());
	}

	@Override
	public void destroy() {

	}

	@Override
	public void start() {
		if (!stopped) {
			return;
		}

		stopped = false;
		loadException = null;

		loadEventMonitor.start();
		batchExecPool.start();
		mainThread.start();
	}

	@Override
	public void stop() {
		if (stopped) {
			return;
		}

		stopped = true;

		mainThread.interrupt();
		batchExecPool.stop();
		loadEventMonitor.stop();
	}

	@Override
	public void cleanup() {

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
		if (consistent) {
			SCBatchRowPool scBatchRowPool = new SCBatchRowPool();
			scBatchRowPool.setPoolSize(batchRowPoolSize);
			batchRowPool = scBatchRowPool;
		} else {
			WCBatchRowPool wcBatchRowPool = new WCBatchRowPool();
			wcBatchRowPool.setPoolSize(batchRowPoolSize);
			batchRowPool = wcBatchRowPool;
		}
		batchRowPool.init();
	}

	private void initBatchExecPool() {
		if (consistent) {
			SCBatchExecPool scBatchExecPool = new SCBatchExecPool();
			scBatchExecPool.setHost(host);
			scBatchExecPool.setUsername(username);
			scBatchExecPool.setPassword(password);
			scBatchExecPool.setPoolSize(1);
			scBatchExecPool.setRetires(retries);
			scBatchExecPool.setBinlogManager(binlogManager);
			scBatchExecPool.setDelay(delay);
			scBatchExecPool.setUpdates(updates);
			scBatchExecPool.setInserts(inserts);
			scBatchExecPool.setDeletes(deletes);
			scBatchExecPool.setDdls(ddls);
			batchExecPool = scBatchExecPool;
		} else {
			WCBatchExecPool wcBatchExecPool = new WCBatchExecPool();
			wcBatchExecPool.setHost(host);
			wcBatchExecPool.setPassword(password);
			wcBatchExecPool.setPoolSize(batchExecPoolSize);
			wcBatchExecPool.setRetires(retries);
			wcBatchExecPool.setBinlogManager(binlogManager);
			batchExecPool = wcBatchExecPool;
		}
		batchExecPool.init();
	}

	private class MainWorker implements Runnable {

		@Override
		public void run() {
			try {
				while (!stopped) {
					// Take batch row from the batch row pool.
					BatchRow batchRow = batchRowPool.take();

					// Put batch row into the batch exec pool.
					batchExecPool.asyncThrow();
					batchExecPool.put(batchRow);
				}
			} catch (LoadException e) {
				if (!stopped) {
					String msg = String.format("Loader main thread error.");
					loadException = e;
					LOG.error(msg, loadException);
					Cat.logError(msg, loadException);
					stop();
				}
			}
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

	public void setConsistent(boolean consistent) {
		this.consistent = consistent;
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

	public void setDelay(AtomicLong delay) {
		this.delay = delay;
	}

	public void setUpdates(AtomicLong updates) {
		this.updates = updates;
	}

	public void setInserts(AtomicLong inserts) {
		this.inserts = inserts;
	}

	public void setDeletes(AtomicLong deletes) {
		this.deletes = deletes;
	}

	public void setDdls(AtomicLong ddls) {
		this.ddls = ddls;
	}
}
