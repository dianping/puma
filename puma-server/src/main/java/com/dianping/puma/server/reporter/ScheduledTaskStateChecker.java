package com.dianping.puma.server.reporter;

import com.dianping.puma.biz.entity.PumaTaskStateEntity;
import com.dianping.puma.biz.service.PumaTaskStateService;
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
	PumaTaskStateService taskStateService;

	@Autowired
	TaskServerManager taskServerManager;

	@Autowired
	TaskContainer taskContainer;

	private ConcurrentMap<String, PumaTaskStateEntity> taskStates = new ConcurrentHashMap<String, PumaTaskStateEntity>();

	@Override
	public void check() {
		ConcurrentMap<String, PumaTaskStateEntity> oriTaskStates = taskStates;
		taskStates.clear();

		try {
			for (String host: taskServerManager.findAuthorizedHosts()) {
				for (PumaTaskStateEntity taskState: taskStateService.findByServerName(host)) {
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
			Map<String, PumaTaskStateEntity> oriTaskStates, Map<String, PumaTaskStateEntity> taskStates) {

		MapDifference<String, PumaTaskStateEntity> taskStateDifference = Maps.difference(oriTaskStates, taskStates);
		for (Map.Entry<String, PumaTaskStateEntity> entry: taskStateDifference.entriesOnlyOnRight().entrySet()) {
			try {
				changeTaskState(entry.getKey(), entry.getValue());
			} catch (Exception e) {
				// @todo.
			}
		}
	}

	protected void findAndHandleUpdatedTaskStates(
			Map<String, PumaTaskStateEntity> oriTaskStates, Map<String, PumaTaskStateEntity> taskStates) {

		MapDifference<String, PumaTaskStateEntity> taskStateDifference = Maps.difference(oriTaskStates, taskStates);
		Map<String, ValueDifference<PumaTaskStateEntity>> diffs = taskStateDifference.entriesDiffering();

		for (Map.Entry<String, ValueDifference<PumaTaskStateEntity>> entry: diffs.entrySet()) {
			PumaTaskStateEntity oriTaskState = entry.getValue().leftValue();
			PumaTaskStateEntity taskState = entry.getValue().rightValue();

			if (!oriTaskState.getController().equals(taskState.getController())) {
				try {
					changeTaskState(entry.getKey(), taskState);
				} catch (Exception e) {
					// @todo.
				}
			}
		}
	}

	private void changeTaskState(String taskName, PumaTaskStateEntity taskState) {
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
