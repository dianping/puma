package com.dianping.puma.syncserver.task.container;

import com.dianping.puma.biz.entity.sync.SyncTaskEntity;
import com.dianping.puma.syncserver.exception.PumaBaseTaskException;
import com.dianping.puma.syncserver.task.TaskExecutor;
import com.dianping.puma.syncserver.task.builder.TaskBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component("syncTaskContainer")
public class DefaultSyncTaskContainer implements TaskContainer<SyncTaskEntity> {

	@Autowired
	TaskBuilder<SyncTaskEntity> syncTaskBuilder;

	private ConcurrentMap<String, TaskExecutor> syncTaskExecutors = new ConcurrentHashMap<String, TaskExecutor>();

	@Override
	public void create(SyncTaskEntity task) {
		TaskExecutor taskExecutor = syncTaskBuilder.build(task);
		if (syncTaskExecutors.putIfAbsent(task.getName(), taskExecutor) == null) {
			throw new PumaBaseTaskException("create sync task failure, duplicate exist.");
		}

		start(task);
	}

	@Override
	public void update(SyncTaskEntity task) {
		// For easy design, `update` causes a `delete` and a `create`.
		delete(task);
		create(task);
	}

	@Override
	public void delete(SyncTaskEntity task) {
		stop(task);

		if (syncTaskExecutors.remove(task.getName()) == null) {
			throw new PumaBaseTaskException("delete sync task failure, not exist.");
		}
	}

	@Override
	public void start(SyncTaskEntity task) {
		TaskExecutor taskExecutor =
	}

	@Override
	public void stop(SyncTaskEntity task) {

	}
}
