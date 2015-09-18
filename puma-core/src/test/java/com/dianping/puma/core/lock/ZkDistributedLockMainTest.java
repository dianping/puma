package com.dianping.puma.core.lock;

import com.google.common.util.concurrent.Uninterruptibles;
import org.apache.log4j.BasicConfigurator;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ZkDistributedLockMainTest {


	private static ExecutorService pool = Executors.newCachedThreadPool();

	private static boolean keepOn = true;

	public static void main(String args[]) {

		BasicConfigurator.configure();

		DistributedLock lock = DistributedLockFactory.newZkDistributedLock("dozer-debug");

		lock.lockNotify(new DistributedLockLostListener() {
			@Override public void onLost() {
				System.out.println("######################");
			}
		});

		Uninterruptibles.sleepUninterruptibly(100, TimeUnit.SECONDS);
	}
}
