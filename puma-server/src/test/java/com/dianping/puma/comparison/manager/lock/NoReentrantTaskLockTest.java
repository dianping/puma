package com.dianping.puma.comparison.manager.lock;

import com.dianping.puma.MockTest;
import com.dianping.puma.biz.entity.CheckTaskEntity;
import com.dianping.puma.comparison.manager.container.CheckTaskContainer;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class NoReentrantTaskLockTest extends MockTest {

	NoReentrantTaskLock taskLock;

	@Mock
	CheckTaskContainer checkTaskContainer;

	@Mock
	CheckTaskEntity checkTask;

	@Before
	public void before() {
		taskLock = new NoReentrantTaskLock();
		taskLock.setCheckTask(checkTask);
		taskLock.setCheckTaskContainer(checkTaskContainer);
	}

	@Test
	public void testTryLock() throws Exception {
		doReturn(1).when(checkTask).getId();
		doReturn(false).when(checkTaskContainer).contains(1);
		assertTrue(taskLock.tryLock());
		verify(checkTaskContainer, times(1)).create(checkTask);

		doReturn(true).when(checkTaskContainer).contains(1);
		assertFalse(taskLock.tryLock());
		verify(checkTaskContainer, times(1)).create(checkTask);
	}

	@Test
	public void testUnlock() throws Exception {
		doReturn(1).when(checkTask).getId();

		taskLock.unlock();
		verify(checkTaskContainer, times(1)).remove(1);
	}
}