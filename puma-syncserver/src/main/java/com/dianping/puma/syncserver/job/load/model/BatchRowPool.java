package com.dianping.puma.syncserver.job.load.model;

import com.dianping.puma.core.event.ChangedEvent;

import java.util.concurrent.*;

public class BatchRowPool {

	private int poolSize;

	private BlockingQueue<BatchRow> batchRows;

	private BatchRow batchRow;

	public BatchRowPool(int poolSize) {
		this.poolSize = poolSize;
		batchRows = new ArrayBlockingQueue<BatchRow>(poolSize);
		batchRow = new BatchRow();
	}

	public void put(BatchRow batchRow) throws InterruptedException {
		if (this.batchRow == null) {
			this.batchRow = batchRow;
		} else {
			batchRows.put(this.batchRow);
			this.batchRow = batchRow;
		}
	}

	public void put(ChangedEvent row) throws InterruptedException {
		if (batchRow == null) {
			batchRow = new BatchRow(row);
		} else {
			if (!batchRow.addRow(row)) {
				batchRows.put(batchRow);
				batchRow = new BatchRow(row);
			}
		}
	}

	public BatchRow take() throws InterruptedException {
		if (batchRows.isEmpty() && batchRow != null) {
			batchRows.put(batchRow);
			batchRow = new BatchRow();
		}
		return batchRows.take();
	}
}
