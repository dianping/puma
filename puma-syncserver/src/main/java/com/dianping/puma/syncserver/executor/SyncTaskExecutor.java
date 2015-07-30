package com.dianping.puma.syncserver.executor;

import com.dianping.puma.api.PumaClient;
import com.dianping.puma.biz.entity.SyncTaskEntity;
import com.dianping.puma.core.dto.BinlogMessage;
import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.event.Event;
import com.dianping.puma.syncserver.executor.load.LoadFutureListener;
import com.dianping.puma.syncserver.executor.load.Loader;
import com.dianping.puma.syncserver.executor.load.LoaderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.concurrent.ExecutorService;

public class SyncTaskExecutor extends AbstractTaskExecutor<SyncTaskEntity> {

	private static final Logger logger = LoggerFactory.getLogger(SyncTaskExecutor.class);

	/**
	 * Mysql data source.
	 * Injected and managed by the builder.
	 */
	protected DataSource dataSource;

	/**
	 * Mysql executor thread pool.
	 * Injected and managed by the builder.
	 */
	protected ExecutorService sqlExecutorThreadPool;

	/**
	 * Puma client for subscription binlog event.
	 * Managed by the executor.
	 */
	protected PumaClient client;

	/**
	 * Mysql loader for executing binlog event.
	 * Managed by the executor.
	 */
	protected Loader loader;

	@Override
	protected void doStart() {
		initClient();
		// @todo: start the client.

		initLoader();
		loader.start();
	}

	@Override
	protected void doStop() {
		loader.stop();
		loader = null;

		// @todo: stop the client.
	}

	private void initClient() {
		// @todo: init the client.
	}

	private void initLoader() {
		loader = LoaderFactory.createAsyncConcurrentLoader(5, dataSource, sqlExecutorThreadPool);
	}

	private Runnable mainTask = new Runnable() {
		@Override
		public void run() {
			BinlogMessage binlogMessage = new BinlogMessage();

			for (Event binlogEvent: binlogMessage.getBinlogEvents()) {
				if (binlogEvent instanceof ChangedEvent) {
					consume((ChangedEvent) binlogEvent);
				}
			}
		}
	};

	private void consume(ChangedEvent binlogEvent) {
		loader.load(binlogEvent).addListener(new LoadFutureListener() {
			@Override
			public void onSuccess(Integer result) {

			}

			@Override
			public void onFailure(Throwable cause) {

			}
		});
	}
}
