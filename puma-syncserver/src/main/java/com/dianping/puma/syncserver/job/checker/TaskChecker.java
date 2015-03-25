package com.dianping.puma.syncserver.job.checker;

import java.util.List;

import javax.annotation.PostConstruct;

import com.dianping.puma.core.entity.SyncTask;
import com.dianping.puma.core.holder.BinlogInfoHolder;
import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.core.monitor.*;
import com.dianping.puma.core.monitor.event.Event;
import com.dianping.puma.core.service.BaseSyncTaskService;
import com.dianping.puma.core.service.SyncTaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dianping.puma.syncserver.config.SyncServerConfig;
import com.dianping.puma.syncserver.job.container.TaskExecutorContainer;
import com.dianping.puma.syncserver.job.executor.TaskExecutionException;
import com.dianping.puma.syncserver.job.executor.TaskExecutor;
import com.dianping.puma.syncserver.job.executor.builder.TaskExecutorBuilder;

@Service("taskChecker")
public class TaskChecker implements EventListener {

	private static final Logger LOG = LoggerFactory.getLogger(TaskChecker.class);

	@Autowired
	private SyncTaskService syncTaskService;

	@Autowired
	private TaskExecutorContainer taskExecutorContainer;

	@Autowired
	private TaskExecutorBuilder taskExecutorBuilder;

	@Autowired
	private SyncServerConfig syncServerConfig;

	@Autowired
	NotifyService notifyService;

	@Autowired
	BaseSyncTaskService baseSyncTaskService;

	@Autowired
	BinlogInfoHolder binlogInfoHolder;

	@SuppressWarnings("rawtypes")
	@PostConstruct
	public void init() {
		//加载所有Task
		String syncServerName = syncServerConfig.getSyncServerName();

		List<SyncTask> syncTasks = syncTaskService.findBySyncServerName(syncServerName);
		//构造成SyncTaskExecutor
		if (syncTasks != null && syncTasks.size() > 0) {
			for (SyncTask syncTask : syncTasks) {
				BinlogInfo binlogInfo = binlogInfoHolder.getBinlogInfo(syncTask.getName());
				if (binlogInfo != null) {
					syncTask.setBinlogInfo(binlogInfo);
				}
				TaskExecutor executor = taskExecutorBuilder.build(syncTask);
				//将Task交给Container
				try {
					taskExecutorContainer.submit(executor);
				} catch (TaskExecutionException e) {
					notifyService.alarm(e.getMessage(), e, false);
				}
			}
		}
		LOG.info("TaskChecker loaded " + (syncTasks != null ? syncTasks.size() : 0) + " tasks.");
		LOG.info("TaskChecker inited.");
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void onEvent(Event event) {
		/*
		LOG.info("Receive event: " + event);

		if (event instanceof SyncTaskOperationEvent) {

			SyncTaskOperationEvent syncTaskOperationEvent = (SyncTaskOperationEvent) event;
			String taskName = syncTaskOperationEvent.getTaskName();
			ActionOperation operation = syncTaskOperationEvent.getOperation();

			switch (operation) {
			case CREATE:
				BaseSyncTask task = baseSyncTaskService.find(SyncType.SYNC, taskName);
				TaskExecutor executor = taskExecutorBuilder.build(task);

				try {
					taskExecutorContainer.submit(executor);
				} catch (TaskExecutionException e) {
					notifyService.alarm(e.getMessage(), e, false);
				}
				break;

			case REMOVE:
				taskExecutorContainer.deleteSyncTask(taskName);
			}

		} else if (event instanceof SyncTaskControllerEvent) {

			SyncTaskControllerEvent syncTaskControllerEvent = (SyncTaskControllerEvent) event;
			taskExecutorContainer
					.changeStatus(syncTaskControllerEvent.getTaskName(), syncTaskControllerEvent.getController());

		} else {
			LOG.error("Receive error event.");
		}*/
	}
}
