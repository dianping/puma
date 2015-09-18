package com.dianping.puma.api.lock;

import com.dianping.puma.core.lock.DistributedLock;
import com.dianping.puma.core.lock.DistributedLockFactory;

import java.util.concurrent.TimeUnit;

public class PumaClientLock {

	private DistributedLock lock;

	public PumaClientLock(String clientName) {
		lock = DistributedLockFactory.newZkDistributedLock(clientName);
	}

	public void lock(PumaClientLockListener listener) {
		lock.lockNotify(listener);
	}

	public boolean tryLock(PumaClientLockListener listener) {
		return lock.tryLockNotify(listener);
	}

	public boolean tryLock(long time, TimeUnit timeUnit, PumaClientLockListener listener) {
		return lock.tryLockNotify(time, timeUnit, listener);
	}

	public void unlock() {
		lock.unlockNotify();
	}
}
