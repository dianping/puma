package com.dianping.puma.syncserver.manager.container;

import com.dianping.puma.biz.entity.BaseTaskEntity;
import com.dianping.puma.syncserver.exception.PumaException;
import com.dianping.puma.syncserver.executor.TaskExecutor;
import com.dianping.puma.syncserver.manager.builder.TaskBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class DefaultTaskContainer implements TaskContainer {

	@Autowired
	TaskBuilder taskBuilder;

	protected ConcurrentMap<String, TaskExecutor> taskExecutors = new ConcurrentHashMap<String, TaskExecutor>();

	@Override
	public void create(String taskName, BaseTaskEntity task) {
		TaskExecutor taskExecutor = taskBuilder.build(task);
		if (taskExecutors.putIfAbsent(taskName, taskExecutor) != null) {
			taskBuilder.unbuild(task);
			throw new PumaException("create puma task failure, duplicated task name.");
		}
	}

	@Override
	public void delete(String taskName, BaseTaskEntity task) {
		if (taskExecutors.remove(taskName) == null) {
			throw new PumaException("delete puma task failure, not exists.");
		}
		taskBuilder.unbuild(task);
	}

	@Override
	public void update(String taskName, BaseTaskEntity oriTask, BaseTaskEntity task) {
	}

	@Override
	public void start(String taskName) {
		TaskExecutor taskExecutor = taskExecutors.get(taskName);
		if (taskExecutor == null) {
			throw new PumaException("start puma task failure, not exists.");
		}
		taskExecutor.start();
	}

	@Override
	public void stop(String taskName) {
		TaskExecutor taskExecutor = taskExecutors.get(taskName);
		if (taskExecutor == null) {
			throw new PumaException("stop puma task failure, not exists.");
		}
		taskExecutor.stop();
	}
}
