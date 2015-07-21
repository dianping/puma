/**
 * Project: puma-server
 * 
 * File Created at 2013-1-9
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
package com.dianping.puma.storage.index;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.dianping.puma.core.constant.SubscribeConstant;
import com.dianping.puma.storage.Sequence;

/**
 * 
 * @author damonzhu
 *
 */
public class DefaultDataIndexTest {
	protected File baseDir = null;

	private DefaultDataIndexImpl<BinlogIndexKey, L2Index> index;

	private BinlogIndexKey key1;

	private BinlogIndexKey key2;

	private BinlogIndexKey key3;

	@Before
	public void before() throws IOException {
		baseDir = new File(System.getProperty("java.io.tmpdir", "."), "IndexTest");
		baseDir.mkdirs();

		index = new DefaultDataIndexImpl<BinlogIndexKey, L2Index>(baseDir.getAbsolutePath(), new L2IndexItemConvertor(),
		      new BinlogIndexKeyConvertor());
		index.start();

		key1 = new BinlogIndexKey("bin-0001.bin", 5, 0);
		L2Index l2Index1 = new L2Index();
		l2Index1.setBinlogIndexKey(key1);
		l2Index1.setDatabase("dianping");
		l2Index1.setTable("receipt");
		l2Index1.setDdl(false);
		l2Index1.setDml(true);
		l2Index1.setTransaction(false);
		l2Index1.setSequence(new Sequence(123L,0));

		index.addL1Index(key1, "1");
		index.addL2Index(key1, l2Index1);

		key2 = new BinlogIndexKey("bin-0002.bin", 10, 0);
		L2Index l2Index2 = new L2Index();
		l2Index2.setBinlogIndexKey(key2);
		l2Index2.setDatabase("dianping");
		l2Index2.setTable("receipt");
		l2Index2.setDdl(false);
		l2Index2.setDml(true);
		l2Index2.setTransaction(false);
		l2Index2.setSequence(new Sequence(123555L,0));

		index.addL2Index(key2, l2Index2);

		key3 = new BinlogIndexKey("bin-0003.bin", 200, 0);
		L2Index l2Index3 = new L2Index();
		l2Index3.setBinlogIndexKey(key3);
		l2Index3.setDatabase("dianping");
		l2Index3.setTable("receipt");
		l2Index3.setDdl(false);
		l2Index3.setDml(true);
		l2Index3.setTransaction(false);
		l2Index3.setSequence(new Sequence(123555L,0));

		index.addL1Index(key3, "2");
		index.addL2Index(key3, l2Index3);
	}

	@After
	public void after() {
		FileUtils.deleteQuietly(baseDir);
	}

	@Test
	public void testAddL1Index() throws IOException {
		File l1IndexFile = new File(baseDir, DefaultDataIndexImpl.L1INDEX_FILENAME);
		Assert.assertTrue(l1IndexFile.exists());
		Properties prop = new Properties();
		prop.load(new FileInputStream(l1IndexFile));
		Assert.assertEquals(2, prop.size());
		Assert.assertEquals("1", prop.getProperty(new BinlogIndexKeyConvertor().convertToObj(key1)));
		Assert.assertEquals("2", prop.getProperty(new BinlogIndexKeyConvertor().convertToObj(key3)));

		File l2IndexFile1 = new File(new File(baseDir, DefaultDataIndexImpl.L2INDEX_FOLDER), "1"
		      + DefaultDataIndexImpl.L2INDEX_FILESUFFIX);
		Assert.assertTrue(l2IndexFile1.exists());
		File l2IndexFile2 = new File(new File(baseDir, DefaultDataIndexImpl.L2INDEX_FOLDER), "2"
		      + DefaultDataIndexImpl.L2INDEX_FILESUFFIX);
		Assert.assertTrue(l2IndexFile2.exists());
	}

	@Test
	public void testGetIndexBucketFromBinLogInfo() throws IOException {
		IndexBucket<BinlogIndexKey, L2Index> indexBucket = index.getIndexBucket(SubscribeConstant.SEQ_FROM_BINLOGINFO,
		      new BinlogIndexKey("bin-0001.bin", 5, 0));
		L2Index convertFromObj = indexBucket.next();

		Assert.assertEquals("bin-0002.bin", convertFromObj.getBinlogIndexKey().getBinlogFile());
		Assert.assertEquals(10L, convertFromObj.getBinlogIndexKey().getBinlogPos());
		Assert.assertEquals(0L, convertFromObj.getBinlogIndexKey().getServerId());
		Assert.assertEquals("dianping", convertFromObj.getDatabase());
		Assert.assertEquals("receipt", convertFromObj.getTable());
		Assert.assertEquals(false, convertFromObj.isDdl());
		Assert.assertEquals(true, convertFromObj.isDml());
		Assert.assertEquals(123555L, convertFromObj.getSequence().longValue());
	}

	@Test
	public void testGetIndexBucketFromOldest() throws IOException {
		IndexBucket<BinlogIndexKey, L2Index> indexBucket = index.getIndexBucket(SubscribeConstant.SEQ_FROM_OLDEST, null);
		L2Index convertFromObj = indexBucket.next();

		Assert.assertEquals("bin-0001.bin", convertFromObj.getBinlogIndexKey().getBinlogFile());
		Assert.assertEquals(5L, convertFromObj.getBinlogIndexKey().getBinlogPos());
		Assert.assertEquals(0L, convertFromObj.getBinlogIndexKey().getServerId());
		Assert.assertEquals("dianping", convertFromObj.getDatabase());
		Assert.assertEquals("receipt", convertFromObj.getTable());
		Assert.assertEquals(false, convertFromObj.isDdl());
		Assert.assertEquals(true, convertFromObj.isDml());
		Assert.assertEquals(123L, convertFromObj.getSequence().longValue());
	}

	@Test(expected = EOFException.class)
	public void testGetIndexBucketFromLaest1() throws IOException {
		IndexBucket<BinlogIndexKey, L2Index> indexBucket = index.getIndexBucket(SubscribeConstant.SEQ_FROM_LATEST, null);
		indexBucket.next();
	}

	@Test
	public void testGetIndexBucketFromLaest2() throws IOException {
		IndexBucket<BinlogIndexKey, L2Index> indexBucket = index.getIndexBucket(SubscribeConstant.SEQ_FROM_LATEST, null);

		BinlogIndexKey key1 = new BinlogIndexKey("bin-0002.bin", 15, 0);
		L2Index l2Index2 = new L2Index();
		l2Index2.setBinlogIndexKey(key1);
		l2Index2.setDatabase("dianping");
		l2Index2.setTable("receipt");
		l2Index2.setDdl(false);
		l2Index2.setDml(true);
		l2Index2.setSequence(new Sequence(123567L,0));
		index.addL2Index(key1, l2Index2);

		L2Index convertFromObj = indexBucket.next();

		Assert.assertEquals("bin-0002.bin", convertFromObj.getBinlogIndexKey().getBinlogFile());
		Assert.assertEquals(15L, convertFromObj.getBinlogIndexKey().getBinlogPos());
		Assert.assertEquals(0L, convertFromObj.getBinlogIndexKey().getServerId());
		Assert.assertEquals("dianping", convertFromObj.getDatabase());
		Assert.assertEquals("receipt", convertFromObj.getTable());
		Assert.assertEquals(false, convertFromObj.isDdl());
		Assert.assertEquals(true, convertFromObj.isDml());
		Assert.assertEquals(123567L, convertFromObj.getSequence().longValue());
	}

	@Test
	public void testGetNextIndexBucket() throws IOException {
		IndexBucket<BinlogIndexKey, L2Index> indexBucket = index.getNextIndexBucket(key3);

		L2Index convertFromObj = indexBucket.next();

		Assert.assertEquals("bin-0003.bin", convertFromObj.getBinlogIndexKey().getBinlogFile());
		Assert.assertEquals(200L, convertFromObj.getBinlogIndexKey().getBinlogPos());
		Assert.assertEquals(0L, convertFromObj.getBinlogIndexKey().getServerId());
		Assert.assertEquals("dianping", convertFromObj.getDatabase());
		Assert.assertEquals("receipt", convertFromObj.getTable());
		Assert.assertEquals(false, convertFromObj.isDdl());
		Assert.assertEquals(true, convertFromObj.isDml());
		Assert.assertEquals(123555L, convertFromObj.getSequence().longValue());
	}

	@Test
	public void testRemoveByL2IndexName() throws IOException {
		index.removeByL2IndexName("1");

		File l1IndexFile = new File(baseDir, DefaultDataIndexImpl.L1INDEX_FILENAME);
		Assert.assertTrue(l1IndexFile.exists());
		Properties prop = new Properties();
		prop.load(new FileInputStream(l1IndexFile));
		Assert.assertEquals(1, prop.size());

		File l2IndexFile = new File(new File(baseDir, DefaultDataIndexImpl.L2INDEX_FOLDER), "1"
		      + DefaultDataIndexImpl.L2INDEX_FILESUFFIX);
		Assert.assertFalse(l2IndexFile.exists());
	}
}
