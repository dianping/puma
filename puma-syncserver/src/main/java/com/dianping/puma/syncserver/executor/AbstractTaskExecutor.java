package com.dianping.puma.syncserver.executor;

import com.dianping.puma.biz.entity.BaseTaskEntity;
import com.dianping.puma.syncserver.task.fail.FailPattern;

public abstract class AbstractTaskExecutor<T extends BaseTaskEntity> implements TaskExecutor<T> {

	protected volatile boolean stopped = true;

	protected T task;

	protected volatile FailPattern failPattern;

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

	protected void fail(String msg, Throwable cause) {

	}
}
