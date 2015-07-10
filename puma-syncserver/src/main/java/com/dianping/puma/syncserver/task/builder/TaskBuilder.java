package com.dianping.puma.syncserver.task.builder;

import com.dianping.puma.biz.entity.sync.BaseTaskEntity;
import com.dianping.puma.syncserver.task.TaskExecutor;

public interface TaskBuilder<T extends BaseTaskEntity> {

	TaskExecutor build(T task);
}
