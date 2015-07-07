package com.dianping.puma.syncserver.task;

import com.dianping.puma.api.PumaClient;
import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.syncserver.accept.DuplexBuffer;
import com.dianping.puma.syncserver.load.LoadFuture;
import com.dianping.puma.syncserver.load.Loader;
import com.dianping.puma.syncserver.transform.Transformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

public class SyncTaskExecutor extends AbstractTaskExecutor {

	private static final Logger logger = LoggerFactory.getLogger(SyncTaskExecutor.class);

	private String taskName;

	private ScheduledExecutorService BossThreadPool;
	private ScheduledExecutorService WorkerThreadPool;
	private ScheduledExecutorService SQLThreadPool;
	private BlockingQueue<LoadFuture> loadFutureBlockingQueue;

	private PumaClient client;
	private DuplexBuffer duplexBuffer;
	private Transformer transformer;
	private Loader loader;

	@Override
	public void doStart() {

	}

	@Override
	public void doStop() {

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
					// @todo.
				} catch (ExecutionException e) {
					// @todo.
				}
			}
		}
	};
}
