package com.dianping.puma.syncserver.job.load.pool;

import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.syncserver.job.load.model.BatchRow;

import java.util.concurrent.*;

public class BatchRowPool {

	// Pool status.
	private boolean stopped = false;
	private Exception exception = null;

	// Pool size.
	private int poolSize;

	// Bottom implementation.
	private LinkedBlockingDeque<BatchRow> batchRows;

	public BatchRowPool() {}

	public void start() {
		stopped = false;
		exception = null;

		batchRows = new LinkedBlockingDeque<BatchRow>(poolSize);
	}

	public void stop() {
		stopped = true;

		batchRows.clear();
		batchRows = null;
	}

	public boolean hasException() {
		return false;
	}

	public Exception getException() {
		return exception;
	}

	/*
	public void put(ChangedEvent event) throws InterruptedException {
		if (!stopped) {
			batchRows.put(new BatchRow(event));
		}
	}*/

	public void put(ChangedEvent event) throws InterruptedException {
		if (!stopped) {
			BatchRow last = batchRows.peekLast();
			if (last == null) {
				batchRows.put(new BatchRow(event));
			} else {
				if (!last.addRow(event)) {
					batchRows.put(new BatchRow(event));
				}
			}
		}
	}

	public BatchRow take() throws InterruptedException {
		if (!stopped) {
			return batchRows.take();
		}
		return null;
	}

	public void setPoolSize(int poolSize) {
		this.poolSize = poolSize;
	}
}
