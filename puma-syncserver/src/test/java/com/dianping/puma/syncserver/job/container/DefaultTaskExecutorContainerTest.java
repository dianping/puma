package com.dianping.puma.syncserver.job.container;

import com.dianping.puma.syncserver.job.container.exception.TECException;
import com.dianping.puma.syncserver.job.executor.SyncTaskExecutor;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

public class DefaultTaskExecutorContainerTest {

	DefaultTaskExecutorContainer container;

	@Before
	public void before() {
		container = new DefaultTaskExecutorContainer();
	}

	@Test
	public void testSubmit() {
		// Submit "puma".
		container.submit("puma", new SyncTaskExecutor());
		assertEquals(1, container.size());

		// Submit "hello".
		container.submit("hello", new SyncTaskExecutor());
		assertEquals(2, container.size());
	}

	@Test(expected = TECException.class)
	public void testSubmit1() {
		// Submit "puma".
		container.submit("puma", new SyncTaskExecutor());
		assertEquals(1, container.size());

		// Submit "puma".
		container.submit("puma", new SyncTaskExecutor());
	}

	@Test
	public void testWithdraw() {
		// Withdraw "puma"
		container.submit("puma", new SyncTaskExecutor());
		container.withdraw("puma");
		assertEquals(0, container.size());

		// Withdraw "puma".
		container.withdraw("puma");
		assertEquals(0, container.size());
	}
}
