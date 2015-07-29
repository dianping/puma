package com.dianping.puma.syncserver.executor;

import com.dianping.puma.api.PumaClient;
import com.dianping.puma.biz.entity.SyncTaskEntity;
import com.dianping.puma.core.dto.BinlogMessage;
import com.dianping.puma.syncserver.executor.load.Loader;
import com.dianping.puma.syncserver.executor.transform.Transformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

public class SyncTaskExecutor extends AbstractTaskExecutor<SyncTaskEntity> {

	private static final Logger logger = LoggerFactory.getLogger(SyncTaskExecutor.class);

	private String taskName;

	private SyncTaskEntity task;

	private ExecutorService bossThreadPool;
	private ExecutorService workerThreadPool;

	private PumaClient client;
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
					BinlogMessage binlogMessage = client.get(1);
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

			}
		}
	};

	public void setClient(PumaClient client) {
		this.client = client;
	}

	public void setTransformer(Transformer transformer) {
		this.transformer = transformer;
	}

	public void setLoader(Loader loader) {
		this.loader = loader;
	}
}
