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

    /*
    @Override
    public void submit(final TaskExecutor newTaskExecutor) throws TaskExecutionException {
        LOG.info("TaskExecutor submit: " + newTaskExecutor.getTask());
        BaseSyncTask task = newTaskExecutor.getTask();
        SyncType syncType = task.getSyncType();
        //Type type = task.getType();
        //获取已有的TaskExecutor
        //执行taskExecutor
        TaskExecutor taskExecutor = taskExecutorMap
                .get(TaskExecutorStatus.calHashCode(syncType, newTaskExecutor.getTask().getName()));
        if (taskExecutor != null) {
            //如果有的话，一定是SyncTaskExecutor，是修改后重启
            refreshSyncTask(taskExecutorMap, taskExecutor, newTaskExecutor);
        } else {
            //新的TaskExecutor(Sync,Dump,Catchup)，无论如何先put到container。
            //接着如果是Sync且StatusAction是START/RESTART，则启动即可；如果是Dump/Catchup，则直接启动
            startTask(taskExecutorMap, newTaskExecutor);
        }
    }*/

	/*
	private void refreshSyncTask(ConcurrentHashMap<String, TaskExecutor> taskExecutorMap, TaskExecutor taskExecutor,
			TaskExecutor newTaskExecutor) {
		//验证
		if (!(taskExecutor instanceof SyncTaskExecutor && newTaskExecutor instanceof SyncTaskExecutor)) {
			return;
		}
		SyncTaskExecutor syncTaskExecutor0 = (SyncTaskExecutor) taskExecutor;
		SyncTaskExecutor syncTaskExecutor = (SyncTaskExecutor) newTaskExecutor;
		if (syncTaskExecutor.getTask().getController() != ActionController.RESUME
				|| syncTaskExecutor0.getTaskState().getStatus() != Status.SUSPENDED
				&& syncTaskExecutor0.getTaskState().getStatus() != Status.FAILED
				&& syncTaskExecutor0.getTaskState().getStatus() != Status.SUCCESS) {
			notifyService
					.alarm("Ignored a TaskExecutor which status is not correct: " + newTaskExecutor.getTask(), null, false);
			return;
		}
		//使用新的newTaskExecutor替换现有的taskExecutor
		taskExecutorMap.put(newTaskExecutor.getTask().getName(), newTaskExecutor);
		//taskExecutor.pause("Pause this old SyncTaskExecutor, because is replace by a new SyncTaskExecutor.");
		newTaskExecutor.start();
	}

	private void startTask(ConcurrentHashMap<String, TaskExecutor> taskExecutorMap, TaskExecutor newTaskExecutor) {
		String taskName = newTaskExecutor.getTask().getName();

		String dumpTaskName = taskName.replace("SyncTask", "DumpTask");
		if (taskExecutorMap.get(dumpTaskName) != null) {
			taskExecutorMap.remove(dumpTaskName);
			dumpTaskService.remove(dumpTaskName);
		}

		taskExecutorMap.put(newTaskExecutor.getTask().getName(), newTaskExecutor);
		if (newTaskExecutor instanceof SyncTaskExecutor) {
			SyncTaskExecutor syncTaskExecutor = (SyncTaskExecutor) newTaskExecutor;

			if (syncTaskExecutor.getTask().getController() == ActionController.RESUME
					|| syncTaskExecutor.getTask().getController() == ActionController.START) {
				syncTaskExecutor.start();
			} else {
				//syncTaskExecutor.pause("Stop because the StatusAction is Pause.");
			}
		} else if (newTaskExecutor instanceof ShardDumpTaskExecutor || newTaskExecutor instanceof ShardSyncTaskExecutor
				|| newTaskExecutor instanceof DumpTaskExecutor || newTaskExecutor instanceof CatchupTaskExecutor) {
			newTaskExecutor.start();
		} else {
			notifyService
					.alarm("ignored a new TaskExecutor which status is not correct: " + newTaskExecutor.getTask(), null,
							false);
		}
	}

	@Override
	public TaskExecutor get(String taskName) {
		return taskExecutorMap.get(taskName);
	}

	private void delete(SyncType syncType, String taskName) {
		taskExecutorMap.remove(taskName);
	}

	@Override
	public void changeStatus(String taskName, ActionController controller) {
		SyncTaskExecutor syncTaskExecutor = (SyncTaskExecutor) this.get(taskName);
		if (syncTaskExecutor != null) {
			if (controller == ActionController.PAUSE) {
				//syncTaskExecutor.pause("Stop because the StatusAction is Pause.");
			} else if (controller == ActionController.RESUME
					&& (syncTaskExecutor.getTaskState().getStatus() == Status.SUSPENDED
					|| syncTaskExecutor.getTaskState().getStatus() == Status.SUCCESS
					|| syncTaskExecutor.getTaskState().getStatus() == Status.FAILED)) {
				syncTaskExecutor.start();
			}
		}
	}

	public ConcurrentHashMap<String, TaskExecutor> getTaskExecutorMap() {
		return taskExecutorMap;
	}

	@Override
	public void deleteSyncTask(String taskName) {
		SyncTaskExecutor syncTaskExecutor = (SyncTaskExecutor) this.get(taskName);
		if (syncTaskExecutor != null) {
			this.delete(SyncType.SYNC, taskName);
			binlogInfoHolder.remove(taskName);
			LOG.info(syncTaskExecutor + " is deleted.");
			syncTaskExecutor.stop();
		}
	}

	@Override
	public void deleteShardSyncTask(String taskName) {
		ShardSyncTaskExecutor shardSyncTaskExecutor = (ShardSyncTaskExecutor) this.get(taskName);
		if (shardSyncTaskExecutor != null) {
			this.delete(SyncType.SHARD_SYNC, taskName);
			LOG.info(shardSyncTaskExecutor + " is deleted.");
			shardSyncTaskExecutor.stop();
			binlogInfoHolder.remove(taskName);
		}
	}

	@Override
	public void deleteShardDumpTask(String taskName) {
		ShardDumpTaskExecutor shardDumpTaskExecutor = (ShardDumpTaskExecutor) this.get(taskName);
		if (shardDumpTaskExecutor != null) {
			this.delete(SyncType.SHARD_DUMP, taskName);
			LOG.info(shardDumpTaskExecutor + " is deleted.");
			shardDumpTaskExecutor.stop();
		}
	}*/

	/*
	@Override
	public List<TaskExecutor> toList() {
		return new ArrayList<TaskExecutor>(taskExecutorMap.values());
	}

	@Override
	public List<TaskExecutor> getAll() {
		return new ArrayList<TaskExecutor>(taskExecutorMap.values());
	}*/

}
