package com.dianping.puma.comparison.manager.lock;

import com.dianping.puma.biz.entity.CheckTaskEntity;
import com.dianping.puma.biz.service.CheckTaskService;
import com.dianping.puma.comparison.manager.server.CheckTaskServerManager;
import com.dianping.puma.comparison.manager.utils.ThreadPool;
import com.google.common.util.concurrent.Uninterruptibles;

import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;

public class DatabaseTaskLock implements TaskLock {

	private CheckTaskService checkTaskService;

	private CheckTaskServerManager checkTaskServerManager;

	private CheckTaskEntity checkTask;

	private volatile boolean stopped = false;

	protected long lockTimeout = 60 * 1000; // 1 min.

	@Override
	public void lock() {

	}

	@Override
	public boolean tryLock() {
		try {
			CheckTaskEntity tempCheckTask = checkTaskService.findById(checkTask.getId());
			String host = checkTaskServerManager.findFirstAuthorizedHost();

			if (tempCheckTask.isRunning()) {
				if (isTimeout(tempCheckTask.getUpdateTime())) {
					return tryLock0();
				} else {
					return host.equals(tempCheckTask.getOwnerHost()) && tryLock0();
				}
			} else {
				return tryLock0();
			}
		} catch (Throwable t) {
			return false;
		}
	}

	@Override public void lockInterruptibly() throws InterruptedException {

	}

	@Override public boolean tryLock(long l, TimeUnit timeUnit) throws InterruptedException {
		return false;
	}

	@Override
	public void unlock() {
		CheckTaskEntity tempCheckTask = checkTaskService.findById(checkTask.getId());
		String host = checkTaskServerManager.findFirstAuthorizedHost();

		if (!tempCheckTask.isRunning()
				|| isTimeout(tempCheckTask.getUpdateTime())
				|| !host.equals(tempCheckTask.getOwnerHost())) {
			return;
		}

		stopped = true;

		checkTask.setRunning(false);
		checkTask.setOwnerHost(null);
		checkTask.setUpdateTime(new Date());

		checkTaskService.update(checkTask);
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

	protected boolean tryLock0() {
		String host = checkTaskServerManager.findFirstAuthorizedHost();

		checkTask.setRunning(true);
		checkTask.setOwnerHost(host);
		checkTask.setUpdateTime(new Date());

		int result = checkTaskService.update(checkTask);
		if (result == 0) {
			return false;
		}

		stopped = false;
		ThreadPool.execute(heartbeatWorker);
		return true;
	}

	protected boolean isTimeout(Date updateTime) {
		return new Date().getTime() - updateTime.getTime() >= lockTimeout;
	}

	public void setCheckTaskService(CheckTaskService checkTaskService) {
		this.checkTaskService = checkTaskService;
	}

	public void setCheckTaskServerManager(CheckTaskServerManager checkTaskServerManager) {
		this.checkTaskServerManager = checkTaskServerManager;
	}

	public void setCheckTask(CheckTaskEntity checkTask) {
		this.checkTask = checkTask;
	}
}
