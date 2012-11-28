/**
 * Project: puma-server
 * 
 * File Created at 2012-8-6
 * $Id$
 * 
 * Copyright 2010 dianping.com.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Dianping Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with dianping.com.
 */
package com.dianping.puma.storage;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.dianping.puma.core.codec.JsonEventCodec;
import com.dianping.puma.core.util.ByteArrayUtils;

/**
 * TODO Comment of DefaultCleanupStrategyTest
 * 
 * @author Leo Liang
 * 
 */
public class DefaultCleanupStrategyTest {

	private File mbaseDir;
	private File sbaseDir;

	@Before
	public void before() throws IOException {
		mbaseDir = new File(System.getProperty("java.io.tmpdir", "."), "Master");
		sbaseDir = new File(System.getProperty("java.io.tmpdir", "."), "Puma");
	}

	@Test
	public void testCleanup() throws Exception {
		int preservedDay = 5;
		DefaultCleanupStrategy defaultCleanupStrategy = new DefaultCleanupStrategy();
		defaultCleanupStrategy.setPreservedDay(preservedDay);
		LocalFileBucketIndex mindex = new LocalFileBucketIndex();
		LocalFileBucketIndex sindex = new LocalFileBucketIndex();		
		mindex.setBaseDir(mbaseDir.getAbsolutePath());
		mindex.setBucketFilePrefix("bucket-");
		sindex.setBaseDir(sbaseDir.getAbsolutePath());
		sindex.setBucketFilePrefix("bucket-");
		BinlogIndexManager binlogIndexManager = null;
		DefaultBinlogIndexManager dbim = new DefaultBinlogIndexManager();
		MainBinlogIndexImpl mbii = new MainBinlogIndexImpl();
		mbii.setMainBinlogIndexBasedir(System.getProperty("java.io.tmpdir", ".") + "/Puma");
		mbii.setMainBinlogIndexFileName("binlogIndex");
		mbii.openMainBinlogIndex();
		SubBinlogIndexImpl sbii = new SubBinlogIndexImpl();
		sbii.setSubBinlogIndexBaseDir(System.getProperty("java.io.tmpdir", ".") + "/binlogindex");
		sbii.setSubBinlogIndexPrefix("index");
		dbim.setMainBinlogIndex(mbii);
		dbim.setSubBinlogIndex(sbii);
		dbim.setCodec(new JsonEventCodec());
		dbim.setMasterIndex(mindex);
		dbim.setSlaveIndex(sindex);
		binlogIndexManager = dbim;
		ZipCompressor zc = new ZipCompressor();
		JsonEventCodec jec = new JsonEventCodec();
		zc.setCodec(jec);
		sindex.setCompressor(zc);
		sindex.setCodec(jec);

		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");

		for (int i = 0; i <= 12; i++) {
			cal.add(Calendar.DAY_OF_MONTH, i == 0 ? 0 : -1);
			File file = new File(sbaseDir, sdf.format(cal.getTime()) + "/bucket-0");
			file.getParentFile().mkdirs();
			file.createNewFile();
		}
		mindex.start();
		sindex.start();

		defaultCleanupStrategy.cleanup(sindex, binlogIndexManager);

		Assert.assertEquals(preservedDay, sindex.size());

		sdf = new SimpleDateFormat("yyMMdd");

		cal = Calendar.getInstance();
		for (int i = 0; i < preservedDay; i++) {
			cal.add(Calendar.DAY_OF_MONTH, i == 0 ? 0 : -1);

			File file = new File(sbaseDir, "20" + sdf.format(cal.getTime()) + "/bucket-0");
			RandomAccessFile acfile = new RandomAccessFile(file, "rw");
			byte[] data = "ZIPFORMAT           ".getBytes();
			acfile.write(ByteArrayUtils.intToByteArray(data.length));
			acfile.write(data);
			acfile.close();
			Assert.assertTrue(file.exists());
			Assert.assertNotNull(sindex
					.getReadBucket(new Sequence(Integer.valueOf(sdf.format(cal.getTime())), 0).longValue(), true));
		}

		cal = Calendar.getInstance();
		for (int i = preservedDay; i <= 12; i++) {
			cal.add(Calendar.DAY_OF_MONTH, -1 * preservedDay);

			File file = new File(sbaseDir, "20" + sdf.format(cal.getTime()) + "/bucket-0");
			Assert.assertFalse(file.exists());
			Assert.assertNull(sindex.getReadBucket(new Sequence(Integer.valueOf(sdf.format(cal.getTime())), 0).longValue(), true));
		}

	}

	@After
	public void after() throws Exception {
		if (sbaseDir != null && sbaseDir.exists() && sbaseDir.isDirectory()) {
			FileUtils.deleteDirectory(sbaseDir);
		}
	}

}
