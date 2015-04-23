package com.dianping.puma.monitor;

import com.dianping.puma.monitor.todo.ClientLaggingMonitor;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ClientLaggingMonitorTest {

	ClientLaggingMonitor clientLaggingMonitor = new ClientLaggingMonitor();

	@Before
	public void before() {
		clientLaggingMonitor.setClientLaggingBinlogFileThreshold(8);
	}

	@Test
	public void testIsLagging()
			throws NoSuchMethodException, IllegalArgumentException, InvocationTargetException, IllegalAccessException {
		Method isLagging = ClientLaggingMonitor.class.getDeclaredMethod("isBinlogFileLagging", String.class, String.class);
		isLagging.setAccessible(true);

		String baseBinlogFile = "mysql-bin.000099";
		String binlogFile;

		binlogFile = "mysql-bin.000002";
		Assert.assertFalse((Boolean) isLagging.invoke(clientLaggingMonitor, baseBinlogFile, binlogFile));

		binlogFile = "mysql-bin.000099";
		Assert.assertFalse((Boolean) isLagging.invoke(clientLaggingMonitor, baseBinlogFile, binlogFile));

		binlogFile = "mysql-bin.000100";
		Assert.assertFalse((Boolean) isLagging.invoke(clientLaggingMonitor, baseBinlogFile, binlogFile));

		binlogFile = "mysql-bin.000107";
		Assert.assertTrue((Boolean) isLagging.invoke(clientLaggingMonitor, baseBinlogFile, binlogFile));

		binlogFile = "mysql-bin.000110";
		Assert.assertTrue((Boolean) isLagging.invoke(clientLaggingMonitor, baseBinlogFile, binlogFile));
	}
}
