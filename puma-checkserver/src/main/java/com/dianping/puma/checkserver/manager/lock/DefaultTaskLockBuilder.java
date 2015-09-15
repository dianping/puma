package com.dianping.puma.checkserver.manager.lock;

import com.dianping.puma.biz.entity.CheckTaskEntity;
import com.dianping.puma.biz.service.CheckTaskService;
import com.dianping.puma.checkserver.manager.container.CheckTaskContainer;
import com.dianping.puma.checkserver.manager.server.CheckTaskServerManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DefaultTaskLockBuilder implements TaskLockBuilder {

	@Autowired
	CheckTaskContainer checkTaskContainer;

	@Autowired
	CheckTaskService checkTaskService;

	@Autowired
	CheckTaskServerManager checkTaskServerManager;

	@Override
	public TaskLock buildLocalLock(CheckTaskEntity checkTask) {
		NoReentrantTaskLock taskLock = new NoReentrantTaskLock();
		taskLock.setCheckTaskContainer(checkTaskContainer);
		taskLock.setCheckTask(checkTask);
		return taskLock;
	}

	@Override
	public TaskLock buildRemoteLock(CheckTaskEntity checkTask) {
		DatabaseTaskLock taskLock = new DatabaseTaskLock();
		taskLock.setCheckTaskService(checkTaskService);
		taskLock.setCheckTaskServerManager(checkTaskServerManager);
		taskLock.setCheckTask(checkTask);
		return taskLock;
	}
}
