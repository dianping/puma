package com.dianping.puma.syncserver.job.binlogmanage;

import com.dianping.puma.core.model.BinlogInfo;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class MapDBBinlogManagerTest {

	@Test
	public void testGetRecoveryPoint() {
		MapDBBinlogManager binlogManager = new MapDBBinlogManager(-3, new BinlogInfo("mysql-bin.000001", 4L));
		binlogManager.setName("puma");

		binlogManager.start();
		binlogManager.cleanup();
		binlogManager.start();

		BinlogInfo expected, result;

		expected = new BinlogInfo("mysql-bin.000001", 4L);
		result = binlogManager.getBinlogInfo();
		assertTrue(EqualsBuilder.reflectionEquals(expected, result));

		// (000002, 4L).
		binlogManager.before(-3, new BinlogInfo("mysql-bin.000002", 4L));
		expected = new BinlogInfo("mysql-bin.000002", 4L);
		result = binlogManager.getBinlogInfo();
		assertTrue(EqualsBuilder.reflectionEquals(expected, result));

		// (000002, 4L), (000002, 10L).
		binlogManager.before(-3, new BinlogInfo("mysql-bin.000002", 10L));
		expected = new BinlogInfo("mysql-bin.000002", 4L);
		result = binlogManager.getBinlogInfo();
		assertTrue(EqualsBuilder.reflectionEquals(expected, result));

		// (000002, 10L).
		binlogManager.after(-3, new BinlogInfo("mysql-bin.000002", 4L));
		expected = new BinlogInfo("mysql-bin.000002", 10L);
		result = binlogManager.getBinlogInfo();
		assertTrue(EqualsBuilder.reflectionEquals(expected, result));

		// (000002, 10L), (000002, 12L).
		binlogManager.before(-3, new BinlogInfo("mysql-bin.000002", 12L));
		expected = new BinlogInfo("mysql-bin.000002", 10L);
		result = binlogManager.getBinlogInfo();
		assertTrue(EqualsBuilder.reflectionEquals(expected, result));

		// (000002, 10L).
		binlogManager.after(-3, new BinlogInfo("mysql-bin.000002", 12L));
		expected = new BinlogInfo("mysql-bin.000002", 10L);
		result = binlogManager.getBinlogInfo();
		assertTrue(EqualsBuilder.reflectionEquals(expected, result));

		// null.
		binlogManager.after(-3, new BinlogInfo("mysql-bin.000002", 10L));
		expected = new BinlogInfo("mysql-bin.000002", 12L);
		result = binlogManager.getBinlogInfo();
		assertTrue(EqualsBuilder.reflectionEquals(expected, result));
	}
}
