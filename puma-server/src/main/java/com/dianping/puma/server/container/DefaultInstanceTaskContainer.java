package com.dianping.puma.server.container;

import com.dianping.puma.server.builder.TaskBuilder;
import com.dianping.puma.status.SystemStatusManager;
import com.dianping.puma.storage.manage.InstanceStorageManager;
import com.dianping.puma.taskexecutor.TaskExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class DefaultInstanceTaskContainer implements InstanceTaskContainer {

	private final Logger logger = LoggerFactory.getLogger(DefaultInstanceTaskContainer.class);

	@Autowired
	TaskBuilder taskBuilder;

	@Autowired
	InstanceStorageManager instanceStorageManager;

	private ConcurrentMap<String, InstanceTask> instanceTaskMap = new ConcurrentHashMap<String, InstanceTask>();

	private ConcurrentMap<String, TaskExecutor> taskExecutorMap = new ConcurrentHashMap<String, TaskExecutor>();

	private ExecutorService es = Executors.newCachedThreadPool();

	@Override
	public InstanceTask get(String instanceName) {
		return instanceTaskMap.get(instanceName);
	}

	@Override
	public void create(InstanceTask instanceTask) {
		String instanceName = instanceTask.getInstance();

		if (instanceTaskMap.containsKey(instanceName) || taskExecutorMap.containsKey(instanceName)) {
			throw new RuntimeException("instance already exists.");
		}

		TaskExecutor taskExecutor = taskBuilder.build(instanceTask);
		start(taskExecutor);

		taskExecutorMap.put(instanceName, taskExecutor);
		instanceTaskMap.put(instanceName, instanceTask);
	}

	@Override
	public void remove(String instanceName) {
		if (!instanceTaskMap.containsKey(instanceName) || !taskExecutorMap.containsKey(instanceName)) {
			throw new RuntimeException("instance not exists.");
		}

		TaskExecutor taskExecutor = taskExecutorMap.get(instanceName);
		stop(taskExecutor);

		SystemStatusManager.deleteServer(instanceName);

		instanceStorageManager.delete(instanceName);

		taskExecutorMap.remove(instanceName);
		instanceTaskMap.remove(instanceName);
	}

	@Override
	public void update(InstanceTask instanceTask) {
		String instanceName = instanceTask.getInstance();

		if (!instanceTaskMap.containsKey(instanceName) || !taskExecutorMap.containsKey(instanceName)) {
			throw new RuntimeException("instance not exists.");
		}

		TaskExecutor taskExecutor = taskExecutorMap.get(instanceName);
		onChange(taskExecutor, instanceTask);

		instanceTaskMap.put(instanceName, instanceTask);
	}

	protected void start(final TaskExecutor taskExecutor) {
		if (taskExecutor == null) {
			throw new NullPointerException("taskExecutor");
		}

		try {
			es.execute(new Runnable() {
				@Override
				public void run() {
					try {
						taskExecutor.start();
					} catch (Throwable t) {
						logger.error("task execute error.", t);
					}
				}
			});
		} catch (Throwable t) {
			throw new RuntimeException("failed to start task executor.", t);
		}
	}

	protected void stop(final TaskExecutor taskExecutor) {
		if (taskExecutor == null) {
			throw new NullPointerException("taskExecutor");
		}

		try {
			taskExecutor.stop();
		} catch (Throwable t) {
			throw new RuntimeException("failed to stop task executor.", t);
		}
	}

	protected void onChange(final TaskExecutor taskExecutor, InstanceTask instanceTask) {
		if (taskExecutor == null) {
			throw new NullPointerException("taskExecutor");
		}

		try {
			taskExecutor.onChange();
		} catch (Throwable t) {
			throw new RuntimeException("");
		}
	}
}
