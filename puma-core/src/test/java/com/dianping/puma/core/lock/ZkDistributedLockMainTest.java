package com.dianping.puma.core.lock;

import org.apache.log4j.BasicConfigurator;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ZkDistributedLockMainTest {


	private static ExecutorService pool = Executors.newCachedThreadPool();

	private static boolean keepOn = true;

	public static void main(String args[]) {

		BasicConfigurator.configure();

		DistributedLock lock = DistributedLockFactory.newZkDistributedLock("dozer-debug");

		lock.lockNotify(new DistributedLockLostListener() {
			@Override
			public void onLost() {
			}
		});

		lock.lock();

		System.out.println("lock lock.");

		lock.unlock();
		lock.unlock();

		System.out.println("lock unlock.");

		DistributedLock lock1 = DistributedLockFactory.newZkDistributedLock("dozer-debug");
		lock1.lock();

		System.out.println("lock1 lock.");

	}
}
