package com.dianping.puma.syncserver.job.load.pool;

import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.syncserver.job.load.exception.LoadException;
import com.dianping.puma.syncserver.job.load.row.BatchRow;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

public class WCBatchRowPool implements BatchRowPool {

	private static final Logger LOG = LoggerFactory.getLogger(WCBatchRowPool.class);

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

	public WCBatchRowPool() {
	}

	@Override
	public void init() {
		batchRows = new LinkedBlockingDeque<BatchRow>(poolSize);
	}

	@Override
	public void destroy() {
		batchRows.clear();
		batchRows = null;
	}

	@Override
	public void cleanup() {

	}

	public LoadException exception() {
		return loadException;
	}

	public void put(ChangedEvent event) throws LoadException {
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
				throw LoadException.translate(e);
			}
		}

	}

	public BatchRow take() throws LoadException {
		if (stopped) {
			LOG.error("Batch row pool({}) is already stopped.", name);
			throw new LoadException(-1, String.format("Batch row pool(%s) is already stopped.", name));
		} else {
			try {
				return batchRows.take();
			} catch (InterruptedException e) {
				throw LoadException.translate(e);
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
