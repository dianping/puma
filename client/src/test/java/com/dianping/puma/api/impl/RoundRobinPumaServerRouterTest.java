package com.dianping.puma.api.impl;

import com.dianping.puma.api.MockTest;
import com.dianping.puma.api.PumaServerMonitor;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.doReturn;

public class RoundRobinPumaServerRouterTest extends MockTest {

	RoundRobinPumaServerRouter router;

	@Mock
	PumaServerMonitor monitor;

	@Before
	public void before() {
		router = new RoundRobinPumaServerRouter(monitor);
	}

	@Test
	public void testNext() {
		List<String> hosts = new ArrayList<String>();
		hosts.add("0.0.0.0");
		hosts.add("1.1.1.1");
		hosts.add("2.2.2.2");
		doReturn(hosts).when(monitor).get();

		assertEquals("0.0.0.0", router.next());
		assertEquals("1.1.1.1", router.next());
		assertEquals("2.2.2.2", router.next());
		assertEquals("0.0.0.0", router.next());

		hosts.add("3.3.3.3");
		doReturn(hosts).when(monitor).get();
		assertEquals("1.1.1.1", router.next());
		assertEquals("2.2.2.2", router.next());
		assertEquals("3.3.3.3", router.next());
		assertEquals("0.0.0.0", router.next());

		hosts.clear();
		doReturn(hosts).when(monitor).get();
		assertNull(router.next());
	}
}