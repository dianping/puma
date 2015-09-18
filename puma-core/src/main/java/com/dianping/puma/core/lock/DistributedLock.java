package com.dianping.puma.core.lock;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

public interface DistributedLock extends Lock {

	public void lockNotify(DistributedLockLostListener listener);

	public boolean tryLockNotify(DistributedLockLostListener listener);

	public boolean tryLockNotify(long time, TimeUnit timeUnit, DistributedLockLostListener listener) throws InterruptedException;

	public void unlockNotify();
}
