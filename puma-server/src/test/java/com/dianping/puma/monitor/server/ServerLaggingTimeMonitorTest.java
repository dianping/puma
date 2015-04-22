package com.dianping.puma.monitor.server;

import com.dianping.puma.core.exception.ConfigException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ServerLaggingTimeMonitorTest {

	ServerLaggingTimeMonitor serverLaggingTimeMonitor = new ServerLaggingTimeMonitor();

	@Before
	public void before() throws ConfigException {
		serverLaggingTimeMonitor.init();
	}

	@Test
	public void testGenStatus() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
		Method genStatus = ServerLaggingTimeMonitor.class.getDeclaredMethod("genStatus", long.class);
		genStatus.setAccessible(true);

		long diff = 1;
		serverLaggingTimeMonitor.setServerLaggingTimeThreshold(diff);
		long time = System.currentTimeMillis();

		String result = (String) genStatus.invoke(serverLaggingTimeMonitor, time);
		String expected = "0";
		Assert.assertEquals(expected, result);

		serverLaggingTimeMonitor.record("helo", time);
	}
}
