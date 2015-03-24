package com.dianping.puma.syncserver.job.container;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.dianping.puma.core.constant.ActionController;
import com.dianping.puma.core.constant.Status;
import com.dianping.puma.core.constant.SyncType;
import com.dianping.puma.core.entity.BaseSyncTask;
import com.dianping.puma.core.holder.BinlogInfoHolder;
import com.dianping.puma.core.service.DumpTaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dianping.puma.core.monitor.NotifyService;
import com.dianping.puma.core.sync.model.taskexecutor.TaskExecutorStatus;
import com.dianping.puma.syncserver.job.executor.CatchupTaskExecutor;
import com.dianping.puma.syncserver.job.executor.DumpTaskExecutor;
import com.dianping.puma.syncserver.job.executor.SyncTaskExecutor;
import com.dianping.puma.syncserver.job.executor.TaskExecutionException;
import com.dianping.puma.syncserver.job.executor.TaskExecutor;

/**
 * @author wukezhu
 */
@Service
@SuppressWarnings("rawtypes")
public class DefaultTaskExecutorContainer implements TaskExecutionContainer {
	private static final Logger LOG = LoggerFactory.getLogger(DefaultTaskExecutorContainer.class);

	private ConcurrentHashMap<String, TaskExecutor> taskExecutorMap = new ConcurrentHashMap<String, TaskExecutor>();

	@Autowired
	private NotifyService notifyService;

	@Autowired
	BinlogInfoHolder binlogInfoHolder;

	@Autowired
	DumpTaskService dumpTaskService;

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
	}

	/**
	 * 此情形，是对SyncTask修改后的重启。 故需验证，只允许以下情形出现：<br>
	 * 新SyncTaskExecutor的StatusAction是RESTART，旧的SyncTaskExecutor状态是SUSPPENDED/FAILED/SUCCEED
	 */
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
		taskExecutor.pause("Pause this old SyncTaskExecutor, because is replace by a new SyncTaskExecutor.");
		newTaskExecutor.start();
	}

	/**
	 * 新的TaskExecutor(Sync,Dump,Catchup)，无论如何先put到container。 <br>
	 * 接着如果是Sync且StatusAction是START/RESTART，则启动即可；如果是Dump/Catchup，则直接启动
	 */
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
				syncTaskExecutor.pause("Stop because the StatusAction is Pause.");
			}
		} else if (newTaskExecutor instanceof DumpTaskExecutor || newTaskExecutor instanceof CatchupTaskExecutor) {
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

	/**
	 * 验证，只允许以下情况出现：<br>
	 * taskStatusActionEvent的StatusAction是PAUSE，旧的SyncTaskExecutor状态是PREPARING/RUNNING
	 * taskStatusActionEvent的StatusAction是RESTART，旧的SyncTaskExecutor状态是SUSPPENDED/FAILED/SUCCEED
	 */
	@Override
	public void changeStatus(String taskName, ActionController controller) {
		SyncTaskExecutor syncTaskExecutor = (SyncTaskExecutor) this.get(taskName);
		if (syncTaskExecutor != null) {
			if (controller == ActionController.PAUSE) {
				syncTaskExecutor.pause("Stop because the StatusAction is Pause.");
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
			syncTaskExecutor.stop("Disconnect because the StatusAction is deleted.");
		}
	}

	@Override
	public List<TaskExecutor> toList() {
		return new ArrayList<TaskExecutor>(taskExecutorMap.values());
	}

	@Override
	public List<TaskExecutor> getAll() {
		return new ArrayList<TaskExecutor>(taskExecutorMap.values());
	}

}
