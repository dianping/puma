package com.dianping.puma.server.container;

import com.dianping.puma.core.registry.RegistryService;
import com.dianping.puma.instance.InstanceManager;
import com.dianping.puma.server.builder.TaskBuilder;
import com.dianping.puma.server.server.TaskServerManager;
import com.dianping.puma.status.SystemStatusManager;
import com.dianping.puma.storage.holder.BinlogInfoHolder;
import com.dianping.puma.storage.manage.DatabaseStorageManager;
import com.dianping.puma.taskexecutor.TaskExecutor;
import com.dianping.puma.server.operator.TaskOperator;
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

@Service
public class DefaultTaskContainer implements TaskContainer {

	private final Logger logger = LoggerFactory.getLogger(DefaultTaskContainer.class);

	@Autowired
	InstanceManager instanceManager;

	@Autowired
	BinlogInfoHolder binlogInfoHolder;

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

	private Map<String, TaskExecutor> mainTaskExecutors = new HashMap<String, TaskExecutor>();

	private Map<String , List<TaskExecutor>> tempTaskExecutors = new HashMap<String, List<TaskExecutor>>();

	private Map<String, TaskExecutor> taskExecutors = new HashMap<String, TaskExecutor>();

	private Map<String, DatabaseTask> databaseTasks = new HashMap<String, DatabaseTask>();

	@Override
	public Map<String, DatabaseTask> getAll() {
		return databaseTasks;
	}

	@Override
	public void create(InstanceTask instanceTask) {
		logger.info("start creating instance task...");
		logger.info("instance task: {}.", instanceTask);

		if (instanceTask.isMain()) {
			createMain(instanceTask);
		} else {
			createTemp(instanceTask);
		}

		logger.info("success to create instance task.");
		logger.info("main tasks: {}.", countMainTaskExecutors());
		logger.info("temp tasks: {}.", countTempTaskExecutors());
		logger.info("task executors: {}.", countTaskExecutor());
		logger.info("database tasks: {}.", countDatabaseTask());
	}

	@Override
	public void create(DatabaseTask databaseTask) {
		logger.info("start creating temp task...");
		logger.info("database task: {}.", databaseTask);

		String database = databaseTask.getDatabase();
		String instance = findInstance(database);

		if (countMainTaskExecutor(instance) + countTempTaskExecutor(instance) == 0) {
			createMain(databaseTask);
		} else {
			createTemp(databaseTask);
		}

		logger.info("success to create task.");
		logger.info("main tasks: {}.", countMainTaskExecutors());
		logger.info("temp tasks: {}.", countTempTaskExecutors());
		logger.info("task executors: {}.", countTaskExecutor());
		logger.info("database tasks: {}.", countDatabaseTask());
	}

	protected void createMain(InstanceTask instanceTask) {
		logger.info("start creating main instance task...");

		String instance = instanceTask.getInstance();
		TaskExecutor mainTaskExecutor = taskOperator.create(instanceTask);

		addMainTaskExecutor(instance, mainTaskExecutor);
		for (DatabaseTask databaseTask: instanceTask.getDatabaseTasks()) {
			String database = databaseTask.getDatabase();
			addTaskExecutor(database, mainTaskExecutor);
			addDatabaseTask(database, databaseTask);
		}

		logger.info("success to create main instance task.");
	}

	protected void createMain(DatabaseTask databaseTask) {
		logger.info("start creating main database task...");

		String database = databaseTask.getDatabase();
		String instance = findInstance(database);
		if (instance == null) {
			throw new NullPointerException("instance");
		}

		InstanceTask mainInstanceTask = new InstanceTask(true, instance, databaseTask);
		TaskExecutor mainTaskExecutor = taskOperator.create(mainInstanceTask);

		addMainTaskExecutor(instance, mainTaskExecutor);
		addTaskExecutor(database, mainTaskExecutor);
		addDatabaseTask(database, databaseTask);

		logger.info("success to create main database task.");
	}

	protected void createTemp(InstanceTask instanceTask) {
		logger.info("start creating temp instance task...");

		String instance = instanceTask.getInstance();
		TaskExecutor tempTaskExecutor = taskOperator.create(instanceTask);

		addTempTaskExecutor(instance, tempTaskExecutor);
		for (DatabaseTask databaseTask: instanceTask.getDatabaseTasks()) {
			String database = databaseTask.getDatabase();
			addTaskExecutor(database, tempTaskExecutor);
			addDatabaseTask(database, databaseTask);
		}

		logger.info("success to create temp instance task.");
	}

	protected void createTemp(DatabaseTask databaseTask) {
		logger.info("start creating temp task...");

		String database = databaseTask.getDatabase();
		String instance = findInstance(database);
		if (instance == null) {
			throw new NullPointerException("instance");
		}

		InstanceTask tempInstanceTask = new InstanceTask(false, instance, databaseTask);
		TaskExecutor tempTaskExecutor = taskOperator.create(tempInstanceTask);

		addTempTaskExecutor(instance, tempTaskExecutor);
		addTaskExecutor(database, tempTaskExecutor);
		addDatabaseTask(database, databaseTask);

		logger.info("success to create temp task.");
	}

	@Override
	public void update(DatabaseTask databaseTask) {
		String database = databaseTask.getDatabase();
		remove(database);
		create(databaseTask);
	}

	@Override
	public void remove(String database) {
		logger.info("start removing database task...");
		logger.info("database: {}.", database);

		TaskExecutor taskExecutor = findTaskExecutor(database);
		InstanceTask instanceTask = taskExecutor.getInstanceTask();
		if (instanceTask.isMain()) {
			removeMain(database);
		} else {
			removeTemp(database);
		}

		logger.info("success to remove task.");
		logger.info("main task: {}.", countMainTaskExecutors());
		logger.info("temp task: {}.", countTempTaskExecutors());
		logger.info("task executor: {}.", countTaskExecutor());
		logger.info("database task: {}.", countDatabaseTask());
	}

	protected void removeMain(String database) {
		logger.info("start removing main database task...");

		TaskExecutor mainTaskExecutor = findTaskExecutor(database);
		mainTaskExecutor = taskOperator.complement(mainTaskExecutor, database);

		InstanceTask instanceTask = mainTaskExecutor.getInstanceTask();
		if (instanceTask.size() == 0) {
			binlogInfoHolder.remove(instanceTask.getTaskName());
			removeMainTaskExecutor(mainTaskExecutor);
			SystemStatusManager.deleteServer(instanceTask.getTaskName());
		}

		databaseStorageManager.delete(database);
		removeTaskExecutor(database);
		removeDatabaseTask(database);

		logger.info("success to remove main database task.");
	}

	protected void removeTemp(String database) {
		logger.info("start removing temp database task...");

		TaskExecutor tempTaskExecutor = findTaskExecutor(database);
		tempTaskExecutor = taskOperator.complement(tempTaskExecutor, database);

		InstanceTask instanceTask = tempTaskExecutor.getInstanceTask();
		binlogInfoHolder.remove(instanceTask.getTaskName());
		removeTempTaskExecutor(tempTaskExecutor);
		SystemStatusManager.deleteServer(instanceTask.getTaskName());

		databaseStorageManager.delete(database);
		removeTaskExecutor(database);
		removeDatabaseTask(database);

		logger.info("success to remove temp database task.");
	}

	protected void addMainTaskExecutor(String instance, TaskExecutor taskExecutor) {
		mainTaskExecutors.put(instance, taskExecutor);
	}

	protected void removeMainTaskExecutor(TaskExecutor taskExecutor) {
		String instance = taskExecutor.getInstanceTask().getInstance();
		TaskExecutor mainTaskExecutor = mainTaskExecutors.get(instance);
		if (mainTaskExecutor != null && mainTaskExecutor.equals(taskExecutor)) {
			mainTaskExecutors.remove(instance);
		}
	}

	protected int countMainTaskExecutors() {
		return mainTaskExecutors.size();
	}

	protected int countMainTaskExecutor(String instance) {
		TaskExecutor mainTaskExecutor = mainTaskExecutors.get(instance);
		return (mainTaskExecutor == null) ? 0 : 1;
	}

	protected void addTempTaskExecutor(String instance, TaskExecutor taskExecutor) {
		List<TaskExecutor> tempTaskExecutorList = tempTaskExecutors.get(instance);
		if (tempTaskExecutorList == null) {
			tempTaskExecutors.put(instance, Lists.newArrayList(taskExecutor));
		} else {
			tempTaskExecutorList.add(taskExecutor);
		}
	}

	protected void removeTempTaskExecutor(TaskExecutor taskExecutor) {
		String instance = taskExecutor.getInstanceTask().getInstance();
		List<TaskExecutor> tempTaskExecutorList = tempTaskExecutors.get(instance);
		if (tempTaskExecutorList != null) {
			tempTaskExecutorList.remove(taskExecutor);
		}
	}

	protected int countTempTaskExecutors() {
		int size = 0;
		for (List<TaskExecutor> taskExecutorList: tempTaskExecutors.values()) {
			size += taskExecutorList.size();
		}
		return size;
	}

	protected int countTempTaskExecutor(String instance) {
		List<TaskExecutor> taskExecutorList = tempTaskExecutors.get(instance);
		return taskExecutorList == null ? 0 : taskExecutorList.size();
	}

	protected TaskExecutor findTaskExecutor(String database) {
		return taskExecutors.get(database);
	}

	protected void addTaskExecutor(String database, TaskExecutor taskExecutor) {
		taskExecutors.put(database, taskExecutor);
	}

	protected void removeTaskExecutor(String database) {
		taskExecutors.remove(database);
	}

	protected int countTaskExecutor() {
		return taskExecutors.size();
	}

	protected void addDatabaseTask(String database, DatabaseTask databaseTask) {
		databaseTasks.put(database, databaseTask);
	}

	protected void removeDatabaseTask(String database) {
		databaseTasks.remove(database);
	}

	protected int countDatabaseTask() {
		return databaseTasks.size();
	}

	protected String findInstance(String database) {
		return instanceManager.getClusterByDb(database);
	}
}
