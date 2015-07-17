package com.dianping.puma.server.checker;

import com.dianping.puma.biz.entity.PumaTaskEntity;
import com.dianping.puma.biz.service.PumaTaskService;
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
		tasks = new ConcurrentHashMap<String, PumaTaskEntity>();

		try {
			for (String host : taskServerManager.findAuthorizedHosts()) {
				for (PumaTaskEntity task : pumaTaskService.findByPumaServerName(host)) {
					tasks.put(task.getName(), task);
				}
			}
		} catch (Exception e) {
			// @todo.
			tasks = oriTasks;
			return;
		}

		// Created.
		handleCreatedTasks(findCreatedTasks(oriTasks, tasks));

		// Updated.
		handleUpdatedTasks(findUpdatedTasks(oriTasks, tasks));

		// Deleted.
		handleDeletedTasks(findDeletedTasks(oriTasks, tasks));
	}

	@Scheduled(fixedDelay = 5 * 1000)
	public void scheduledCheck() {
		check();
	}

	protected Map<String, PumaTaskEntity> findCreatedTasks(Map<String, PumaTaskEntity> oriTasks,
			Map<String, PumaTaskEntity> tasks) {
		MapDifference<String, PumaTaskEntity> taskDifference = Maps.difference(oriTasks, tasks);
		return taskDifference.entriesOnlyOnRight();
	}

	protected Map<String, MapDifference.ValueDifference<PumaTaskEntity>> findUpdatedTasks(
			Map<String, PumaTaskEntity> oriTasks, Map<String, PumaTaskEntity> tasks) {
		MapDifference<String, PumaTaskEntity> taskDifference = Maps.difference(oriTasks, tasks);
		return taskDifference.entriesDiffering();
	}

	protected Map<String, PumaTaskEntity> findDeletedTasks(Map<String, PumaTaskEntity> oriTasks,
			Map<String, PumaTaskEntity> tasks) {
		MapDifference<String, PumaTaskEntity> taskDifference = Maps.difference(oriTasks, tasks);
		return taskDifference.entriesOnlyOnLeft();
	}

	private void handleCreatedTasks(Map<String, PumaTaskEntity> createdTasks) {
		for (Map.Entry<String, PumaTaskEntity> entry: createdTasks.entrySet()) {
			try {
				taskContainer.create(entry.getKey(), entry.getValue());
			} catch (Exception e) {
				// @todo.
			}
		}
	}

	private void handleUpdatedTasks(Map<String, MapDifference.ValueDifference<PumaTaskEntity>> updatedTasks) {
		for (Map.Entry<String, MapDifference.ValueDifference<PumaTaskEntity>> entry: updatedTasks.entrySet()) {
			try {
				taskContainer.update(entry.getKey(), entry.getValue().leftValue(), entry.getValue().rightValue());
			} catch (Exception e) {
				// @todo.
			}
		}
	}

	private void handleDeletedTasks(Map<String, PumaTaskEntity> deletedTasks) {
		for (Map.Entry<String, PumaTaskEntity> entry: deletedTasks.entrySet()) {
			try {
				taskContainer.delete(entry.getKey(), entry.getValue());
			} catch (Exception e) {
				// @todo.
			}
		}
	}
}
