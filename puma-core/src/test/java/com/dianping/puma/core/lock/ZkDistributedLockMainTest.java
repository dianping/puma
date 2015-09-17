package com.dianping.puma.core.lock;

import com.google.common.util.concurrent.Uninterruptibles;

import java.util.concurrent.TimeUnit;

public class ZkDistributedLockMainTest {

	public static void main(String args[]) {
		System.out.println("start main...");

		DistributedLock lock = DistributedLockFactory.newZkDistributedLock("dozer-debug");

		System.out.println("start locking...");

		lock.lock();

		System.out.println("success to lock.");

		Uninterruptibles.sleepUninterruptibly(60, TimeUnit.SECONDS);

		System.out.println("start unlocking...");

		lock.unlock();

		System.out.println("success to unlock.");
	}
}
