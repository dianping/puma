package com.dianping.puma.api.lock;

import com.dianping.puma.core.lock.DistributedLock;
import com.dianping.puma.core.lock.DistributedLockFactory;
import com.dianping.puma.core.lock.DistributedLockLostListener;
import com.dianping.puma.core.lock.DistributedLockUtils;

import java.util.concurrent.TimeUnit;

public class PumaClientLock {

	private volatile boolean lockState = false;

	private DistributedLock lock;

	private DistributedLockLostListener listener;

	public PumaClientLock(String clientName) {
		lock = DistributedLockFactory.newZkDistributedLock(clientName);
		listener = new DistributedLockLostListener() {
			@Override
			public void onLost() {
				lockState = false;
			}
		};
	}

	public void lock() {
		if (!lockState) {
			lock.lockNotify(listener);
			lockState = true;
		}
	}

	public boolean tryLock() {
		if (!lockState) {
			lockState = lock.tryLockNotify(listener);
			return lockState;
		} else {
			return true;
		}
	}

	public boolean tryLock(long time, TimeUnit timeUnit) throws InterruptedException {
		if (!lockState) {
			lockState = lock.tryLockNotify(time, timeUnit, listener);
			return lockState;
		} else {
			return true;
		}
	}

	public void unlock() {
		lock.unlockNotify();
		lockState = false;
	}

	public void unlockQuietly() {
		DistributedLockUtils.unlockNotifyQuietly(lock);
		lockState = false;
	}

	public boolean isLocked() {
		return lockState;
	}
}
