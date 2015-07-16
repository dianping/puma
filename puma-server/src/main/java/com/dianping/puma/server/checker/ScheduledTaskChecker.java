package com.dianping.puma.server.checker;

import com.dianping.puma.biz.entity.PumaTaskEntity;
import com.dianping.puma.biz.service.PumaTaskService;
import com.dianping.puma.core.constant.ActionController;
import com.dianping.puma.server.container.TaskContainer;
import com.dianping.puma.server.server.TaskServerManager;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class ScheduledTaskChecker implements TaskChecker {

	@Autowired
	TaskServerManager taskServerManager;

	@Autowired
	PumaTaskService pumaTaskService;

	@Autowired
	TaskContainer taskContainer;

	private ConcurrentMap<String, PumaTaskEntity> tasks = new ConcurrentHashMap<String, PumaTaskEntity>();

	@Override
	public void check() {
		ConcurrentMap<String, PumaTaskEntity> oriTasks = tasks;
		tasks.clear();

		try {
			for (String host: taskServerManager.findAuthorizedHosts()) {
				for (PumaTaskEntity task: pumaTaskService.findByPumaServerName(host)) {
					tasks.put(task.getName(), task);
				}
			}
		} catch (Exception e) {
			// @todo.
			tasks = oriTasks;
			return;
		}

		// Created.
		findAndHandleCreatedTasks(oriTasks, tasks);

		// Updated.
		findAndHandleUpdatedTasks(oriTasks, tasks);

		// Deleted.
		findAndHandleDeletedTasks(oriTasks, tasks);
	}

	@Scheduled(fixedDelay = 5 * 1000)
	public void scheduledCheck() {
		check();
	}

	protected void findAndHandleCreatedTasks(
			Map<String, PumaTaskEntity> oriTasks, Map<String, PumaTaskEntity> tasks) {

		MapDifference<String, PumaTaskEntity> taskDifference = Maps.difference(oriTasks, tasks);
		for (Map.Entry<String, PumaTaskEntity> entry: taskDifference.entriesOnlyOnRight().entrySet()) {
			try {
				String taskName = entry.getKey();
				PumaTaskEntity task = entry.getValue();

				taskContainer.create(taskName, task);

				if (task.getActionController().equals(ActionController.START)) {
				}

				taskContainer.create(entry.getKey(), entry.getValue());
			} catch (Exception e) {
				// @todo.
			}
		}
	}

	protected void findAndHandleUpdatedTasks(
			Map<String, PumaTaskEntity> oriTasks, Map<String, PumaTaskEntity> tasks) {

		MapDifference<String, PumaTaskEntity> taskDifference = Maps.difference(oriTasks, tasks);
		Map<String, MapDifference.ValueDifference<PumaTaskEntity>> diffs = taskDifference.entriesDiffering();
		for (Map.Entry<String, MapDifference.ValueDifference<PumaTaskEntity>> entry: diffs.entrySet()) {
			try {
				taskContainer.update(entry.getKey(), entry.getValue().leftValue(), entry.getValue().rightValue());
			} catch (Exception e) {
				// @todo.
			}
		}
	}

	protected void findAndHandleDeletedTasks(
			Map<String, PumaTaskEntity> oriTasks, Map<String, PumaTaskEntity> tasks) {

		MapDifference<String, PumaTaskEntity> taskDifference = Maps.difference(oriTasks, tasks);
		for (Map.Entry<String, PumaTaskEntity> entry: taskDifference.entriesOnlyOnLeft().entrySet()) {
			try {
				taskContainer.delete(entry.getKey(), entry.getValue());
			} catch (Exception e) {
				// @todo.
			}
		}
	}
}
