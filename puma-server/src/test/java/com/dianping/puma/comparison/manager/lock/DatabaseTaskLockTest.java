package com.dianping.puma.comparison.manager.lock;

import com.dianping.puma.MockTest;
import com.dianping.puma.biz.entity.CheckTaskEntity;
import com.dianping.puma.biz.service.CheckTaskService;
import com.dianping.puma.comparison.manager.server.CheckTaskServerManager;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Spy;

import java.util.Date;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class DatabaseTaskLockTest extends MockTest {

	@Spy
	DatabaseTaskLock taskLock;

	@Mock
	CheckTaskEntity checkTask;

	@Mock
	CheckTaskService checkTaskService;

	@Mock
	CheckTaskServerManager checkTaskServerManager;

	@Before
	public void before() {
		taskLock.setCheckTask(checkTask);
		taskLock.setCheckTaskService(checkTaskService);
		taskLock.setCheckTaskServerManager(checkTaskServerManager);
	}

	@Test
	public void testTryLock() throws Exception {
		doReturn("127.0.0.1").when(checkTaskServerManager).findFirstAuthorizedHost();
		doReturn(1).when(checkTask).getId();
		doReturn(1).when(checkTaskService).update(any(CheckTaskEntity.class));
		doReturn(true).when(taskLock).tryLock0(any(CheckTaskEntity.class));

		CheckTaskEntity checkTask0 = new CheckTaskEntity();
		checkTask0.setRunning(true);
		checkTask0.setOwnerHost("127.0.0.1");
		doReturn(checkTask0).when(checkTaskService).findById(anyInt());
		doReturn(false).when(taskLock).isTimeout(any(Date.class));
		assertTrue(taskLock.tryLock());

		CheckTaskEntity checkTask1 = new CheckTaskEntity();
		checkTask1.setRunning(true);
		checkTask1.setOwnerHost("127.0.0.1");
		doReturn(checkTask1).when(checkTaskService).findById(anyInt());
		doReturn(true).when(taskLock).isTimeout(any(Date.class));
		assertTrue(taskLock.tryLock());

		CheckTaskEntity checkTask2 = new CheckTaskEntity();
		checkTask2.setRunning(true);
		checkTask2.setOwnerHost("127.0.0.0");
		doReturn(checkTask2).when(checkTaskService).findById(anyInt());
		doReturn(false).when(taskLock).isTimeout(any(Date.class));
		assertFalse(taskLock.tryLock());

		CheckTaskEntity checkTask3 = new CheckTaskEntity();
		checkTask3.setRunning(true);
		checkTask3.setOwnerHost("127.0.0.0");
		doReturn(checkTask3).when(checkTaskService).findById(anyInt());
		doReturn(true).when(taskLock).isTimeout(any(Date.class));
		assertTrue(taskLock.tryLock());

		CheckTaskEntity checkTask4 = new CheckTaskEntity();
		checkTask4.setRunning(false);
		checkTask4.setOwnerHost("127.0.0.1");
		doReturn(checkTask4).when(checkTaskService).findById(anyInt());
		doReturn(false).when(taskLock).isTimeout(any(Date.class));
		assertTrue(taskLock.tryLock());

		CheckTaskEntity checkTask5 = new CheckTaskEntity();
		checkTask5.setRunning(false);
		checkTask5.setOwnerHost("127.0.0.1");
		doReturn(checkTask5).when(checkTaskService).findById(anyInt());
		doReturn(true).when(taskLock).isTimeout(any(Date.class));
		assertTrue(taskLock.tryLock());

		CheckTaskEntity checkTask6 = new CheckTaskEntity();
		checkTask6.setRunning(false);
		checkTask6.setOwnerHost("127.0.0.0");
		doReturn(checkTask6).when(checkTaskService).findById(anyInt());
		doReturn(false).when(taskLock).isTimeout(any(Date.class));
		assertTrue(taskLock.tryLock());

		CheckTaskEntity checkTask7 = new CheckTaskEntity();
		checkTask7.setRunning(false);
		checkTask7.setOwnerHost("127.0.0.0");
		doReturn(checkTask7).when(checkTaskService).findById(anyInt());
		doReturn(true).when(taskLock).isTimeout(any(Date.class));
		assertTrue(taskLock.tryLock());
	}

	@Test
	public void testUnlock() throws Exception {

	}
}