package com.dianping.puma.server.checker;

import com.dianping.puma.biz.entity.PumaTaskEntity;
import com.dianping.puma.biz.service.PumaTaskService;
import com.dianping.puma.server.container.TaskContainer;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class PolledTaskChecker implements TaskChecker {

	@Autowired
	PumaTaskService pumaTaskService;

	@Autowired
	TaskContainer taskContainer;

	private ConcurrentMap<String, PumaTaskEntity> tasks = new ConcurrentHashMap<String, PumaTaskEntity>();

	@Scheduled(fixedDelay = 5 * 1000)
	public void poll() {
		ConcurrentMap<String, PumaTaskEntity> oriTasks = tasks;
		tasks.clear();

		try {
			for (PumaTaskEntity task: pumaTaskService.findAll()) {
				tasks.put(task.getName(), task);
			}
		} catch (Exception e) {
			// @todo.
			tasks = oriTasks;
		}

		// Created.
		Map<String, PumaTaskEntity> createdTasks = findCreatedTask(oriTasks, tasks);
		handleCreatedTask(createdTasks);

		// Updated.
		Map<String, PumaTaskEntity> updatedTasks = findUpdatedTask(oriTasks, tasks);
		handleUpdatedTask(updatedTasks);

		// Deleted.
		Map<String, PumaTaskEntity> deletedTasks = findDeletedTask(oriTasks, tasks);
		handleDeletedTask(deletedTasks);
	}

	protected Map<String, PumaTaskEntity> findCreatedTask(
			Map<String, PumaTaskEntity> oriTasks, Map<String, PumaTaskEntity> tasks) {
		MapDifference<String, PumaTaskEntity> taskDifference = Maps.difference(oriTasks, tasks);
		return taskDifference.entriesOnlyOnRight();
	}

	protected Map<String, PumaTaskEntity> findUpdatedTask(
			Map<String, PumaTaskEntity> oriTasks, Map<String, PumaTaskEntity> tasks) {
		return null;
	}

	protected Map<String, PumaTaskEntity> findDeletedTask(
			Map<String, PumaTaskEntity> oriTasks, Map<String, PumaTaskEntity> tasks) {
		MapDifference<String, PumaTaskEntity> taskDifference = Maps.difference(oriTasks, tasks);
		return taskDifference.entriesOnlyOnLeft();
	}

	private void handleCreatedTask(Map<String, PumaTaskEntity> createdTasks) {
		for (Map.Entry<String, PumaTaskEntity> entry: createdTasks.entrySet()) {
			try {
				taskContainer.create(entry.getKey(), entry.getValue());
			} catch (Exception e) {
				// @todo.
			}
		}
	}

	private void handleUpdatedTask(Map<String, PumaTaskEntity> updatedTasks) {
		for (Map.Entry<String, PumaTaskEntity> entry: updatedTasks.entrySet()) {
			try {
				taskContainer.update(entry.getKey(), entry.getValue());
			} catch (Exception e) {
				// @todo.
			}
		}
	}

	private void handleDeletedTask(Map<String, PumaTaskEntity> deletedTasks) {
		for (Map.Entry<String, PumaTaskEntity> entry: deletedTasks.entrySet()) {
			try {
				taskContainer.delete(entry.getKey(), entry.getValue());
			} catch (Exception e) {
				// @todo.
			}
		}
	}
}
