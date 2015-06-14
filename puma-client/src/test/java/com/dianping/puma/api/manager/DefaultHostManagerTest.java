package com.dianping.puma.api.manager;

import com.dianping.lion.client.ConfigCache;
import com.dianping.puma.api.MockTest;
import com.dianping.puma.api.PumaClient;
import com.dianping.puma.api.config.Config;
import com.dianping.puma.api.manager.impl.DefaultHostManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class DefaultHostManagerTest extends MockTest {

	HostManager hostManager;

	@Mock
	PumaClient client;

	@Mock
	Config config;

	@Mock
	ConfigCache configCache;

	@Before
	public void before() {
		DefaultHostManager defaultHostManager = new DefaultHostManager();

		defaultHostManager.setClient(client);

		when(config.getReconnectCount()).thenReturn(2);
		defaultHostManager.setConfig(config);

		when(configCache.getProperty(anyString())).thenReturn("1.1.1.1:8888,1.0.0.0:8080,1.0.1.0:8070");
		defaultHostManager.setConfigCache(configCache);

		hostManager = defaultHostManager;
		hostManager.start();
	}

	@After
	public void after() {
		hostManager.stop();
	}

	@Test
	public void testNextAndCurrent() {
		// Case 1: Return "1.1.1.1:8888".
		hostManager.feedback(Feedback.INITIAL);
		assertNull(hostManager.current());
		assertEquals("1.1.1.1:8888", hostManager.next());

		// Case 2: Return "1.1.1.1:8888".
		hostManager.feedback(Feedback.NET_ERROR);
		assertEquals("1.1.1.1:8888", hostManager.current());
		assertEquals("1.1.1.1:8888", hostManager.next());

		// Case 3: Return "1.1.1.1:8888".
		hostManager.feedback(Feedback.NET_ERROR);
		assertEquals("1.1.1.1:8888", hostManager.current());
		assertEquals("1.1.1.1:8888", hostManager.next());

		// Case 4: Return "1.0.0.0:8080".
		hostManager.feedback(Feedback.NET_ERROR);
		assertEquals("1.1.1.1:8888", hostManager.current());
		assertEquals("1.0.0.0:8080", hostManager.next());

		// Case 5: Return "1.0.1.0:8070".
		hostManager.feedback(Feedback.SERVER_ERROR);
		assertEquals("1.0.0.0:8080", hostManager.current());
		assertEquals("1.0.1.0:8070", hostManager.next());

		// Case 6: Return "1.0.1.0:8070"
		hostManager.feedback(Feedback.NET_ERROR);
		assertEquals("1.0.1.0:8070", hostManager.current());
		assertEquals("1.0.1.0:8070", hostManager.next());

		// Case 7: Return "1.0.1.0:8070"
		hostManager.feedback(Feedback.NET_ERROR);
		assertEquals("1.0.1.0:8070", hostManager.current());
		assertEquals("1.0.1.0:8070", hostManager.next());

		// Case 8: Return "1.0.1.0:8070"
		hostManager.feedback(Feedback.SUCCESS);
		assertEquals("1.0.1.0:8070", hostManager.current());
		assertEquals("1.0.1.0:8070", hostManager.next());

		// Case 9: Return "1.0.1.0:8070"
		hostManager.feedback(Feedback.NET_ERROR);
		assertEquals("1.0.1.0:8070", hostManager.current());
		assertEquals("1.0.1.0:8070", hostManager.next());

		// Case 10: Return "1.0.1.0:8070"
		hostManager.feedback(Feedback.NET_ERROR);
		assertEquals("1.0.1.0:8070", hostManager.current());
		assertEquals("1.0.1.0:8070", hostManager.next());

		// Case 11: Return "1.1.1.1:8888"
		hostManager.feedback(Feedback.NET_ERROR);
		assertEquals("1.0.1.0:8070", hostManager.current());
		assertEquals("1.1.1.1:8888", hostManager.next());
	}
}
