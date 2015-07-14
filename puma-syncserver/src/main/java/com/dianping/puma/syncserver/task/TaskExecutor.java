package com.dianping.puma.syncserver.task;

import com.dianping.puma.biz.entity.BaseTaskEntity;
import com.dianping.puma.core.LifeCycle;

public interface TaskExecutor<T extends BaseTaskEntity> extends LifeCycle<RuntimeException> {
}
