package com.dianping.puma.monitor;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class BinlogFileLaggingMonitorTest {

	BinlogFileLaggingMonitor binlogFileLaggingMonitor = new BinlogFileLaggingMonitor();

	@Before
	public void before() {
		binlogFileLaggingMonitor.setBinlogFileLaggingThreshold(8);
	}

	@Test
	public void testIsLagging()
			throws NoSuchMethodException, IllegalArgumentException, InvocationTargetException, IllegalAccessException {
		Method isLagging = BinlogFileLaggingMonitor.class.getDeclaredMethod("isLagging", String.class, String.class);
		isLagging.setAccessible(true);

		String baseBinlogFile = "mysql-bin.000099";
		String binlogFile;

		binlogFile = "mysql-bin.000002";
		Assert.assertFalse((Boolean) isLagging.invoke(binlogFileLaggingMonitor, baseBinlogFile, binlogFile));

		binlogFile = "mysql-bin.000099";
		Assert.assertFalse((Boolean) isLagging.invoke(binlogFileLaggingMonitor, baseBinlogFile, binlogFile));

		binlogFile = "mysql-bin.000100";
		Assert.assertFalse((Boolean) isLagging.invoke(binlogFileLaggingMonitor, baseBinlogFile, binlogFile));

		binlogFile = "mysql-bin.000107";
		Assert.assertTrue((Boolean) isLagging.invoke(binlogFileLaggingMonitor, baseBinlogFile, binlogFile));

		binlogFile = "mysql-bin.000110";
		Assert.assertTrue((Boolean) isLagging.invoke(binlogFileLaggingMonitor, baseBinlogFile, binlogFile));
	}
}
