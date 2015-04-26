package com.dianping.puma.monitor;

import com.dianping.puma.core.exception.ConfigException;
import com.dianping.puma.monitor.ServerEventDelayMonitor;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ServerEventDelayMonitorTest {

	ServerEventDelayMonitor serverEventDelayMonitor = new ServerEventDelayMonitor();

	@Before
	public void before() throws ConfigException {
		serverEventDelayMonitor.init();
	}

	@Test
	public void testGenStatus() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
	}
}
