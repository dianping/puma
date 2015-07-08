package com.dianping.puma.syncserver.buffer;

import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.syncserver.load.LoadFuture;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class DefaultDuplexBuffer implements DuplexBuffer {

	private volatile boolean stopped = true;

	private int queryQueueSize = 100;
	private BlockingQueue<ChangedEvent> queryQueue;

	private int resultQueueSize = 100;
	private BlockingQueue<LoadFuture> resultQueue;

	@Override
	public void start() {
		if (!stopped) {
			return;
		}

		queryQueue = new ArrayBlockingQueue<ChangedEvent>(queryQueueSize);
		resultQueue = new ArrayBlockingQueue<LoadFuture>(resultQueueSize);

		stopped = false;
	}

	@Override
	public void stop() {
		if (stopped) {
			return;
		}

		stopped = true;

		queryQueue.clear();
		queryQueue = null;
		resultQueue.clear();
		resultQueue = null;
	}

	@Override
	public void putQuery(ChangedEvent binlogEvent) {
		if (!checkStop()) {
			try {
				queryQueue.put(binlogEvent);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
	}

	@Override
	public ChangedEvent pollQuery() {
		if (!checkStop()) {
			try {
				return queryQueue.take();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}

		return null;
	}

	@Override
	public void putResult(LoadFuture loadFuture) {
		if (!checkStop()) {
			try {
				resultQueue.put(loadFuture);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
	}

	@Override
	public LoadFuture pollResult() {
		if (!checkStop()) {
			try {
				return resultQueue.take();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}

		return null;
	}

	private boolean checkStop() {
		return stopped || Thread.currentThread().isInterrupted();
	}
}
