package com.dianping.puma.checkserver.manager.lock;

import com.dianping.puma.biz.entity.CheckTaskEntity;
import com.dianping.puma.checkserver.manager.container.CheckTaskContainer;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;

public class NoReentrantTaskLock implements TaskLock {

    private static final int MAX_TASK_SIZE = 50;

    protected CheckTaskContainer checkTaskContainer;

    protected CheckTaskEntity checkTask;

    @Override
    public void lock() {

    }

    @Override
    public void lockInterruptibly() throws InterruptedException {

    }

    @Override
    public boolean tryLock() {
        if (checkTaskContainer.contains(checkTask.getId())) {
            return false;
        } else {
            checkTaskContainer.create(checkTask);
            return true;
        }
    }

    @Override
    public boolean tryLock(long l, TimeUnit timeUnit) throws InterruptedException {
        return false;
    }

    @Override
    public void unlock() {
        checkTaskContainer.remove(checkTask.getId());
    }

    @Override
    public Condition newCondition() {
        return null;
    }

    public void setCheckTaskContainer(CheckTaskContainer checkTaskContainer) {
        this.checkTaskContainer = checkTaskContainer;
    }

    public void setCheckTask(CheckTaskEntity checkTask) {
        this.checkTask = checkTask;
    }
}
