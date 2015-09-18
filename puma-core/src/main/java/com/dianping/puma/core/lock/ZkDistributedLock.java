package com.dianping.puma.core.lock;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;

public class ZkDistributedLock implements DistributedLock {

	private final Logger logger = LoggerFactory.getLogger(ZkDistributedLock.class);

	private final String lockName;

	private final InterProcessMutex lock;

	private final Deque<DistributedLockLostListener> listeners
			= new ArrayDeque<DistributedLockLostListener>();

	protected ZkDistributedLock(final String lockName, CuratorFramework zkClient) {
		this.lockName = lockName;
		this.lock = new InterProcessMutex(zkClient, genLockPath(lockName));

		zkClient.getConnectionStateListenable().addListener(new ConnectionStateListener() {
			@Override
			public void stateChanged(CuratorFramework client, ConnectionState newState) {
				if (newState.equals(ConnectionState.LOST) || newState.equals(ConnectionState.SUSPENDED)) {
					logger.info("zookeeper connection lost or suspend for lock `{}`.", lockName);

					trigger();
				}
			}
		});
	}

	@Override
	public void lock() {
		try {
			lock.acquire();
			logger.info("success to lock `{}`.", lockName);
		} catch (Exception e) {
			throw new RuntimeException("failed to lock `" + lockName + "`.", e);
		}
	}

	@Override
	public void lockNotify(final DistributedLockLostListener listener) {
		lock();
		push(listener);
	}

	@Override
	public void lockInterruptibly() throws InterruptedException {

	}

	@Override
	public boolean tryLock() {
		try {
			if (lock.acquire(0, TimeUnit.SECONDS)) {
				logger.info("success to try lock `{}`.", lockName);
				return true;
			} else {
				logger.info("failed to try lock `{}`.", lockName);
				return false;
			}
		} catch (Exception e) {
			throw new RuntimeException("failed to try lock " + lockName + ".", e);
		}
	}

	@Override
	public boolean tryLockNotify(DistributedLockLostListener listener) {
		boolean result = tryLock();
		push(listener);
		return result;
	}

	@Override
	public boolean tryLock(long time, TimeUnit timeUnit) throws InterruptedException {
		try {
			if (lock.acquire(time, timeUnit)) {
				logger.info("success to try lock `{}`.", lockName);
				return true;
			} else {
				logger.info("failed to try lock `{}`.", lockName);
				return false;
			}
		} catch (InterruptedException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException("failed to try lock " + lockName + ".", e);
		}
	}

	@Override
	public boolean tryLockNotify(long time, TimeUnit timeUnit, DistributedLockLostListener listener) throws InterruptedException {
		boolean result = tryLock(time, timeUnit);
		push(listener);
		return result;
	}

	@Override
	public void unlock() {
		try {
			lock.release();
			logger.info("success to unlock `{}`.", lockName);
		} catch (Exception e) {
			throw new RuntimeException("failed to unlock `" + lockName + "`.", e);
		}
	}

	@Override
	public void unlockNotify() {
		unlock();
		pop();
	}

	@Override
	public Condition newCondition() {
		return null;
	}

	protected String genLockPath(String lockName) {
		return "/dp/lock/puma/" + lockName;
	}

	protected void push(DistributedLockLostListener listener) {
		listeners.push(listener);
	}

	protected synchronized void trigger() {
		List<DistributedLockLostListener> triggers = new ArrayList<DistributedLockLostListener>();

		while (listeners.size() != 0) {
			DistributedLockLostListener listener = listeners.pop();
			if (!triggers.contains(listener)) {
				triggers.add(listener);
			}
		}

		for (DistributedLockLostListener listener: triggers) {
			try {
				listener.onLost();
			} catch (Throwable t) {
				logger.error("failed to call lock lost listener for lock `{}`.", lockName, t);
			}
		}
	}

	protected void pop() {
		listeners.pollLast();
	}
}
