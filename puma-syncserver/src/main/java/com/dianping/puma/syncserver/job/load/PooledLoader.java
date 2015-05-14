package com.dianping.puma.syncserver.job.load;

import com.dianping.cat.Cat;
import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.monitor.EventMonitor;
import com.dianping.puma.syncserver.job.BinlogInfoManager;
import com.dianping.puma.syncserver.job.load.exception.LoadException;
import com.dianping.puma.syncserver.job.load.model.BatchExecPool;
import com.dianping.puma.syncserver.job.load.model.BatchRow;
import com.dianping.puma.syncserver.job.load.model.BatchRowPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PooledLoader implements Loader {

	private static final Logger LOG = LoggerFactory.getLogger(PooledLoader.class);

	private String name = "default";

	private boolean stopped = true;

	private LoadException loadException = null;

	private Thread loopThread;

	private BatchExecPool batchExecPool;

	private BatchRowPool batchRowPool;

	private BinlogInfoManager binlogInfoManager;

	// Monitor.
	private EventMonitor loadEventMonitor = new EventMonitor("EventCount.load", 1L);

	// JDBC connection settings.
	private String host;
	private String username;
	private String password;

	public PooledLoader(String host, String username, String password, BinlogInfoManager binlogInfoManager) {
		this.host = host;
		this.username = username;
		this.password = password;
		this.binlogInfoManager = binlogInfoManager;
	}

	public void start() {
		LOG.info("PooledLoader({}) is starting.", name);

		stopped = false;
		loadException = null;

		initBatchRowPool();
		batchRowPool.start();

		initBatchExecPool();
		batchExecPool.start();

		loadEventMonitor.start();

		initLoopThread();
		loopThread.start();
	}

	public void stop() {
		LOG.info("PooledLoader({}) is stopping.", name);

		stopped = true;

		batchRowPool.stop();
		batchRowPool = null;

		batchExecPool.stop();
		batchExecPool = null;

		loadEventMonitor.stop();

		loopThread.interrupt();
		loopThread = null;
	}

	public void load(ChangedEvent event) {
		if (!stopped) {
			try {
				LOG.info("load");
				Cat.logEvent("EventCount.load", "default");
				batchRowPool.put(event);
			} catch (InterruptedException e) {
				if (!stopped) {
					handleException(e);
				}
			}
		}
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
					// @TODO Log error.
				}
				break;
			}
		}
	}

	private void handleException(Exception e) {
		LOG.error("error: {}.", e.getStackTrace());
		stop();
	}
}
