package com.dianping.puma.server.builder;

import com.dianping.puma.biz.entity.PumaTask;
import com.dianping.puma.server.TaskExecutor;

public interface TaskExecutorBuilder {

	TaskExecutor build(PumaTask pumaTask) throws Exception;
}
