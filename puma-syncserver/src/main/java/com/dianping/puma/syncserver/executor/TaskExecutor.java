package com.dianping.puma.syncserver.executor;

import com.dianping.puma.biz.entity.BaseTaskEntity;

public interface TaskExecutor<T extends BaseTaskEntity> {

	public T getTask();
}
