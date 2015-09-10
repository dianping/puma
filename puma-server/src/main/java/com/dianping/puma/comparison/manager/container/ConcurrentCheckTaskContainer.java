package com.dianping.puma.comparison.manager.container;

import com.dianping.puma.biz.entity.CheckTaskEntity;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class ConcurrentCheckTaskContainer implements CheckTaskContainer {

	private ConcurrentMap<Integer, CheckTaskEntity> checkTasks = new ConcurrentHashMap<Integer, CheckTaskEntity>();

	@Override
	public void create(CheckTaskEntity checkTask) {
		checkTasks.put(checkTask.getId(), checkTask);
	}

	@Override
	public void remove(int taskId) {
		checkTasks.remove(taskId);
	}

	@Override
	public boolean contains(int taskId) {
		return checkTasks.containsKey(taskId);
	}
}
