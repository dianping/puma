package com.dianping.puma.api.manager;

import com.dianping.puma.api.MockTest;
import com.dianping.puma.api.PumaClient;
import com.dianping.puma.api.config.Config;
import com.dianping.puma.api.util.Clock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.mockito.Mockito.when;

public class HeartbeatManagerTest extends MockTest {

	HeartbeatManager heartbeatManager = new HeartbeatManager();

	@Mock
	PumaClient client;

	@Mock
	Config config;

	@Mock
	Clock clock;

	@Before
	public void before() {
		heartbeatManager.setClient(client);

		when(config.getHeartbeatCheckTime()).thenReturn(60L);
		when(config.getHeartbeatExpiredTime()).thenReturn(120L);
		heartbeatManager.setConfig(config);

		when(clock.getCurrentTime()).thenReturn(0L);
		heartbeatManager.setClock(clock);

		heartbeatManager.start();
	}

	@After
	public void after() {
		heartbeatManager.stop();
	}

	@Test
	public void test() {

	}
}
