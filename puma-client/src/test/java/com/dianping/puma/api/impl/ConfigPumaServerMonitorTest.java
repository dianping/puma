package com.dianping.puma.api.impl;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class ConfigPumaServerMonitorTest {

	ConfigPumaServerMonitor monitor;

	@Before
	public void before() {
		monitor = mock(ConfigPumaServerMonitor.class);
		doCallRealMethod().when(monitor).parseServers(anyString());
	}

	@Test
	public void testParseServers() {
		String node0 = "0.0.0.0#1.0.0.0#2.0.0.0";
		List<String> expected0 = new ArrayList<String>();
		expected0.add("0.0.0.0");
		expected0.add("1.0.0.0");
		expected0.add("2.0.0.0");

		List<String> result0 = monitor.parseServers(node0);
		assertEquals(expected0, result0);

		String node1 = "0.0.0.0#1.0.0.0#2.0.0.0";
		List<String> expected1 = new ArrayList<String>();
		expected1.add("0.0.0.0");
		expected1.add("1.0.0.0");
		expected1.add("2.0.0.0");

		List<String> result1 = monitor.parseServers(node1);
		assertEquals(expected1, result1);

		String node2 = "0.0.0.0";
		List<String> expected2 = new ArrayList<String>();
		expected2.add("0.0.0.0");

		List<String> result2 = monitor.parseServers(node2);
		assertEquals(expected2, result2);
	}
}