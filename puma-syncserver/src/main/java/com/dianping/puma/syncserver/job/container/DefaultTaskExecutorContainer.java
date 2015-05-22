package com.dianping.puma.syncserver.job.container;

import com.dianping.puma.core.entity.SyncTask;
import com.dianping.puma.core.service.SyncTaskService;
import com.dianping.puma.syncserver.config.SyncServerConfig;
import com.dianping.puma.syncserver.job.container.exception.TECException;
import com.dianping.puma.syncserver.job.executor.*;
import com.dianping.puma.syncserver.job.executor.builder.TaskExecutorBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Service("defaultTaskExecutorContainer")
public class DefaultTaskExecutorContainer implements TaskExecutorContainer {

	private static final Logger LOG = LoggerFactory.getLogger(DefaultTaskExecutorContainer.class);

	private ConcurrentHashMap<String, TaskExecutor> taskExecutorMap = new ConcurrentHashMap<String, TaskExecutor>();

	@Autowired
	private SyncServerConfig syncServerConfig;

	@Autowired
	private SyncTaskService syncTaskService;

	@Autowired
	private TaskExecutorBuilder taskExecutorBuilder;

	@PostConstruct
	public void init() {
		// Find syncserver name.
		String syncServerName = syncServerConfig.getSyncServerName();

		// Find corresponding syncserver tasks.
		List<SyncTask> syncTasks = syncTaskService.findBySyncServerName(syncServerName);
		for (SyncTask syncTask: syncTasks) {
			TaskExecutor taskExecutor = taskExecutorBuilder.build(syncTask);
			submit(syncTask.getName(), taskExecutor);
			taskExecutor.start();
		}
	}

	@Override
	public void submit(String name, TaskExecutor taskExecutor) throws TECException {
		LOG.info("Submitting to task executor({})...", name);

		// Put into the container.
		TaskExecutor oriTaskExecutor = taskExecutorMap.putIfAbsent(name, taskExecutor);

		if (oriTaskExecutor != null) {
			// Submit failure.
			LOG.error("Submitting task executor({}) failure: duplicated.", name);
			throw new TECException(-1, String.format("Submitting task executor(%s) failure: duplicated.", name));
		}
	}

	@Override
	public void withdraw(String name) throws TECException {
		LOG.info("Withdrawing task executor({})...", name);

		// Remove from the container.
		taskExecutorMap.remove(name);
	}

	@Override
	public void start(String name) throws TECException {
		LOG.info("Starting task executor({})...", name);

		TaskExecutor taskExecutor = taskExecutorMap.get(name);
		if (taskExecutor == null) {
			// Not in container.
			LOG.error("Starting task executor({}) failure: not in container.", name);
			throw new TECException(-1, String.format("Starting task executor(%s) failure: not in container.", name));
		} else {
			// In container.
			taskExecutor.start();
		}
	}

	@Override
	public void stop(String name) throws TECException {
		LOG.info("Stopping task executor({})...", name);

		TaskExecutor taskExecutor = taskExecutorMap.get(name);
		if (taskExecutor == null) {
			// Not in container.
			LOG.error("Stopping task executor({}) failure: not in container.", name);
			throw new TECException(-1, String.format("Stopping task executor(%s) failure: not in container.", name));
		} else {
			// In container.
			taskExecutor.stop();
		}
	}

	@Override
	public void die(String name) throws TECException {
		LOG.info("Dieing task executor({})...", name);

		TaskExecutor taskExecutor = taskExecutorMap.get(name);
		if (taskExecutor == null) {
			// Not in container.
			LOG.error("Dieing task executor({}) failure: not in container.", name);
			throw new TECException(-1, String.format("Dieing task executor(%s) failure: not in container.", name));
		} else {
			// In container.
			taskExecutor.die();
		}
	}

	@Override
	public TaskExecutor get(String name) {
		return taskExecutorMap.get(name);
	}

	@Override
	public List<TaskExecutor> getAll() {
		return new ArrayList<TaskExecutor>(taskExecutorMap.values());
	}

	@Override
	public int size() {
		return taskExecutorMap.size();
	}
}
