package com.dianping.puma.api.manager;

import com.dianping.lion.client.ConfigCache;
import com.dianping.puma.api.MockTest;
import com.dianping.puma.api.PumaClient;
import com.dianping.puma.api.config.Config;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

public class HostManagerTest extends MockTest {

	HostManager hostManager;

	@Mock
	PumaClient client;

	@Mock
	Config config;

	@Mock
	ConfigCache configCache;

	@Before
	public void before() {
		hostManager = new HostManager();

		hostManager.setClient(client);
		hostManager.setConfig(config);
		hostManager.setConfigCache(configCache);
	}

	@After
	public void after() {

	}

	@Test
	public void testNext() {

	}
}
