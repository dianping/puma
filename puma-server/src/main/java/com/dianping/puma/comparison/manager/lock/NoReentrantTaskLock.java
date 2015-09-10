package com.dianping.puma.comparison.manager.lock;

import com.dianping.puma.biz.entity.CheckTaskEntity;
import com.dianping.puma.comparison.manager.container.TaskContainer;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;

public class NoReentrantTaskLock implements TaskLock {

	protected TaskContainer taskContainer;

	protected CheckTaskEntity checkTask;

	@Override public void lock() {

	}

	@Override public void lockInterruptibly() throws InterruptedException {

	}

	@Override
	public boolean tryLock() {
		if (taskContainer.contains(checkTask.getId())) {
			return false;
		} else {
			taskContainer.create(checkTask);
			return true;
		}
	}

	@Override public boolean tryLock(long l, TimeUnit timeUnit) throws InterruptedException {
		return false;
	}

	@Override
	public void unlock() {
		taskContainer.remove(checkTask.getId());
	}

	@Override public Condition newCondition() {
		return null;
	}

	public void setTaskContainer(TaskContainer taskContainer) {
		this.taskContainer = taskContainer;
	}

	public void setCheckTask(CheckTaskEntity checkTask) {
		this.checkTask = checkTask;
	}
}
