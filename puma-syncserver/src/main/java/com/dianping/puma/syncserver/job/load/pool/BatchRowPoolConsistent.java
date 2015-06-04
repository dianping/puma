package com.dianping.puma.syncserver.job.load.pool;

import com.dianping.cat.Cat;
import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.event.DdlEvent;
import com.dianping.puma.core.event.RowChangedEvent;
import com.dianping.puma.syncserver.job.load.exception.LoadException;
import com.dianping.puma.syncserver.job.load.row.BatchRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

public class BatchRowPoolConsistent implements BatchRowPool {

	private static final Logger LOG = LoggerFactory.getLogger(BatchRowPoolConsistent.class);

	private boolean inited = false;

	private volatile boolean stopped = true;

	private Transaction transaction = Transaction.BEGIN;

	private int poolSize = 100;

	protected BlockingDeque<BatchRow> batchRows;

	public BatchRowPoolConsistent() {
	}

	@Override
	public void init() {
		if (inited) {
			return;
		}

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
		if (!stopped) {
			return;
		}

		stopped = false;

		transaction = Transaction.UNSET;
	}

	@Override
	public void stop() {
		if (stopped) {
			return;
		}

		stopped = true;

		batchRows.clear();
	}

	@Override
	public void put(ChangedEvent event) throws InterruptedException {
		if (accept(event)) {
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

	@Override
	public BatchRow take() throws InterruptedException {
		return batchRows.take();
	}

	private boolean accept(ChangedEvent event) {
		if (event instanceof DdlEvent) {
			return true;
		} else {
			RowChangedEvent row = (RowChangedEvent) event;

			switch (transaction) {
			case UNSET:
				return (!row.isTransactionBegin() && !row.isTransactionCommit());
			case BEGIN:
				if (row.isTransactionBegin()) {
					handleEventOrderException(event);
					return false;
				} else {
					return true;
				}
			case DML:
				if (row.isTransactionBegin()) {
					handleEventOrderException(event);
					return false;
				} else {
					return true;
				}
			case COMMIT:
				if (row.isTransactionBegin()) {
					return false;
				} else {
					handleEventOrderException(event);
					return false;
				}
			default:
				return false;
			}
		}
	}

	private void handleEventOrderException(ChangedEvent event) {
		String msg = String.format("Batch row pool event order exception: event(%s).", event.toString());
		LoadException e = new LoadException(-1, msg); // Can not recover, set -1.

		LOG.error(msg, e);
		Cat.logError(msg, e);

		throw e;
	}

	public void setPoolSize(int poolSize) {
		this.poolSize = poolSize;
	}

	private enum Transaction {
		UNSET,
		BEGIN,
		DML,
		COMMIT,
	}
}
