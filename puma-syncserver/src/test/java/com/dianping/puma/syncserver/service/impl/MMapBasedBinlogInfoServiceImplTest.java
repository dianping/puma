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
		mMapBasedBinlogInfoService.setBaseDir("data/appdata/puma/syncserver");
		mMapBasedBinlogInfoService.init();
	}

	public void tearDown() throws Exception {

	}

	@Test
	public void testInit() throws Exception {
		BinlogInfo binlogInfo = new BinlogInfo();
		binlogInfo.setBinlogFile("a");
		binlogInfo.setBinlogPosition(987);

		Assert.assertEquals(binlogInfo, mMapBasedBinlogInfoService.getBinlogInfo(1230));
	}

	@Test
	public void testGetBinlogInfo() throws Exception {
		BinlogInfo binlogInfo = new BinlogInfo();
		binlogInfo.setBinlogFile("a");
		binlogInfo.setBinlogPosition(999888777);
		mMapBasedBinlogInfoService.saveBinlogInfo(123, binlogInfo);
		Assert.assertEquals(binlogInfo, mMapBasedBinlogInfoService.getBinlogInfo(123));
	}

	@Test
	public void testSaveBinlogInfo() throws Exception {
		BinlogInfo binlogInfo = new BinlogInfo();
		binlogInfo.setBinlogFile("a");
		binlogInfo.setBinlogPosition(987);
		mMapBasedBinlogInfoService.saveBinlogInfo(1230, binlogInfo);
		Assert.assertEquals(binlogInfo, mMapBasedBinlogInfoService.getBinlogInfo(1230));
	}

	@Test
	public void testRemoveBinlogInfo() throws Exception {
		mMapBasedBinlogInfoService.removeBinlogInfo(123);
		Assert.assertEquals(null, mMapBasedBinlogInfoService.getBinlogInfo(123));
	}

	@Test
	public void testFindSyncTaskIds() throws Exception {
		List<Long> syncTaskIds = new ArrayList<Long>();
		syncTaskIds.add((long) 1230);
		Assert.assertEquals(syncTaskIds, mMapBasedBinlogInfoService.findSyncTaskIds());
	}
}