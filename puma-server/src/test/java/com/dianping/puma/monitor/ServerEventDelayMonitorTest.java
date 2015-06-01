package com.dianping.puma.monitor;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ServerEventDelayMonitorTest {

	ServerEventDelayMonitor serverEventDelayMonitor = new ServerEventDelayMonitor();

	@Before
	public void before() {
		serverEventDelayMonitor.init();
	}

	@After
	public void after() {
		serverEventDelayMonitor.destroy();
	}

	@Test
	public void test() {
		serverEventDelayMonitor.record("puma-test", 1L);

		try {
			Thread.sleep(50000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
