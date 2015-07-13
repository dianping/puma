package com.dianping.puma.server.builder;

import com.dianping.puma.biz.entity.PumaTaskEntity;
import com.dianping.puma.biz.entity.old.PumaTask;
import com.dianping.puma.server.TaskExecutor;

public interface TaskBuilder {

	TaskExecutor build(PumaTaskEntity task);
}
