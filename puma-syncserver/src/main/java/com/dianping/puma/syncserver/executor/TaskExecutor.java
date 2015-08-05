package com.dianping.puma.syncserver.executor;

import com.dianping.puma.biz.entity.BaseTaskEntity;
import com.dianping.puma.syncserver.common.LifeCycle;

public interface TaskExecutor<T extends BaseTaskEntity> extends LifeCycle {

	public T getTask();
}
