package com.dianping.puma.monitor;

import com.dianping.lion.client.ConfigCache;
import com.dianping.puma.MockTest;
import com.dianping.puma.core.monitor.EventMonitor;
import com.dianping.puma.monitor.FetcherEventCountMonitor;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.mockito.Mockito.*;

public class FetcherEventCountMonitorTest extends MockTest {

	FetcherEventCountMonitor fetcherEventCountMonitor = new FetcherEventCountMonitor();

	@Mock
	EventMonitor eventMonitor;

	@Mock
	ConfigCache configCache;

	@Before
	public void before() {
		fetcherEventCountMonitor.setEventMonitor(eventMonitor);

		when(configCache.getLongProperty("puma.server.eventcount.fetcher.internal")).thenReturn(1000L);
		fetcherEventCountMonitor.setConfigCache(configCache);

		fetcherEventCountMonitor.init();
	}

	@Test
	public void testRecord() {
		for (int i = 0; i != 1000; ++i) {
			fetcherEventCountMonitor.record("name");
		}
		verify(eventMonitor, times(1000)).record("name", "0");
	}
}
