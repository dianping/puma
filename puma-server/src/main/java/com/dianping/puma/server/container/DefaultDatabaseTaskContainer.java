package com.dianping.puma.server.container;

import com.dianping.puma.core.registry.RegistryService;
import com.dianping.puma.instance.InstanceManager;
import com.dianping.puma.server.container.InstanceTaskContainer.InstanceTask;
import com.dianping.puma.server.server.TaskServerManager;
import com.dianping.puma.storage.manage.DatabaseStorageManager;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class DefaultDatabaseTaskContainer implements DatabaseTaskContainer {

	private final Logger logger = LoggerFactory.getLogger(DefaultDatabaseTaskContainer.class);

	@Autowired
	InstanceTaskContainer instanceTaskContainer;

	@Autowired
	InstanceManager instanceManager;

	@Autowired
	DatabaseStorageManager databaseStorageManager;

	@Autowired
	RegistryService registryService;

	@Autowired
	TaskServerManager taskServerManager;

	private Map<String, DatabaseTask> databaseTasks = new HashMap<String, DatabaseTask>();

	@Override
	public Map<String, DatabaseTask> getAll() {
		return databaseTasks;
	}

	@Override
	public synchronized void create(DatabaseTask databaseTask) {
		String database = databaseTask.getDatabase();

		String instance = instanceManager.getClusterByDb(database);
		if (instance == null) {
			logger.error("failed to find cluster for database({}).", database);
			throw new NullPointerException("instance");
		}

		InstanceTask instanceTask = instanceTaskContainer.get(instance);
		if (instanceTask == null) {
			instanceTask = new InstanceTask(instance, Lists.newArrayList(databaseTask));
			instanceTaskContainer.create(instanceTask);
		} else {
			instanceTask.create(databaseTask);
			instanceTaskContainer.update(instanceTask);
		}

		registryService.register(taskServerManager.findSelfHost(), database);

		databaseTasks.put(database, databaseTask);
	}

	@Override
	public void update(DatabaseTask databaseTask) {
		String database = databaseTask.getDatabase();

		String instance = instanceManager.getClusterByDb(database);
		if (instance == null) {
			logger.error("failed to find cluster for database({}).", database);
			throw new NullPointerException("instance");
		}

		InstanceTask instanceTask = instanceTaskContainer.get(instance);

		if (instanceTask == null) {
			logger.error("failed to find instance task for instance({}).", instance);
			throw new NullPointerException("instance task");
		}
		instanceTask.update(databaseTask);

		instanceTaskContainer.update(instanceTask);
	}

	@Override
	public void remove(String database) {
		String instance = instanceManager.getClusterByDb(database);
		if (instance == null) {
			logger.error("failed to find instance for database({}).", database);
			throw new NullPointerException("instance");
		}

		InstanceTask instanceTask = instanceTaskContainer.get(instance);

		if (instanceTask == null) {
			logger.error("failed to find instance task for instance({}).", instance);
			throw new NullPointerException("instance task");
		}
		instanceTask.remove(database);

		if (instanceTask.size() == 0) {
			instanceTaskContainer.remove(instance);
		} else {
			instanceTaskContainer.update(instanceTask);
		}

		databaseStorageManager.delete(database);

		registryService.unregister(taskServerManager.findSelfHost(), database);

		databaseTasks.remove(database);
	}
}
