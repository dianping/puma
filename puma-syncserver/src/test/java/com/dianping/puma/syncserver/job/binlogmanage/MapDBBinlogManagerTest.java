package com.dianping.puma.syncserver.job.binlogmanage;

import com.dianping.puma.core.model.BinlogInfo;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class MapDBBinlogManagerTest {

	MapDBBinlogManager manager;

	@Before
	public void before() {
		manager = new MapDBBinlogManager(1, new BinlogInfo("mysql-bin.000001", 1L));
		manager.setFolderName("/data/appdatas/puma-syncserver/binlog/");
		manager.setName("test");
		manager.init();
		manager.start();
	}

	@After
	public void after() {
		manager.stop();
		manager.destroy();
	}

	@Test
	public void test() {
		manager.saveBreakpoint();
		manager.loadBreakpoint();
	}
}
