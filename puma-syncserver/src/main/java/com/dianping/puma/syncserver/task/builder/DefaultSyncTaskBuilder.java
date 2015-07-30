package com.dianping.puma.syncserver.task.builder;

import com.dianping.puma.biz.entity.SyncTaskEntity;
import com.dianping.puma.syncserver.executor.load.AsyncLoader;
import com.dianping.puma.syncserver.executor.load.Loader;
import com.dianping.puma.syncserver.executor.SyncTaskExecutor;
import com.dianping.puma.syncserver.executor.TaskExecutor;
import org.springframework.stereotype.Component;

@Component("syncTaskBuilder")
public class DefaultSyncTaskBuilder implements TaskBuilder<SyncTaskEntity> {

	@Override
	public TaskExecutor build(SyncTaskEntity task) {
		SyncTaskExecutor executor = new SyncTaskExecutor();

		// @todo.
		// Puma client.

		return executor;
	}
}
