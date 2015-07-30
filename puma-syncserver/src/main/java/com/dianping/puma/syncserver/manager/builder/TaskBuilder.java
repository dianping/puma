package com.dianping.puma.syncserver.manager.builder;

import com.dianping.puma.biz.entity.BaseTaskEntity;
import com.dianping.puma.syncserver.executor.TaskExecutor;

public interface TaskBuilder {

	public TaskExecutor build(BaseTaskEntity task);

	public void unbuild(BaseTaskEntity task);
}
