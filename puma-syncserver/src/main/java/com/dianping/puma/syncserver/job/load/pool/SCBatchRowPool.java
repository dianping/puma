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

	private boolean inited = false;

	int transaction = -1;

	private int poolSize = 100;

	protected BlockingDeque<BatchRow> batchRows;

	public SCBatchRowPool() {}

	@Override
	public void init() {
		if (inited) {
			return;
		}

		transaction = -1;
		batchRows = new LinkedBlockingDeque<BatchRow>(poolSize);

		inited = true;
	}

	@Override
	public void destroy() {
		if (!inited) {
			return;
		}

		batchRows.clear();
		batchRows = null;

		inited = false;
	}

	@Override
	public void start() {

	}

	@Override
	public void stop() {

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
			case 1:
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
			throw LoadException.translate(e);
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

	public void setPoolSize(int poolSize) {
		this.poolSize = poolSize;
	}
}
