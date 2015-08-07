package com.dianping.puma.api.impl;

import com.dianping.puma.api.MockTest;
import com.dianping.puma.api.PumaServerMonitor;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Spy;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class MonitorBasedPumaServerRouterTest extends MockTest {

	@Spy
	MonitorBasedPumaServerRouter router;

	@Mock
	PumaServerMonitor monitor;

	@SuppressWarnings("unchecked")
	@Before
	public void before() {
		List<String> serverHosts = new ArrayList<String>();
		serverHosts.add("1.0.0.0");
		serverHosts.add("2.0.0.0");
		serverHosts.add("3.0.0.0");

		when(monitor.fetch(anyString(), anyList())).thenReturn(serverHosts);
		router.monitor = monitor;
	}

	@Test(expected = RuntimeException.class)
	public void testNext() {
		when(router.isFrozenExpired(anyLong(), anyLong())).thenReturn(true);

		assertEquals("1.0.0.0", router.next(null, null));
		assertEquals("1.0.0.0", router.next(null, null));
		assertEquals("1.0.0.0", router.next(null, null));
		assertEquals("1.0.0.0", router.next(null, null));

		when(router.isFrozenExpired(anyLong(), anyLong())).thenReturn(false);

		assertEquals("2.0.0.0", router.next(null, null));
		assertEquals("3.0.0.0", router.next(null, null));

		// should throw runtime exception.
		router.next(null, null);
	}
}