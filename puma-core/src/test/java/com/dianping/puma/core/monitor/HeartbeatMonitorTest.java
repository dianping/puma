package com.dianping.puma.core.monitor;

import com.dianping.puma.core.MockTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

public class HeartbeatMonitorTest extends MockTest {

	HeartbeatMonitor heartbeatMonitor = new HeartbeatMonitor();

	@Mock
	Monitor monitor;

	@Before
	public void before() {
		heartbeatMonitor.setType("type");
		heartbeatMonitor.setCountThreshold(1000L);
		heartbeatMonitor.setMonitor(monitor);
		heartbeatMonitor.start();
	}

	@Test
	public void testRecord() {

	}
}
