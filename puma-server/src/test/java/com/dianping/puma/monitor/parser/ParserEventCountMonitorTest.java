package com.dianping.puma.monitor.parser;

import com.dianping.lion.client.ConfigCache;
import com.dianping.puma.MockTest;
import com.dianping.puma.core.monitor.EventMonitor;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.mockito.Mockito.*;

public class ParserEventCountMonitorTest extends MockTest {

	ParserEventCountMonitor parserEventCountMonitor = new ParserEventCountMonitor();

	@Mock
	EventMonitor eventMonitor;

	@Mock
	ConfigCache configCache;

	@Before
	public void init() {
		parserEventCountMonitor.setEventMonitor(eventMonitor);

		when(configCache.getLongProperty("puma.server.eventcount.parser.internal")).thenReturn(1000L);
		parserEventCountMonitor.setConfigCache(configCache);

		parserEventCountMonitor.init();
	}

	@Test
	public void testRecord() {
		for (int i = 0; i != 1000; ++i) {
			parserEventCountMonitor.record("name");
		}
		verify(eventMonitor, times(1000)).record("name", "0");
	}
}
