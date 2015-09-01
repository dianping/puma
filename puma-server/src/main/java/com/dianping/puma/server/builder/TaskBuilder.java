package com.dianping.puma.server.builder;

import com.dianping.puma.biz.entity.PumaTaskEntity;
import com.dianping.puma.server.container.InstanceTaskContainer;
import com.dianping.puma.taskexecutor.TaskExecutor;

public interface TaskBuilder {

	TaskExecutor build(InstanceTaskContainer.InstanceTask instanceTask);

	TaskExecutor build(PumaTaskEntity task) throws Exception;
}
