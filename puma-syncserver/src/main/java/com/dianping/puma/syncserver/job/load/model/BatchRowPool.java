package com.dianping.puma.syncserver.job.load.model;

import com.dianping.puma.core.event.ChangedEvent;

import java.util.concurrent.*;

public class BatchRowPool {

	private LinkedBlockingDeque<BatchRow> batchRows;

	public BatchRowPool(int poolSize) {
		batchRows = new LinkedBlockingDeque<BatchRow>(poolSize);
	}

	public void put(ChangedEvent event) throws InterruptedException {
		BatchRow last = batchRows.peekLast();
		if (last == null) {
			batchRows.put(new BatchRow(event));
		} else {
			if (!last.addRow(event)) {
				batchRows.put(new BatchRow(event));
			}
		}
	}

	public void put(BatchRow batchRow) throws InterruptedException {
		batchRows.put(batchRow);
	}

	/*
	public void put(BatchRow batchRow) throws InterruptedException {
		if (this.buffer == null) {
			this.buffer = batchRow;
		} else {
			batchRows.put(this.buffer);
			this.buffer = batchRow;
		}
	}*/

	/*
	public void put(ChangedEvent row) throws InterruptedException {
		if (buffer.size() == 0) {
			buffer.addRow(row);
		} else {
			if (!buffer.addRow(row)) {
				batchRows.put(buffer);
				buffer = new BatchRow(row);
			}
		}
	}*/

	public BatchRow take() throws InterruptedException {
		return batchRows.take();
	}
}
