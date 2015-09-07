package com.dianping.puma.server.operator;

import com.dianping.puma.instance.InstanceManager;
import com.dianping.puma.server.builder.TaskBuilder;
import com.dianping.puma.taskexecutor.TaskExecutor;
import com.dianping.puma.taskexecutor.task.DatabaseTask;
import com.dianping.puma.taskexecutor.task.InstanceTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class DefaultTaskOperator implements TaskOperator {

	private final Logger logger = LoggerFactory.getLogger(DefaultTaskOperator.class);

	@Autowired
	TaskBuilder taskBuilder;

	@Autowired
	InstanceManager instanceManager;

	private ExecutorService pool = Executors.newCachedThreadPool();

	public TaskExecutor create(InstanceTask instanceTask) {
		TaskExecutor taskExecutor = taskBuilder.build(instanceTask);
		start(taskExecutor);
		return taskExecutor;
	}

	public TaskExecutor union(TaskExecutor executor, DatabaseTask databaseTask) {
		if (!canUnion(executor, databaseTask)) {
			return null;
		}

		stop(executor);

		if (!canUnion(executor, databaseTask)) {
			start(executor);
			return null;
		}

		TaskExecutor newExecutor = union0(executor, databaseTask);
		start(newExecutor);
		return newExecutor;
	}

	public TaskExecutor union(TaskExecutor executor0, TaskExecutor executor1) {
		return null;
	}

	public TaskExecutor complement(TaskExecutor executor, String database) {
		InstanceTask instanceTask = executor.getInstanceTask();
		if (instanceTask.contains(database)) {
			stop(executor);
			instanceTask.remove(database);
			if (instanceTask.size() != 0) {
				executor = taskBuilder.build(instanceTask);
				start(executor);
			}
		}
		return executor;
	}

	protected boolean canUnion(TaskExecutor executor, DatabaseTask databaseTask) {
		return false;
	}

	protected TaskExecutor union0(TaskExecutor executor, DatabaseTask databaseTask) {
		return null;
	}

	protected void start(final TaskExecutor taskExecutor) {
		if (taskExecutor == null) {
			throw new NullPointerException("task executor");
		}

		try {
			pool.execute(new Runnable() {
				@Override
				public void run() {
					try {
						taskExecutor.start();
					} catch (Throwable t) {
						logger.error("task executor error occurs.", t);
					}
				}
			});
		} catch (Throwable t) {
			throw new RuntimeException("failed to start task executor.", t);
		}
	}

	protected void stop(final TaskExecutor taskExecutor) {
		if (taskExecutor == null) {
			throw new NullPointerException("task executor");
		}

		try {
			taskExecutor.stop();
		} catch (Throwable t) {
			throw new RuntimeException("failed to stop task executor.", t);
		}
	}
}
