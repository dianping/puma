package com.dianping.puma.core.lock;

import com.google.common.util.concurrent.Uninterruptibles;

import java.util.concurrent.TimeUnit;

public class ZkDistributedLockMainTest2 {

	public static void main(String args[]) {
		System.out.println("start main...");

		DistributedLock lock = DistributedLockFactory.newZkDistributedLock("puma-test");

		System.out.println("start locking...");

		lock.lock();

		System.out.println("success to lock.");

		Uninterruptibles.sleepUninterruptibly(30, TimeUnit.SECONDS);

		System.out.println("start unlocking...");

		lock.unlock();

		System.out.println("success to unlock.");
	}
}
