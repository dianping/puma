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
import java.util.TreeMap;

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

	private File baseDir;

	@Before
	public void before() throws IOException {
		baseDir = new File(System.getProperty("java.io.tmpdir", "."), "Puma");
	}

	@Test
	public void testCleanup() throws Exception {
		int preservedDay = 5;
		DefaultCleanupStrategy defaultCleanupStrategy = new DefaultCleanupStrategy();
		defaultCleanupStrategy.setPreservedDay(preservedDay);
		LocalFileBucketIndex index = new LocalFileBucketIndex();
		index.setBaseDir(baseDir.getAbsolutePath());
		index.setBucketFilePrefix("bucket-");
		BinlogIndexManager binlogIndexManager = new DefaultBinlogIndexManager();
		binlogIndexManager.setMainbinlogIndexFileName("binlogIndex");
		binlogIndexManager.setMainbinlogIndexFileNameBasedir("java.io.tmpdir" + "Puma");
		binlogIndexManager.setSubBinlogIndexBaseDir("java.io.tmpdir" + "binlogindex");
		binlogIndexManager.setCodec(new JsonEventCodec());
		binlogIndexManager.setBinlogIndex(new TreeMap<BinlogInfoAndSeq, BinlogInfoAndSeq>());
		binlogIndexManager.setMainBinlogIndexFile(new File("java.io.tmpdir" + "Puma", "binlogIndex"));

		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");

		for (int i = 0; i <= 12; i++) {
			cal.add(Calendar.DAY_OF_MONTH, i == 0 ? 0 : -1);
			File file = new File(baseDir, sdf.format(cal.getTime()) + "/bucket-0");
			file.getParentFile().mkdirs();
			file.createNewFile();
		}

		index.start();

		defaultCleanupStrategy.cleanup(index, binlogIndexManager);

		Assert.assertEquals(preservedDay, index.size());

		sdf = new SimpleDateFormat("yyMMdd");

		cal = Calendar.getInstance();
		for (int i = 0; i < preservedDay; i++) {
			cal.add(Calendar.DAY_OF_MONTH, i == 0 ? 0 : -1);

			File file = new File(baseDir, "20" + sdf.format(cal.getTime()) + "/bucket-0");
			RandomAccessFile acfile = new RandomAccessFile(file, "rw");
			byte[] data = "ZIPFORMAT           ".getBytes();
			acfile.write(ByteArrayUtils.intToByteArray(data.length));
			acfile.write(data);
			acfile.close();
			Assert.assertTrue(file.exists());
			Assert.assertNotNull(index
					.getReadBucket(new Sequence(Integer.valueOf(sdf.format(cal.getTime())), 0).longValue(), true));
		}

		cal = Calendar.getInstance();
		for (int i = preservedDay; i <= 12; i++) {
			cal.add(Calendar.DAY_OF_MONTH, -1 * preservedDay);

			File file = new File(baseDir, "20" + sdf.format(cal.getTime()) + "/bucket-0");
			Assert.assertFalse(file.exists());
			Assert.assertNull(index.getReadBucket(new Sequence(Integer.valueOf(sdf.format(cal.getTime())), 0).longValue(), true));
		}

	}

	@After
	public void after() throws Exception {
		if (baseDir != null && baseDir.exists() && baseDir.isDirectory()) {
			FileUtils.deleteDirectory(baseDir);
		}
	}

}
