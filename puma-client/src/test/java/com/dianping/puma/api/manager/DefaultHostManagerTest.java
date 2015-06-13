package com.dianping.puma.api.manager;

import com.dianping.lion.client.ConfigCache;
import com.dianping.puma.api.MockTest;
import com.dianping.puma.api.PumaClient;
import com.dianping.puma.api.manager.impl.DefaultHostManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

public class DefaultHostManagerTest extends MockTest {

	DefaultHostManager defaultHostManager;

	@Mock
	PumaClient client;

	@Mock
	ConfigCache configCache;

	@Before
	public void before() {
		defaultHostManager = new DefaultHostManager();

		defaultHostManager.setClient(client);
		defaultHostManager.setConfigCache(configCache);
	}

	@After
	public void after() {

	}

	@Test
	public void testNext() {

	}
}
