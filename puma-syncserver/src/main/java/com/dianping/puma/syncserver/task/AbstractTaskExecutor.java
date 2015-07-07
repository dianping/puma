package com.dianping.puma.syncserver.task;

public abstract class AbstractTaskExecutor implements TaskExecutor {

	protected volatile boolean stopped = true;

	@Override
	public void start() {
		if (!stopped) {
			return;
		}

		doStart();

		stopped = false;
	}

	@Override
	public void stop() {
		if (stopped) {
			return;
		}

		stopped = true;

		doStop();
	}

	abstract void doStart();

	abstract void doStop();

	protected boolean checkStop() {
		return stopped || Thread.currentThread().isInterrupted();
	}
}
