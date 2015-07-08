package com.dianping.puma.syncserver.task;

import com.dianping.puma.api.PumaClient;
import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.syncserver.buffer.DuplexBuffer;
import com.dianping.puma.syncserver.exception.PumaTimeoutException;
import com.dianping.puma.syncserver.load.LoadFuture;
import com.dianping.puma.syncserver.load.Loader;
import com.dianping.puma.syncserver.transform.Transformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

public class SyncTaskExecutor extends AbstractTaskExecutor {

	private static final Logger logger = LoggerFactory.getLogger(SyncTaskExecutor.class);

	private String taskName;

	private ExecutorService bossThreadPool;
	private ExecutorService workerThreadPool;

	private PumaClient client;
	private DuplexBuffer duplexBuffer;
	private Transformer transformer;
	private Loader loader;

	@Override
	public void doStart() {
		startWorker();
		startBoss();
	}

	@Override
	public void doStop() {
		stopWorker();
		stopBoss();
	}

	private void startBoss() {
		bossThreadPool = Executors.newFixedThreadPool(3, new ThreadFactory() {
			@Override
			public Thread newThread(Runnable runnable) {
				Thread thread = new Thread(runnable);
				thread.setDaemon(true);
				thread.setName("puma-boss");
				return thread;
			}
		});

		bossThreadPool.execute(listenTask);
		bossThreadPool.execute(handleTask);
		bossThreadPool.execute(commitTask);
	}

	private void stopBoss() {
		bossThreadPool.shutdown();
		bossThreadPool = null;
	}

	private void startWorker() {
		workerThreadPool = Executors.newFixedThreadPool(10, new ThreadFactory() {
			@Override
			public Thread newThread(Runnable runnable) {
				Thread thread = new Thread(runnable);
				thread.setDaemon(true);
				thread.setName("puma-boss");
				return thread;
			}
		});
	}

	private void stopWorker() {
		workerThreadPool.shutdown();
		workerThreadPool = null;
	}

	private Runnable listenTask = new Runnable() {
		@Override
		public void run() {
			while (!checkStop()) {
				try {
					ChangedEvent binlogEvent = client.get();
					if (binlogEvent != null) {
						duplexBuffer.putQuery(binlogEvent);
					}
				} catch (Exception e) {
					// @todo
				}
			}
		}
	};

	private Runnable handleTask = new Runnable() {
		@Override
		public void run() {
			while (!checkStop()) {
				try {
					ChangedEvent binlogEvent = duplexBuffer.pollQuery();
					binlogEvent = transformer.transform(binlogEvent);
					LoadFuture loadFuture = loader.load(binlogEvent);
					duplexBuffer.putResult(loadFuture);
				} catch (Exception e) {
					// @todo
				}
			}
		}
	};

	private Runnable commitTask = new Runnable() {
		@Override
		public void run() {
			while (!checkStop()) {
				try {
					LoadFuture loadFuture = duplexBuffer.pollResult();
					loadFuture.get(1000, TimeUnit.MILLISECONDS);
				} catch (CancellationException e) {
					// do nothing.
				} catch (InterruptedException e) {
					// do nothing.
				} catch (TimeoutException e) {
					fail("binlog execution timeout.", new PumaTimeoutException(e));
				} catch (ExecutionException e) {
					fail("binlog execution error.", e.getCause());
				}
			}
		}
	};
}
