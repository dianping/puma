package com.dianping.puma.syncserver.job.load.model;

import com.dianping.puma.core.event.ChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class BatchRowCollision {

	private static final Logger LOG = LoggerFactory.getLogger(BatchRowCollision.class);

	private int poolSize;

	private ConcurrentMap<RowKey, Boolean> rowKeys = new ConcurrentHashMap<RowKey, Boolean>();

	private volatile int dmlCount;

	private volatile int ddlCount;

	private BatchRow buffer;

	private volatile boolean locked = false;

	private final Object lock = new Object();

	public BatchRowCollision(int poolSize) {
		this.poolSize = poolSize;
	}

	public void inject(BatchRow batchRow) throws InterruptedException {
		buffer = batchRow;

		if (isCollision()) {
			synchronized (lock) {
				locked = true;
				lock.wait();
			}
		}

		if (buffer.isDdl()) {
			++ddlCount;
		} else {
			for (RowKey rowKey: buffer.listRowKeys()) {
				rowKeys.put(rowKey, true);
				++dmlCount;
			}
		}

		buffer = null;
	}

	public void extract(BatchRow row) {
		if (row.isDdl()) {
			--ddlCount;
		} else {
			for (RowKey rowKey: row.listRowKeys()) {
				rowKeys.remove(rowKey);
				--dmlCount;
			}
		}

		synchronized (lock) {
			if (locked && !isCollision()) {
				locked = false;
				lock.notify();
			}
		}
	}

	public boolean isFirst(ChangedEvent row) {
		return false;
	}

	private boolean isCollision() {
		if (buffer != null) {
			if (buffer.isDdl()) {
				return isDdlCollision();
			} else {
				return isDmlCollision();
			}
		}
		return false;
	}

	private boolean isDmlCollision() {
		if (buffer != null) {
			if (dmlCount + ddlCount >= poolSize) {
				return true;
			}

			if (ddlCount > 0) {
				return true;
			}

			for (RowKey rowKey: buffer.listRowKeys()) {
				if (rowKeys.containsKey(rowKey)) {
					return true;
				}
			}

			return false;
		}
		return false;
	}

	private boolean isDdlCollision() {
		if (buffer != null) {
			if (dmlCount + ddlCount >= poolSize) {
				return true;
			}

			if (ddlCount > 0) {
				return true;
			}

			if (dmlCount > 0) {
				return true;
			}

			return false;
		}
		return false;
	}
}
