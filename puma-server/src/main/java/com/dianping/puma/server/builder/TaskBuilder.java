package com.dianping.puma.server.builder;

import com.dianping.puma.biz.entity.PumaTaskEntity;
import com.dianping.puma.taskexecutor.TaskExecutor;

public interface TaskBuilder {

	TaskExecutor build(PumaTaskEntity task) throws Exception;
}
