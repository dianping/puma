package com.dianping.puma.core.lock;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;

public class LionDistributedLock implements DistributedLock {

	protected LionDistributedLock() {}

	@Override
	public void lock() {

	}

	@Override
	public void lockInterruptibly() throws InterruptedException {

	}

	@Override
	public boolean tryLock() {
		return false;
	}

	@Override
	public boolean tryLock(long l, TimeUnit timeUnit) throws InterruptedException {
		return false;
	}

	@Override
	public void unlock() {

	}

	@Override public void unlockNotify() {

	}

	@Override public void lockNotify(DistributedLockLostListener listener) {

	}

	@Override public boolean tryLockNotify(DistributedLockLostListener listener) {
		return false;
	}

	@Override public boolean tryLockNotify(long time, TimeUnit timeUnit, DistributedLockLostListener listener) {
		return false;
	}

	@Override
	public Condition newCondition() {
		return null;
	}
}
