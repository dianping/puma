package com.dianping.puma.syncserver.job.load.pool;

import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.event.RowChangedEvent;
import com.dianping.puma.syncserver.job.load.exception.LoadException;
import com.dianping.puma.syncserver.job.load.row.BatchRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

public class SCBatchRowPool implements BatchRowPool {

	private static final Logger LOG = LoggerFactory.getLogger(SCBatchRowPool.class);

	/** Pool stopped or not, default true. */
	private boolean stopped = true;

	/** Pool exception, default null. */
	private LoadException loadException = null;

	/** Current transaction state, -1 for begin, 0 for in, 1 for commit. */
	int transaction = -1;

	/** Batch row pool title. */
	private String title = "BatchRowPool-";

	/** Batch row pool name. */
	private String name;

	/** Batch row pool size, default 100. */
	private int poolSize = 100;

	/** Batch row pool bottom storage. */
	protected BlockingDeque<BatchRow> batchRows;

	public SCBatchRowPool() {}

	@Override
	public void start() {
		LOG.info("Starting strong consistency batch row pool({})...", title + name);

		if (!stopped) {
			LOG.warn("Strong consistency batch row pool({}) is already started.", title + name);
		} else {
			stopped = false;
			loadException = null;

			if (batchRows == null) {
				batchRows = new LinkedBlockingDeque<BatchRow>(poolSize);
			}
		}
	}

	@Override
	public void stop() {
		LOG.info("Stopping strong consistency batch row pool({})...", title + name);

		if (stopped) {
			LOG.warn("Strong consistency batch row pool({}) is already stopped.", title + name);
		} else {
			stopped = true;

			batchRows.clear();
			batchRows = null;
		}
	}

	@Override
	public void die() {
		LOG.info("Dieing strong consistency batch row pool({})...", title + name);

		// Batch row pool contains no persistent storage, just stop it.
		stop();
	}

	@Override
	public LoadException exception() {
		return loadException;
	}

	@Override
	public void put(ChangedEvent event) throws LoadException {
		if (event instanceof RowChangedEvent) {
			RowChangedEvent row = (RowChangedEvent) event;

			switch (transaction) {
			case -1:
				if (!row.isTransactionBegin() && !row.isTransactionCommit()) {
					batch(event);
				}
				break;

			case 0:
				if (!row.isTransactionBegin()) {
					batch(event);
				}
				break;

			case 1: // Fall through.
			default:
				break;
			}

			if (row.isTransactionBegin()) {
				transaction = -1;
			} else if (row.isTransactionCommit()) {
				transaction = 1;
			} else {
				transaction = 0;
			}

		} else {
			batch(event);
		}
	}

	@Override
	public BatchRow take() throws LoadException {
		try {
			return batchRows.take();
		} catch (InterruptedException e) {
			loadException = LoadException.translate(e);
			throw loadException;
		}
	}

	private void batch(ChangedEvent event) {
		try {
			BatchRow last = batchRows.peekLast();
			if (last == null) {
				batchRows.put(new BatchRow(event));
			} else {
				if (!last.addRow(event)) {
					batchRows.put(new BatchRow(event));
				}
			}
		} catch (InterruptedException e) {
			throw LoadException.translate(e);
		}
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setPoolSize(int poolSize) {
		this.poolSize = poolSize;
	}
}
