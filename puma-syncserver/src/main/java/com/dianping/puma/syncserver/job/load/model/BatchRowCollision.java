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

	private BatchRow injecting;

	private volatile boolean locked = false;

	private final Object lock = new Object();

	public BatchRowCollision(int poolSize) {
		this.poolSize = poolSize;
	}

	public void inject(BatchRow batchRow) throws InterruptedException {
		injecting = batchRow;

		if (isCollision()) {
			synchronized (lock) {
				locked = true;
				lock.wait();
			}
		}

		if (injecting.isDdl()) {
			++ddlCount;
		} else {
			for (RowKey rowKey: injecting.listRowKeys()) {
				rowKeys.put(rowKey, true);
				++dmlCount;
			}
		}
	}

	public void extract(BatchRow row) {
		if (row.isDdl()) {
			--ddlCount;
		} else {
			for (RowKey rowKey: injecting.listRowKeys()) {
				rowKeys.remove(rowKey);
				--dmlCount;
			}
		}

		synchronized (lock) {
			if (locked && !isCollision()) {
				locked = false;
				injecting = null;
				lock.notify();
			}
		}
	}

	public boolean isFirst(ChangedEvent row) {
		return false;
	}

	private boolean isCollision() {
		if (injecting != null) {
			if (injecting.isDdl()) {
				return isDdlCollision();
			} else {
				return isDmlCollision();
			}
		}
		return false;
	}

	private boolean isDmlCollision() {
		if (injecting != null) {
			if (dmlCount + ddlCount >= poolSize) {
				return true;
			}

			if (ddlCount > 0) {
				return true;
			}

			for (RowKey rowKey: injecting.listRowKeys()) {
				if (rowKeys.containsKey(rowKey)) {
					return true;
				}
			}

			return false;
		}
		return false;
	}

	private boolean isDdlCollision() {
		if (injecting != null) {
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
