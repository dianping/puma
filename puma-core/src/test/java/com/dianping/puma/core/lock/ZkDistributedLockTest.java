package com.dianping.puma.core.lock;

import com.google.common.util.concurrent.Uninterruptibles;
import org.apache.curator.test.TestingServer;
import org.apache.log4j.BasicConfigurator;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class ZkDistributedLockTest {

	static {
		BasicConfigurator.configure();
	}

	@Test
	public void testLock() throws Exception {
		TestingServer server = new TestingServer();

		DistributedLock lock0 = DistributedLockFactory.newTestZkDistributedLock("test-0", server);
		DistributedLock lock1 = DistributedLockFactory.newTestZkDistributedLock("test-0", server);

		lock0.lock();
		assertFalse(lock1.tryLock());

		lock0.unlock();
		assertTrue(lock1.tryLock());
		assertTrue(lock1.tryLock());
		assertFalse(lock0.tryLock());

		lock1.unlock();
		assertFalse(lock0.tryLock());

		lock1.unlock();
		assertTrue(lock0.tryLock());
	}

	@Test
	public void testLockNotify() throws Exception {
		TestingServer server = new TestingServer();

		DistributedLock lock0 = DistributedLockFactory.newTestZkDistributedLock("test-1", server);

		assertTrue(lock0.tryLockNotify(new DistributedLockLostListener() {
			@Override public void onLost() {
				assertTrue(true);
			}
		}));

		server.stop();

		Uninterruptibles.sleepUninterruptibly(5, TimeUnit.SECONDS);
	}
}