package com.dianping.puma.syncserver.job.load.pool;

import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.syncserver.job.LifeCycle;
import com.dianping.puma.syncserver.job.load.exception.LoadException;
import com.dianping.puma.syncserver.job.load.model.BatchRow;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

public class BatchRowPool implements LifeCycle<LoadException> {

	private static final Logger LOG = LoggerFactory.getLogger(BatchRowPool.class);

	/** Pool stopped or not, default is true. */
	private boolean stopped = true;

	/** Pool exception, default is null. */
	private LoadException loadException = null;

	/** Current transaction state, -1 for begin, 0 for in, 1 for commit. */
	int transaction = -1;

	// Pool optional settings.
	private String name = "BatchRowPool-" + RandomStringUtils.randomAlphabetic(5);

	private int poolSize = 100;

	private boolean isStrongConsistency = true;

	private LinkedBlockingDeque<BatchRow> batchRows;

	public BatchRowPool() {
	}

	public void start() {
		LOG.info("Starting batch row pool({})...", name);

		if (!stopped) {
			LOG.warn("Batch row pool({}) is already started.", name);
		} else {
			// Clear batch row pool status.
			stopped = false;
			loadException = null;

			// Initialize blocking queue.
			batchRows = new LinkedBlockingDeque<BatchRow>(poolSize);
		}
	}

	public void stop() {
		LOG.info("Stopping batch row pool({})...", name);

		if (stopped) {
			LOG.warn("Batch row pool({}) is already stopped.", name);
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
		if (stopped) {
			LOG.error("Batch row pool({}) is already stopped.", name);
			throw new LoadException(-1, String.format("Batch row pool(%s) is already stopped.", name));
		} else {
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

	}

	public BatchRow take() throws InterruptedException {
		if (stopped) {
			LOG.error("Batch row pool({}) is already stopped.", name);
			throw new LoadException(-1, String.format("Batch row pool(%s) is already stopped.", name));
		} else {
			try {
				return batchRows.take();
			} catch (InterruptedException e) {
				throw LoadException.handleException(e);
			}
		}
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setStrongConsistency(boolean isStrongConsistency) {
		this.isStrongConsistency = isStrongConsistency;
	}

	public void setPoolSize(int poolSize) {
		this.poolSize = poolSize;
	}
}
