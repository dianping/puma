package com.dianping.puma.syncserver.task.container;

import com.dianping.puma.biz.entity.sync.SyncTaskEntity;
import com.dianping.puma.syncserver.job.executor.TaskExecutor;
import com.dianping.puma.syncserver.task.builder.TaskBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component("syncTaskContainer")
public class DefaultSyncTaskContainer implements TaskContainer<SyncTaskEntity> {

	@Autowired
	TaskBuilder<SyncTaskEntity> syncTaskBuilder;

	private ConcurrentMap<String, SyncTaskEntity> syncTaskExecutors = new ConcurrentHashMap<String, SyncTaskEntity>();

	@Override
	public void create(SyncTaskEntity task) {
		TaskExecutor taskExecutor = syncTaskBuilder.build(task);
	}

	@Override
	public void update(SyncTaskEntity task) {

	}

	@Override
	public void delete(SyncTaskEntity task) {

	}

	@Override
	public void pause(SyncTaskEntity task) {

	}

	@Override
	public void resume(SyncTaskEntity task) {

	}
}
