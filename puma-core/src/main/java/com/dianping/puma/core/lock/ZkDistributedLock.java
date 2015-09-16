package com.dianping.puma.core.lock;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;

public class ZkDistributedLock implements DistributedLock {

	private final Logger logger = LoggerFactory.getLogger(ZkDistributedLock.class);

	private final String lockName;

	private final InterProcessMutex lock;

	protected ZkDistributedLock(String lockName, CuratorFramework zkClient) {
		this.lockName = lockName;
		this.lock = new InterProcessMutex(zkClient, genLockPath(lockName));
	}

	@Override
	public void lock() {
		try {
			lock.acquire();
			logger.info("success to lock {}.", lockName);
		} catch (Exception e) {
			throw new RuntimeException("failed to lock " + lockName + ".", e);
		}
	}

	@Override
	public void lockInterruptibly() throws InterruptedException {

	}

	@Override
	public boolean tryLock() {
		return false;
	}

	@Override
	public boolean tryLock(long time, TimeUnit timeUnit) throws InterruptedException {
		try {
			if (lock.acquire(time, timeUnit)) {
				logger.info("success to try lock {}.", lockName);
				return true;
			} else {
				logger.info("failed to try lock {}.", lockName);
				return false;
			}
		} catch (Exception e) {
			throw new RuntimeException("failed to try lock " + lockName + ".", e);
		}
	}

	@Override
	public void unlock() {
		try {
			lock.release();
		} catch (Exception e) {
			throw new RuntimeException("failed to unlock " + lockName + ".", e);
		}
	}

	@Override
	public Condition newCondition() {
		return null;
	}

	protected String genLockPath(String lockName) {
		return "/dp/lock/puma/" + lockName;
	}
}
