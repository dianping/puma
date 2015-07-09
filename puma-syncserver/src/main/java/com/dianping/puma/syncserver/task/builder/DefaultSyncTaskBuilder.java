package com.dianping.puma.syncserver.task.builder;

import com.dianping.puma.biz.entity.sync.SyncTaskEntity;
import com.dianping.puma.syncserver.job.executor.TaskExecutor;
import org.springframework.stereotype.Component;

@Component("syncTaskBuilder")
public class DefaultSyncTaskBuilder implements TaskBuilder<SyncTaskEntity> {

	@Override
	public TaskExecutor build(SyncTaskEntity task) {
		return null;
	}
}
