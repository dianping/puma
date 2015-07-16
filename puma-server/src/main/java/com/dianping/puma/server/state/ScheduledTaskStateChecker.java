package com.dianping.puma.server.state;

import com.dianping.puma.biz.entity.TaskStateEntity;
import com.dianping.puma.biz.service.TaskStateService;
import com.dianping.puma.server.container.TaskContainer;
import com.dianping.puma.server.server.TaskServerManager;
import com.google.common.collect.MapDifference;
import com.google.common.collect.MapDifference.ValueDifference;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class ScheduledTaskStateChecker implements TaskStateChecker {

	@Autowired
	TaskStateService taskStateService;

	@Autowired
	TaskServerManager taskServerManager;

	@Autowired
	TaskContainer taskContainer;

	private ConcurrentMap<String, TaskStateEntity> taskStates = new ConcurrentHashMap<String, TaskStateEntity>();

	@Override
	public void check() {
		ConcurrentMap<String, TaskStateEntity> oriTaskStates = taskStates;
		taskStates.clear();

		try {
			for (String host: taskServerManager.findAuthorizedHosts()) {
				for (TaskStateEntity taskState: taskStateService.findByServerName(host)) {
					taskStates.put(taskState.getTaskName(), taskState);
				}
			}
		} catch (Exception e) {
			taskStates = oriTaskStates;
			return;
		}

		// Created.
		findAndHandleCreatedTaskStates(oriTaskStates, taskStates);

		// Updated.
		findAndHandleUpdatedTaskStates(oriTaskStates, taskStates);
	}

	protected void findAndHandleCreatedTaskStates(
			Map<String, TaskStateEntity> oriTaskStates, Map<String, TaskStateEntity> taskStates) {

		MapDifference<String, TaskStateEntity> taskStateDifference = Maps.difference(oriTaskStates, taskStates);
		for (Map.Entry<String, TaskStateEntity> entry: taskStateDifference.entriesOnlyOnRight().entrySet()) {
			try {
				changeTaskState(entry.getKey(), entry.getValue());
			} catch (Exception e) {
				// @todo.
			}
		}
	}

	protected void findAndHandleUpdatedTaskStates(
			Map<String, TaskStateEntity> oriTaskStates, Map<String, TaskStateEntity> taskStates) {

		MapDifference<String, TaskStateEntity> taskStateDifference = Maps.difference(oriTaskStates, taskStates);
		Map<String, ValueDifference<TaskStateEntity>> diffs = taskStateDifference.entriesDiffering();

		for (Map.Entry<String, ValueDifference<TaskStateEntity>> entry: diffs.entrySet()) {
			TaskStateEntity oriTaskState = entry.getValue().leftValue();
			TaskStateEntity taskState = entry.getValue().rightValue();

			if (!oriTaskState.getController().equals(taskState.getController())) {
				try {
					changeTaskState(entry.getKey(), taskState);
				} catch (Exception e) {
					// @todo.
				}
			}
		}
	}

	private void changeTaskState(String taskName, TaskStateEntity taskState) {
		switch (taskState.getController()) {
		case START:
			taskContainer.start(taskName);
			break;
		case STOP:
			taskContainer.stop(taskName);
			break;
		case PAUSE:
			break;
		default:
			break;
		}
	}

	@Scheduled(fixedDelay = 5 * 1000)
	private void scheduledCheck() {
		check();
	}
}
