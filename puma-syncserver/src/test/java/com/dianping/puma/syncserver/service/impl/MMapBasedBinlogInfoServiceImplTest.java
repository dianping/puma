package com.dianping.puma.syncserver.service.impl;

import com.dianping.puma.core.sync.model.BinlogInfo;
import junit.framework.Assert;
import junit.framework.TestCase;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class MMapBasedBinlogInfoServiceImplTest extends TestCase {

	MMapBasedBinlogInfoServiceImpl mMapBasedBinlogInfoService = new MMapBasedBinlogInfoServiceImpl();

	public void setUp() throws Exception {
		super.setUp();
		mMapBasedBinlogInfoService.setBaseDir("/data/appdatas/puma/syncserver/");
		mMapBasedBinlogInfoService.init();
	}

	public void tearDown() throws Exception {

	}

	@Test
	public void testGetBinlogInfo() throws Exception {
		BinlogInfo binlogInfo = new BinlogInfo();
		binlogInfo.setBinlogFile("a");
		binlogInfo.setBinlogPosition(999888);
		mMapBasedBinlogInfoService.saveBinlogInfo("no-1", binlogInfo);
		Assert.assertEquals(binlogInfo, mMapBasedBinlogInfoService.getBinlogInfo("no-1"));
	}

	@Test
	public void testSaveBinlogInfo() throws Exception {
		BinlogInfo binlogInfo = new BinlogInfo();
		binlogInfo.setBinlogFile("b");
		binlogInfo.setBinlogPosition(987);
		mMapBasedBinlogInfoService.saveBinlogInfo("no-2", binlogInfo);
		Assert.assertEquals(binlogInfo, mMapBasedBinlogInfoService.getBinlogInfo("no-2"));
	}

	@Test
	public void testRemoveBinlogInfo() throws Exception {
		mMapBasedBinlogInfoService.removeBinlogInfo("no-2");
		Assert.assertEquals(null, mMapBasedBinlogInfoService.getBinlogInfo("no-2"));
	}

	@Test
	public void testFindSyncTaskIds() throws Exception {
		List<String> syncTaskClientNames = new ArrayList<String>();
		syncTaskClientNames.add("no-1");
		Assert.assertEquals(syncTaskClientNames, mMapBasedBinlogInfoService.findSyncTaskClientNames());
	}
}