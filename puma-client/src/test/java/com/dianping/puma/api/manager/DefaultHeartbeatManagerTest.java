package com.dianping.puma.api.manager;

import com.dianping.puma.api.MockTest;
import com.dianping.puma.api.PumaClient;
import com.dianping.puma.api.manager.impl.DefaultHeartbeatManager;
import com.dianping.puma.api.util.Clock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.mockito.Mockito.when;

public class DefaultHeartbeatManagerTest extends MockTest {

	DefaultHeartbeatManager defaultHeartbeatManager = new DefaultHeartbeatManager();

	@Mock
	PumaClient client;


	@Mock
	Clock clock;

	@Before
	public void before() {
		defaultHeartbeatManager.setClient(client);

		when(clock.getCurrentTime()).thenReturn(0L);
		defaultHeartbeatManager.setClock(clock);

		defaultHeartbeatManager.start();
	}

	@After
	public void after() {
		defaultHeartbeatManager.stop();
	}

	@Test
	public void test() {

	}
}
