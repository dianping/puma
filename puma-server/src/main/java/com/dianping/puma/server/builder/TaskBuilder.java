package com.dianping.puma.server.builder;

import com.dianping.puma.biz.entity.PumaTaskEntity;
import com.dianping.puma.taskexecutor.TaskExecutor;
import com.dianping.puma.taskexecutor.task.InstanceTask;

public interface TaskBuilder {

	TaskExecutor build(InstanceTask instanceTask);

	TaskExecutor build(PumaTaskEntity task) throws Exception;
}
