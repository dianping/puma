/**
 * Project: puma-server
 * <p/>
 * File Created at 2013-1-9
 * $Id$
 * <p/>
 * Copyright 2010 dianping.com.
 * All rights reserved.
 * <p/>
 * This software is the confidential and proprietary information of
 * Dianping Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with dianping.com.
 */
package com.dianping.puma.storage.index;

import com.dianping.puma.storage.Sequence;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * @author damonzhu,Dozer
 */
public class DefaultIndexManagerTest {
    protected File baseDir = null;

    private DefaultIndexManager<IndexKeyImpl, IndexValueImpl> index;

    private void addIndex(IndexKeyImpl indexKey, String bucketName, String database, String table, boolean isDdl,
                          boolean isDml, boolean isTransactionBegin, boolean isTransactionCommit, Sequence seq,
                          boolean shouldAddL1Index, boolean shouldAddL2Index) throws IOException {
        if (shouldAddL1Index) {
            index.addL1Index(indexKey, bucketName);
        }

        if (shouldAddL2Index) {
            IndexValueImpl l2IndexValue = new IndexValueImpl();

            l2IndexValue.setIndexKey(indexKey);
            l2IndexValue.setTable(table);
            l2IndexValue.setDdl(isDdl);
            l2IndexValue.setDml(isDml);
            l2IndexValue.setTransactionBegin(isTransactionBegin);
            l2IndexValue.setTransactionCommit(isTransactionCommit);
            l2IndexValue.setSequence(seq);

            index.addL2Index(indexKey, l2IndexValue);
        }
    }

    @After
    public void after() {
        FileUtils.deleteQuietly(baseDir);
    }

    @Before
    public void before() throws IOException {
        baseDir = new File(System.getProperty("java.io.tmpdir", "."), "IndexTest");
        baseDir.mkdirs();

        index = new DefaultIndexManager<IndexKeyImpl, IndexValueImpl>(baseDir.getAbsolutePath(), new IndexKeyConvertor(),
                new IndexValueConvertor());
        index.start();

        addIndex(new IndexKeyImpl(1, 0, "bin-0001.bin", 5), "1", "dianping", "receipt", false, true, false, false,
                new Sequence(123L, 0), true, true);
        addIndex(new IndexKeyImpl(5, 0, "bin-0002.bin", 10), null, "dianping", "receipt", false, true, true, false,
                new Sequence(123555L, 0), false, true);
        addIndex(new IndexKeyImpl(10, 0, "bin-0002.bin", 30), null, "dianping", "receipt", false, true, false, false,
                new Sequence(123556L, 0), false, true);
        addIndex(new IndexKeyImpl(16, 0, "bin-0002.bin", 50), null, "dianping", "receipt", false, true, false, true,
                new Sequence(123557L, 0), false, true);
        addIndex(new IndexKeyImpl(20, 0, "bin-0002.bin", 70), null, "dianping", "receipt", false, true, false, false,
                new Sequence(123557L, 0), false, true);
        addIndex(new IndexKeyImpl(22, 0, "bin-0002.bin", 90), null, "dianping", "receipt", false, true, false, false,
                new Sequence(123557L, 0), false, true);
        addIndex(new IndexKeyImpl(34, 0, "bin-0003.bin", 200), "2", "dianping", "receipt", false, true, false, false,
                new Sequence(123559L, 0), true, true);
        addIndex(new IndexKeyImpl(100, -12, "bin-0001.bin", 200), "3", "dianping", "receipt", false, true, false, false,
                new Sequence(123560L, 0), true, true);
        addIndex(new IndexKeyImpl(101, -12, "bin-0001.bin", 300), "3", "dianping", "receipt", false, true, false, true,
                new Sequence(123560L, 0), false, true);
        addIndex(new IndexKeyImpl(102, -12, "bin-0001.bin", 400), "3", "dianping", "receipt", false, true, true, false,
                new Sequence(123560L, 0), false, true);
        addIndex(new IndexKeyImpl(103, -12, "bin-0001.bin", 500), "3", "dianping", "receipt", false, true, false, false,
                new Sequence(123560L, 0), false, true);
        addIndex(new IndexKeyImpl(104, -12, "bin-0002.bin", 0), "4", "dianping", "receipt", false, true, false, false,
                new Sequence(123560L, 0), true, true);
        addIndex(new IndexKeyImpl(105, -12, "bin-0002.bin", 100), "4", "dianping", "receipt", false, true, false, false,
                new Sequence(123560L, 0), false, true);
        addIndex(new IndexKeyImpl(106, -12, "bin-0002.bin", 200), "4", "dianping", "receipt", false, true, false, false,
                new Sequence(123560L, 0), false, true);

        index.flush();
    }

    @Test
    public void testAddL1Index() throws IOException {
        File l1IndexFile = new File(baseDir, DefaultIndexManager.L1INDEX_FILENAME);

        Assert.assertTrue(l1IndexFile.exists());

        Properties prop = new Properties();
        prop.load(new FileInputStream(l1IndexFile));

        Assert.assertEquals(4, prop.size());
        Assert.assertEquals("1", prop.getProperty("1!0!bin-0001.bin!5"));
        Assert.assertEquals("2", prop.getProperty("34!0!bin-0003.bin!200"));
        Assert.assertEquals("3", prop.getProperty("100!-12!bin-0001.bin!200"));
    }

    @Test
    public void testAddL2Index() throws IOException {
        File l2IndexFile1 = new File(new File(baseDir, DefaultIndexManager.L2INDEX_FOLDER), "1"
                + DefaultIndexManager.L2INDEX_FILESUFFIX);
        Assert.assertTrue(l2IndexFile1.exists());

        File l2IndexFile2 = new File(new File(baseDir, DefaultIndexManager.L2INDEX_FOLDER), "2"
                + DefaultIndexManager.L2INDEX_FILESUFFIX);
        Assert.assertTrue(l2IndexFile2.exists());

        File l2IndexFile3 = new File(new File(baseDir, DefaultIndexManager.L2INDEX_FOLDER), "3"
                + DefaultIndexManager.L2INDEX_FILESUFFIX);
        Assert.assertTrue(l2IndexFile3.exists());

        LocalFileIndexBucket<IndexKeyImpl, IndexValueImpl> bucket = new LocalFileIndexBucket<IndexKeyImpl, IndexValueImpl>(
                "1" + DefaultIndexManager.L2INDEX_FILESUFFIX, l2IndexFile1, new IndexValueConvertor());

        Assert.assertEquals(new IndexKeyImpl(1, 0, "bin-0001.bin", 5), bucket.next().getIndexKey());
        Assert.assertEquals(new IndexKeyImpl(5, 0, "bin-0002.bin", 10), bucket.next().getIndexKey());
    }

    /*
     * 根据binlog查找：查找条件的binlog位置直接可以在l1Index中查到，并且这个位置不属于transactionCommit
     */
    @Test
    public void testfindByBinlog1() throws IOException {
        IndexKeyImpl indexKey = index.findByBinlog(new IndexKeyImpl(1, 0, "bin-0001.bin", 5), false).getIndexKey();

        Assert.assertEquals(new IndexKeyImpl(1, 0, "bin-0001.bin", 5), indexKey);

        Assert.assertEquals(null, index.findByBinlog(new IndexKeyImpl(1, 0, "bin-0001.bin", 5), true));
    }

    /*
     * 根据binlog查找：查找条件的binlog位置不可以在l1Index中直接查到，并且这个位置不属于transactionCommit
     */
    @Test
    public void testfindByBinlog2() throws IOException {
        IndexKeyImpl indexKey = index.findByBinlog(new IndexKeyImpl(20, 0, "bin-0002.bin", 70), false).getIndexKey();

        Assert.assertEquals(new IndexKeyImpl(20, 0, "bin-0002.bin", 70), indexKey);

        indexKey = index.findByBinlog(new IndexKeyImpl(20, 0, "bin-0002.bin", 70), true).getIndexKey();
        Assert.assertEquals(new IndexKeyImpl(16, 0, "bin-0002.bin", 50), indexKey);
    }

    /*
     * 根据binlog查找：查找条件的binlog位置不可以在l1Index中直接查到，并且这个位置属于transactionCommit
     */
    @Test
    public void testfindByBinlog3() throws IOException {
        IndexKeyImpl indexKey = index.findByBinlog(new IndexKeyImpl(16, 0, "bin-0002.bin", 50), false).getIndexKey();

        Assert.assertEquals(new IndexKeyImpl(16, 0, "bin-0002.bin", 50), indexKey);

        indexKey = index.findByBinlog(new IndexKeyImpl(16, 0, "bin-0002.bin", 50), true).getIndexKey();
        Assert.assertEquals(new IndexKeyImpl(16, 0, "bin-0002.bin", 50), indexKey);
    }

    /*
     * 根据binlog查找：查找条件的binlog位置不可以在l1Index中直接查到，同样也不存在l2Index
     */
    @Test
    public void testfindByBinlog4() throws IOException {
        IndexKeyImpl indexKey = index.findByBinlog(new IndexKeyImpl(16, 0, "bin-0002.bin", 80), false).getIndexKey();

        Assert.assertEquals(new IndexKeyImpl(20, 0, "bin-0002.bin", 70), indexKey);

        indexKey = index.findByBinlog(new IndexKeyImpl(16, 0, "bin-0002.bin", 80), true).getIndexKey();
        Assert.assertEquals(new IndexKeyImpl(16, 0, "bin-0002.bin", 50), indexKey);
    }

    /*
     * 根据binlog查找：查找条件的binlog位置不可以在l1Index中直接查到，同样也不存在l2Index,另外需要递归的查找到transactionCommit事件
     */
    @Test
    public void testfindByBinlog5() throws IOException {
        IndexKeyImpl indexKey = index.findByBinlog(new IndexKeyImpl(105, -12, "bin-0002.bin", 100), true).getIndexKey();

        Assert.assertEquals(new IndexKeyImpl(101, -12, "bin-0001.bin", 300), indexKey);
    }

    /*
     * 根据time查找：查找条件的time直接可以在l1Index中查到，并且这个位置不属于transactionCommit
     */
    @Test
    public void testfindByTime1() throws IOException {
        IndexKeyImpl searchKey = new IndexKeyImpl(2);

        IndexKeyImpl key = index.findByTime(searchKey, false).getIndexKey();

        Assert.assertEquals(new IndexKeyImpl(1, 0, "bin-0001.bin", 5), key);

        Assert.assertEquals(null, index.findByTime(searchKey, true));
    }

    /*
     * 根据time查找：查找条件的time不可以在l1Index中直接查到，并且这个位置不属于transactionCommit
     */
    @Test
    public void testfindByTime2() throws IOException {
        IndexKeyImpl searchKey = new IndexKeyImpl(21);

        IndexKeyImpl key = index.findByTime(searchKey, false).getIndexKey();
        Assert.assertEquals(new IndexKeyImpl(20, 0, "bin-0002.bin", 70), key);

        key = index.findByTime(searchKey, true).getIndexKey();
        Assert.assertEquals(new IndexKeyImpl(16, 0, "bin-0002.bin", 50), key);
    }

    /*
     * 根据time查找：查找条件的time不可以在l1Index中直接查到，并且这个位置属于transactionCommit
     */
    @Test
    public void testfindByTime3() throws IOException {
        IndexKeyImpl searchKey = new IndexKeyImpl(17);

        IndexKeyImpl key = index.findByTime(searchKey, false).getIndexKey();
        Assert.assertEquals(new IndexKeyImpl(16, 0, "bin-0002.bin", 50), key);

        key = index.findByTime(searchKey, true).getIndexKey();
        Assert.assertEquals(new IndexKeyImpl(16, 0, "bin-0002.bin", 50), key);
    }

    /*
     * 根据time查找：查找条件的time不可以在l1Index中直接查到，同样也不存在l2Index
     */
    @Test
    public void testfindByTime4() throws IOException {
        IndexKeyImpl searchKey = new IndexKeyImpl(21);

        IndexKeyImpl key = index.findByTime(searchKey, false).getIndexKey();
        Assert.assertEquals(new IndexKeyImpl(20, 0, "bin-0002.bin", 70), key);

        key = index.findByTime(searchKey, true).getIndexKey();
        Assert.assertEquals(new IndexKeyImpl(16, 0, "bin-0002.bin", 50), key);
    }

    /*
     * 根据time查找：查找条件的binlog位置不可以在l1Index中直接查到，同样也不存在l2Index,另外需要递归的查找到transactionCommit事件
     */
    @Test
    public void testfindByTime5() throws IOException {
        IndexKeyImpl searchKey = new IndexKeyImpl(106);

        IndexKeyImpl key = index.findByTime(searchKey, false).getIndexKey();
        Assert.assertEquals(new IndexKeyImpl(105, -12, "bin-0002.bin", 100), key);

        key = index.findByTime(searchKey, true).getIndexKey();
        Assert.assertEquals(new IndexKeyImpl(101, -12, "bin-0001.bin", 300), key);
    }

    @Test
    public void testfindFirst() throws IOException {
        IndexKeyImpl firstKey = index.findFirst().getIndexKey();

        Assert.assertEquals(new IndexKeyImpl(1, 0, "bin-0001.bin", 5), firstKey);
    }

    @Test
    public void testfindLatest() throws IOException {
        IndexKeyImpl latestKey = index.findLatest().getIndexKey();
        Assert.assertEquals(new IndexKeyImpl(106, -12, "bin-0002.bin", 200), latestKey);
    }

    @Test
    public void testHasNextIndexBucket() throws IOException {
        Assert.assertFalse(index.hasNextIndexBucket("xxx"));
        Assert.assertTrue(index.hasNextIndexBucket("1"));
        Assert.assertTrue(index.hasNextIndexBucket("2"));
        Assert.assertTrue(index.hasNextIndexBucket("3"));
        Assert.assertFalse(index.hasNextIndexBucket("4"));
    }

    @Test
    public void testGetNextIndexBucket() throws IOException {
        IndexBucket<IndexKeyImpl, IndexValueImpl> indexBucket = index.getNextIndexBucket("1");

        IndexValueImpl convertFromObj = indexBucket.next();

        Assert.assertEquals("bin-0003.bin", convertFromObj.getIndexKey().getBinlogFile());
        Assert.assertEquals(200L, convertFromObj.getIndexKey().getBinlogPosition());
        Assert.assertEquals(0L, convertFromObj.getIndexKey().getServerId());
        Assert.assertEquals("receipt", convertFromObj.getTable());
        Assert.assertEquals(false, convertFromObj.isDdl());
        Assert.assertEquals(true, convertFromObj.isDml());
        Assert.assertEquals(123559L, convertFromObj.getSequence().longValue());

        indexBucket.stop();
    }

    @Test
    public void testRemoveByL2IndexName() throws IOException {
        index.removeByL2IndexName("1");

        File l1IndexFile = new File(baseDir, DefaultIndexManager.L1INDEX_FILENAME);
        Assert.assertTrue(l1IndexFile.exists());
        Properties prop = new Properties();
        prop.load(new FileInputStream(l1IndexFile));
        Assert.assertEquals(3, prop.size());

        File l2IndexFile = new File(new File(baseDir, DefaultIndexManager.L2INDEX_FOLDER), "1"
                + DefaultIndexManager.L2INDEX_FILESUFFIX);
        Assert.assertFalse(l2IndexFile.exists());
    }

    @Test
    public void testCanNotAddSameL1Index() throws IOException {
        int size = index.loadLinkedL1Index().size();
        addIndex(new IndexKeyImpl(200, -12, "bin-0003.bin", 0), "4", "dianping", "receipt", false, true, false, false,
                new Sequence(123561L, 0), true, true);
        Assert.assertEquals(size + 1, index.loadLinkedL1Index().size());
        addIndex(new IndexKeyImpl(200, -12, "bin-0003.bin", 0), "4", "dianping", "receipt", false, true, false, false,
                new Sequence(123561L, 0), true, true);
        Assert.assertEquals(size + 1, index.loadLinkedL1Index().size());
    }
}