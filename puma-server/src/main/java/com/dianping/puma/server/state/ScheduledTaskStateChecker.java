package com.dianping.puma.server.state;

import com.dianping.puma.biz.entity.TaskStateEntity;
import com.dianping.puma.biz.service.TaskStateService;
import com.dianping.puma.server.TaskExecutor;
import com.dianping.puma.server.container.TaskContainer;
import com.dianping.puma.server.server.TaskServerManager;
import com.google.common.collect.MapDifference;
import com.google.common.collect.MapDifference.ValueDifference;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.HashMap;
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
		for (String host: taskServerManager.findAuthorizedHosts()) {
			for (TaskExecutor taskExecutor: taskContainer.getAll()) {
				String taskName = taskExecutor.getTaskName();
				TaskStateEntity taskState = taskStateService.find(taskName, host);
				if (taskState != null) {
					taskStates.put(taskName, taskState);
				}
			}
		}
	}

	protected Map<String, TaskStateEntity> findChangedTaskStates(
			Map<String, TaskStateEntity> oriTaskStates, Map<String, TaskStateEntity> taskStates) {
		return null;
	}

	protected Map<String, TaskStateEntity> findCreatedTaskStates(
			Map<String, TaskStateEntity> oriTaskStates, Map<String, TaskStateEntity> taskStates) {
		MapDifference<String, TaskStateEntity> taskStateDifference = Maps.difference(oriTaskStates, taskStates);
		return taskStateDifference.entriesOnlyOnRight();
	}

	protected Map<String, TaskStateEntity> findUpdatedTaskStates(
			Map<String, TaskStateEntity> oriTaskStates, Map<String, TaskStateEntity> taskStates) {
		Map<String, TaskStateEntity> updatedTaskStates = new HashMap<String, TaskStateEntity>();
		MapDifference<String, TaskStateEntity> taskStateDifference = Maps.difference(oriTaskStates, taskStates);
		Map<String, ValueDifference<TaskStateEntity>> valueDifferences = taskStateDifference.entriesDiffering();

		for (Map.Entry<String, ValueDifference<TaskStateEntity>> entry: valueDifferences.entrySet()) {
			String taskName = entry.getKey();
			TaskStateEntity oriTaskState = entry.getValue().leftValue();
			TaskStateEntity taskState = entry.getValue().rightValue();

			if (!oriTaskState.getController().equals(taskState.getController())) {
				updatedTaskStates.put(taskName, taskState);
			}
		}

		return updatedTaskStates;
	}

	protected Map<String, TaskStateEntity> findDeletedTaskStates(
			Map<String, TaskStateEntity> oriTaskStates, Map<String, TaskStateEntity> taskStates) {
		MapDifference<String, TaskStateEntity> taskStateDifference = Maps.difference(oriTaskStates, taskStates);
		return taskStateDifference.entriesOnlyOnLeft();
	}

	protected void handleChangedTaskStates(Map<String, TaskStateEntity> changedTaskStates) {
		for (Map.Entry<String, TaskStateEntity> entry: changedTaskStates.entrySet()) {
			try {
				switch (entry.getValue().getController()) {
				case START:
					taskContainer.start(entry.getKey());
					break;
				case STOP:
					taskContainer.stop(entry.getKey());
					break;
				}
			} catch (Exception e) {
				// @todo.
			}
		}
	}

	@Scheduled(fixedDelay = 5 * 1000)
	private void scheduledCheck() {
		check();
	}
}
