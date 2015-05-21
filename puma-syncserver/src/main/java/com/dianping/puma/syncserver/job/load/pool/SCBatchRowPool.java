package com.dianping.puma.syncserver.job.load.pool;

import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.event.RowChangedEvent;
import com.dianping.puma.syncserver.job.load.exception.LoadException;
import com.dianping.puma.syncserver.job.load.row.BatchRow;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

public class SCBatchRowPool {

	private static final Logger LOG = LoggerFactory.getLogger(BatchRowPool.class);

	/** Pool stopped or not, default is true. */
	private boolean stopped = true;

	/** Pool exception, default is null. */
	private LoadException loadException = null;

	/** Current transaction state, -1 for begin, 0 for in, 1 for commit. */
	int transaction = -1;

	/** Pool optional settings: pool name, default to "SCBatchRowPool-XXXXX". */
	private String name = "SCBatchRowPool-" + RandomStringUtils.randomAlphabetic(5);

	/** Pool optional settings: pool size, default to 100. */
	private int poolSize = 100;

	/** Pool batch rows storage. */
	protected BlockingDeque<BatchRow> batchRows;

	public SCBatchRowPool() {}

	public void start() {
		LOG.info("Starting strong consistency batch row pool({})...", name);

		if (!stopped) {
			LOG.warn("Strong consistency batch row pool({}) is already started.", name);
		} else {
			// Clear batch row pool status.
			stopped = false;
			loadException = null;

			// Initialize blocking queue.
			if (batchRows == null) {
				batchRows = new LinkedBlockingDeque<BatchRow>(poolSize);
			}
		}
	}

	public void stop() {
		LOG.info("Stopping strong consistency batch row pool({})...", name);

		if (stopped) {
			LOG.warn("Strong consistency batch row pool({}) is already stopped.", name);
		} else {
			// Clear batch row pool status.
			stopped = true;

			// Destroy blocking queue.
			batchRows.clear();
			batchRows = null;
		}
	}

	public void destroy() {
		// No persistent storage.
	}

	public LoadException exception() {
		return loadException;
	}

	public void put(ChangedEvent event) {
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
			throw LoadException.handleException(e);
		}
	}

	public BatchRow take() {
		try {
			return batchRows.take();
		} catch (InterruptedException e) {
			throw LoadException.handleException(e);
		}
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setPoolSize(int poolSize) {
		this.poolSize = poolSize;
	}
}
