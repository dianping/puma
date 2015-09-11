package com.dianping.puma.syncserver.manager.builder;

import com.dianping.puma.biz.entity.BaseTaskEntity;
import com.dianping.puma.syncserver.executor.TaskExecutor;
import org.springframework.stereotype.Service;

@Service
public class DefaultTaskBuilder implements TaskBuilder {

	@Override
	public TaskExecutor build(BaseTaskEntity task) {
		return null;
	}

	@Override
	public void unbuild(BaseTaskEntity task) {

	}
}
