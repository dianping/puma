package com.dianping.puma.comparison.manager.lock;

import com.dianping.puma.biz.entity.CheckTaskEntity;
import com.dianping.puma.biz.service.CheckTaskService;
import com.dianping.puma.comparison.manager.container.TaskContainer;
import com.dianping.puma.comparison.manager.server.TaskServerManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DefaultTaskLockBuilder implements TaskLockBuilder {

	@Autowired
	TaskContainer taskContainer;

	@Autowired
	CheckTaskService checkTaskService;

	@Autowired
	TaskServerManager taskServerManager;

	@Override
	public TaskLock buildLocalLock(CheckTaskEntity checkTask) {
		NoReentrantTaskLock taskLock = new NoReentrantTaskLock();
		taskLock.setTaskContainer(taskContainer);
		taskLock.setCheckTask(checkTask);
		return taskLock;
	}

	@Override
	public TaskLock buildRemoteLock(CheckTaskEntity checkTask) {
		DatabaseTaskLock taskLock = new DatabaseTaskLock();
		taskLock.setCheckTaskService(checkTaskService);
		taskLock.setTaskServerManager(taskServerManager);
		taskLock.setCheckTask(checkTask);
		return taskLock;
	}
}
