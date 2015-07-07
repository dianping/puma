package com.dianping.puma.monitor;

import com.dianping.lion.client.ConfigCache;
import com.dianping.puma.MockTest;
import com.dianping.puma.biz.monitor.EventMonitor;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class StorageEventCountMonitorTest extends MockTest {

	StorageEventCountMonitor storageEventCountMonitor = new StorageEventCountMonitor();

	@Mock
	EventMonitor eventMonitor;

	@Mock
	ConfigCache configCache;

	@Before
	public void init() {
		storageEventCountMonitor.setEventMonitor(eventMonitor);

		when(configCache.getLongProperty("puma.server.eventcount.storage.internal")).thenReturn(1000L);
		storageEventCountMonitor.setConfigCache(configCache);

		storageEventCountMonitor.init();
	}

	@Test
	public void testRecord() {
		for (int i = 0; i != 1000; ++i) {
			storageEventCountMonitor.record("name");
		}
		verify(eventMonitor, times(1000)).record("name", "0");
	}
}
