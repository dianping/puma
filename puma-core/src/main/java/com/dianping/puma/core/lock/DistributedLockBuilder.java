package com.dianping.puma.core.lock;

public interface DistributedLockBuilder {

	public DistributedLock buildZkDistributedLock(String lockName);

	public DistributedLock buildLionDistributedLock(String lockName);
}
