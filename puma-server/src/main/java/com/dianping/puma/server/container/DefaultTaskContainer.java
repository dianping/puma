package com.dianping.puma.server.container;

import com.dianping.puma.core.registry.RegistryService;
import com.dianping.puma.instance.InstanceManager;
import com.dianping.puma.server.builder.TaskBuilder;
import com.dianping.puma.server.server.TaskServerManager;
import com.dianping.puma.storage.manage.DatabaseStorageManager;
import com.dianping.puma.taskexecutor.TaskExecutor;
import com.dianping.puma.taskexecutor.operator.TaskOperator;
import com.dianping.puma.taskexecutor.task.DatabaseTask;
import com.dianping.puma.taskexecutor.task.InstanceTask;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class DefaultTaskContainer implements TaskContainer {

	private final Logger logger = LoggerFactory.getLogger(DefaultTaskContainer.class);

	@Autowired
	InstanceManager instanceManager;

	@Autowired
	DatabaseStorageManager databaseStorageManager;

	@Autowired
	RegistryService registryService;

	@Autowired
	TaskServerManager taskServerManager;

	@Autowired
	TaskOperator taskOperator;

	@Autowired
	TaskBuilder taskBuilder;

	private ExecutorService pool = Executors.newCachedThreadPool();

	private Map<String, String> mapping = new HashMap<String, String>();

	private Map<String, List<TaskExecutor>> taskExecutors = new HashMap<String, List<TaskExecutor>>();

	private Map<String, DatabaseTask> databaseTasks = new HashMap<String, DatabaseTask>();

	@Override
	public Map<String, DatabaseTask> getAll() {
		return databaseTasks;
	}

	@Override
	public synchronized void create(DatabaseTask databaseTask) {
		String database = databaseTask.getDatabase();
		if (mapping.containsKey(database)) {
			throw new RuntimeException("duplicated create");
		}

		String instance = instanceManager.getClusterByDb(database);
		if (instance == null) {
			throw new NullPointerException("instance");
		}

		TaskExecutor newExecutor = null;

		List<TaskExecutor> oriExecutors = taskExecutors.get(instance);
		if (oriExecutors == null || oriExecutors.isEmpty()) {
			InstanceTask instanceTask = new InstanceTask(instance, Lists.newArrayList(databaseTask));
			newExecutor = taskBuilder.build(instanceTask);
		} else {
			for (TaskExecutor oriExecutor: oriExecutors) {
				newExecutor = taskOperator.union(oriExecutor, databaseTask);
				if (newExecutor != null) {
					break;
				}
			}

			if (newExecutor == null) {
				InstanceTask instanceTask = new InstanceTask(instance, Lists.newArrayList(databaseTask));
				newExecutor = taskBuilder.build(instanceTask);
			}
		}

		start(newExecutor);

		addMapping(database, newExecutor.getTaskName());
		addTaskExecutor(instance, newExecutor);
	}

	@Override
	public void update(DatabaseTask databaseTask) {
//		String database = databaseTask.getDatabase();
//
//		String instance = instanceManager.getClusterByDb(database);
//		if (instance == null) {
//			logger.error("failed to find cluster for database({}).", database);
//			throw new NullPointerException("instance");
//		}
//
//		InstanceTask instanceTask = instanceTaskContainer.get(instance);
//
//		if (instanceTask == null) {
//			logger.error("failed to find instance task for instance({}).", instance);
//			throw new NullPointerException("instance task");
//		}
//		instanceTask.update(databaseTask);
//
//		instanceTaskContainer.update(instanceTask);
	}

	@Override
	public void remove(String database) {
//		String instance = instanceManager.getClusterByDb(database);
//		if (instance == null) {
//			logger.error("failed to find instance for database({}).", database);
//			throw new NullPointerException("instance");
//		}
//
//		TaskExecutor newExecutor = null;
//		List<TaskExecutor> oriExecutors = instanceTaskContainer.get(instance);
//
//		if (oriExecutors == null || oriExecutors.isEmpty()) {
//			throw new RuntimeException();
//		} else {
//			for (TaskExecutor oriExecutor: oriExecutors) {
//				newExecutor = taskOperator.complement(oriExecutor, database);
//				if (newExecutor != null) {
//					break;
//				}
//			}
//
//			if (newExecutor == null) {
//
//			} else {
//
//			}
//		}
//
//		start(newExecutor);
//
//		InstanceTask instanceTask = instanceTaskContainer.get(instance);
//
//		if (instanceTask == null) {
//			logger.error("failed to find instance task for instance({}).", instance);
//			throw new NullPointerException("instance task");
//		}
//		instanceTask.remove(database);
//
//		if (instanceTask.size() == 0) {
//			instanceTaskContainer.remove(instance);
//		} else {
//			instanceTaskContainer.update(instanceTask);
//		}
//
//		databaseStorageManager.delete(database);
//
//		registryService.unregister(taskServerManager.findSelfHost(), database);
//
//		databaseTasks.remove(database);
	}

	protected void addTaskExecutor(String instance, TaskExecutor taskExecutor) {
		if (taskExecutors.containsKey(instance)) {
			taskExecutors.get(instance).add(taskExecutor);
		} else {
			taskExecutors.put(instance, Lists.newArrayList(taskExecutor));
		}
	}

	protected void addMapping(String database, String taskName) {
		mapping.put(database, taskName);
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
