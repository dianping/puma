package com.dianping.puma.syncserver.accept;

import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.syncserver.accept.exception.SyncAcceptException;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class DefaultAcceptor implements Acceptor {

	private volatile boolean stopped = true;

	private int binlogEventQueueSize = 100;

	private BlockingQueue<ChangedEvent> binlogEventQueue;

	@Override
	public void start() {
		if (!stopped) {
			return;
		}

		binlogEventQueue = new ArrayBlockingQueue<ChangedEvent>(binlogEventQueueSize);

		stopped = false;
	}

	@Override
	public void stop() {
		if (stopped) {
			return;
		}

		stopped = true;

		binlogEventQueue.clear();
		binlogEventQueue = null;
	}

	@Override
	public void accept(ChangedEvent binlogEvent) throws SyncAcceptException {
		if (checkStop()) {
			throw new SyncAcceptException("`accept` failure, already stopped.");
		}

		try {
			binlogEventQueue.put(binlogEvent);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new SyncAcceptException("`accept` failure, already stopped.");
		}
	}

	@Override
	public ChangedEvent take() throws SyncAcceptException {
		if (checkStop()) {
			throw new SyncAcceptException("`take` failure, already stopped.");
		}

		try {
			return binlogEventQueue.take();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new SyncAcceptException("`take` failure, already stopped.");
		}
	}

	private boolean checkStop() {
		return stopped || Thread.currentThread().isInterrupted();
	}
}
