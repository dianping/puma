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
<<<<<<< HEAD
		mMapBasedBinlogInfoService.setBaseDir("data/appdata/puma/syncserver");
=======
		mMapBasedBinlogInfoService.setBaseDir("/data/appdatas/puma/syncserver/");
>>>>>>> 692a38483d8b86cc391587d855ef041a5ec1aacc
		mMapBasedBinlogInfoService.init();
	}

	public void tearDown() throws Exception {

	}

	@Test
<<<<<<< HEAD
	public void testInit() throws Exception {
		BinlogInfo binlogInfo = new BinlogInfo();
		binlogInfo.setBinlogFile("a");
		binlogInfo.setBinlogPosition(987);

		Assert.assertEquals(binlogInfo, mMapBasedBinlogInfoService.getBinlogInfo(1230));
	}

	@Test
=======
>>>>>>> 692a38483d8b86cc391587d855ef041a5ec1aacc
	public void testGetBinlogInfo() throws Exception {
		BinlogInfo binlogInfo = new BinlogInfo();
		binlogInfo.setBinlogFile("a");
		binlogInfo.setBinlogPosition(999888777);
<<<<<<< HEAD
		mMapBasedBinlogInfoService.saveBinlogInfo(123, binlogInfo);
		Assert.assertEquals(binlogInfo, mMapBasedBinlogInfoService.getBinlogInfo(123));
=======
		mMapBasedBinlogInfoService.saveBinlogInfo("no-1", binlogInfo);
		Assert.assertEquals(binlogInfo, mMapBasedBinlogInfoService.getBinlogInfo("no-1"));
>>>>>>> 692a38483d8b86cc391587d855ef041a5ec1aacc
	}

	@Test
	public void testSaveBinlogInfo() throws Exception {
		BinlogInfo binlogInfo = new BinlogInfo();
<<<<<<< HEAD
		binlogInfo.setBinlogFile("a");
		binlogInfo.setBinlogPosition(987);
		mMapBasedBinlogInfoService.saveBinlogInfo(1230, binlogInfo);
		Assert.assertEquals(binlogInfo, mMapBasedBinlogInfoService.getBinlogInfo(1230));
=======
		binlogInfo.setBinlogFile("b");
		binlogInfo.setBinlogPosition(987);
		mMapBasedBinlogInfoService.saveBinlogInfo("no-2", binlogInfo);
		Assert.assertEquals(binlogInfo, mMapBasedBinlogInfoService.getBinlogInfo("no-2"));
>>>>>>> 692a38483d8b86cc391587d855ef041a5ec1aacc
	}

	@Test
	public void testRemoveBinlogInfo() throws Exception {
<<<<<<< HEAD
		mMapBasedBinlogInfoService.removeBinlogInfo(123);
		Assert.assertEquals(null, mMapBasedBinlogInfoService.getBinlogInfo(123));
=======
		mMapBasedBinlogInfoService.removeBinlogInfo("no-2");
		Assert.assertEquals(null, mMapBasedBinlogInfoService.getBinlogInfo("no-2"));
>>>>>>> 692a38483d8b86cc391587d855ef041a5ec1aacc
	}

	@Test
	public void testFindSyncTaskIds() throws Exception {
<<<<<<< HEAD
		List<Long> syncTaskIds = new ArrayList<Long>();
		syncTaskIds.add((long) 1230);
		Assert.assertEquals(syncTaskIds, mMapBasedBinlogInfoService.findSyncTaskIds());
=======
		List<String> syncTaskClientNames = new ArrayList<String>();
		syncTaskClientNames.add("no-1");
		Assert.assertEquals(syncTaskClientNames, mMapBasedBinlogInfoService.findSyncTaskClientNames());
>>>>>>> 692a38483d8b86cc391587d855ef041a5ec1aacc
	}
}