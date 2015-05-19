package com.dianping.puma.syncserver.job.binlogmanage;

import com.dianping.puma.core.model.BinlogInfo;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class MapDBBinlogManagerTest {

	MapDBBinlogManager binlogManager;

	@Before
	public void before() {
		binlogManager = new MapDBBinlogManager();
		binlogManager.setName("puma");
		binlogManager.start();
	}

	@After
	public void after() {
		binlogManager.delete();
		binlogManager.stop();
	}

	@Test
	public void testGetEarliest() {
		BinlogInfo binlogInfo0 = new BinlogInfo("mysql-bin.000001", 4L);
		BinlogInfo binlogInfo1 = new BinlogInfo("mysql-bin.000002", 4L);
		BinlogInfo binlogInfo2 = new BinlogInfo("mysql-bin.000002", 10L);
		BinlogInfo binlogInfo3 = new BinlogInfo("mysql-bin.000002", 5L);

		binlogManager.before(binlogInfo0);
		binlogManager.before(binlogInfo1);
		binlogManager.before(binlogInfo2);

		binlogManager.after(binlogInfo0);
		Assert.assertEquals(binlogInfo1, binlogManager.getEarliest());

		binlogManager.after(binlogInfo1);
		Assert.assertEquals(binlogInfo2, binlogManager.getEarliest());

		binlogManager.before(binlogInfo3);
		Assert.assertEquals(binlogInfo3, binlogManager.getEarliest());

		binlogManager.after(binlogInfo2);
		Assert.assertEquals(binlogInfo3, binlogManager.getEarliest());
	}
}
