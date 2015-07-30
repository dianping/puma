package com.dianping.puma.syncserver.executor;

import com.dianping.puma.biz.entity.BaseTaskEntity;
import com.dianping.puma.syncserver.common.AbstractLifeCycle;
import com.dianping.puma.syncserver.manager.fail.FailPattern;

public abstract class AbstractTaskExecutor<T extends BaseTaskEntity> extends AbstractLifeCycle implements TaskExecutor<T> {

	protected volatile boolean stopped = true;

	protected T task;

	protected volatile FailPattern failPattern;

	@Override
	public T getTask() {
		return task;
	}

	protected void fail(String msg, Throwable cause) {

	}
}
