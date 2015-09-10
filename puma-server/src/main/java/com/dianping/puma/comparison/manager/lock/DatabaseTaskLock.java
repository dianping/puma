package com.dianping.puma.comparison.manager.lock;

import com.dianping.puma.biz.entity.CheckTaskEntity;
import com.dianping.puma.biz.service.CheckTaskService;
import com.dianping.puma.comparison.manager.server.TaskServerManager;
import com.google.common.util.concurrent.Uninterruptibles;

import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;

public class DatabaseTaskLock implements TaskLock {

	private CheckTaskService checkTaskService;

	private TaskServerManager taskServerManager;

	private CheckTaskEntity checkTask;

	private volatile boolean stopped = false;

	private long lockTimeout = 30; // 30s.

	@Override
	public void lock() {

	}

	@Override
	public boolean tryLock() {
		CheckTaskEntity tempCheckTask = checkTaskService.findById(checkTask.getId());
		String host = taskServerManager.findFirstAuthorizedHost();

		if (tempCheckTask.isRunning()
				&& isTimeout(tempCheckTask.getUpdateTime())
				&& !host.equals(tempCheckTask.getOwnerHost())) {
			return false;
		}

		tempCheckTask.setRunning(true);
		tempCheckTask.setOwnerHost(host);
		tempCheckTask.setUpdateTime(new Date());
		checkTask = tempCheckTask;

		checkTaskService.update(checkTask);

		stopped = false;
		DatabaseTaskLockThreadPool.execute(heartbeatWorker);

		return true;
	}

	@Override public void lockInterruptibly() throws InterruptedException {

	}

	@Override public boolean tryLock(long l, TimeUnit timeUnit) throws InterruptedException {
		return false;
	}

	@Override
	public void unlock() {
		CheckTaskEntity tempCheckTask = checkTaskService.findById(checkTask.getId());
		String host = taskServerManager.findFirstAuthorizedHost();

		if (!tempCheckTask.isRunning()
				|| isTimeout(tempCheckTask.getUpdateTime())
				|| !host.equals(tempCheckTask.getOwnerHost())) {
			return;
		}

		stopped = true;

		tempCheckTask.setRunning(false);
		tempCheckTask.setOwnerHost(null);
		tempCheckTask.setUpdateTime(new Date());
		checkTask = tempCheckTask;

		checkTaskService.update(tempCheckTask);
	}

	@Override
	public Condition newCondition() {
		return null;
	}

	private Runnable heartbeatWorker = new Runnable() {
		@Override
		public void run() {
			if (!stopped) {
				checkTaskService.update(checkTask);
				Uninterruptibles.sleepUninterruptibly(5, TimeUnit.SECONDS);
			}
		}
	};

	protected boolean isTimeout(Date updateTime) {
		return new Date().getTime() - updateTime.getTime() >= lockTimeout;
	}

	public void setCheckTaskService(CheckTaskService checkTaskService) {
		this.checkTaskService = checkTaskService;
	}

	public void setTaskServerManager(TaskServerManager taskServerManager) {
		this.taskServerManager = taskServerManager;
	}

	public void setCheckTask(CheckTaskEntity checkTask) {
		this.checkTask = checkTask;
	}
}
