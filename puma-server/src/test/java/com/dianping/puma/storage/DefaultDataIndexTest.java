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
package com.dianping.puma.storage;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Leo Liang
 * 
 */
public class DefaultDataIndexTest {
    protected File baseDir = null;

    @BeforeClass
    public static void init() {

    }

    @Before
    public void before() {
        baseDir = new File(System.getProperty("java.io.tmpdir", "."), "IndexTest");
        baseDir.mkdirs();

    }

    @After
    public void after() {
        FileUtils.deleteQuietly(baseDir);
    }

    @AfterClass
    public static void destroy() {
    }

    @Test
    public void testFind() throws IOException {
        createL1IndexFile(new String[] { "0!binlog01!0=1000", "0!binlog02!0=2000" });
        Map<String, String[]> l2IndexFiles = new HashMap<String, String[]>();
        l2IndexFiles.put("1000", new String[] { "0!binlog01!0=10000", "0!binlog01!1=10001", "0!binlog01!2=10002",
                "0!binlog01!3=10003", "0!binlog01!4=10004", "0!binlog01!5=10005" });
        l2IndexFiles.put("2000", new String[] { "0!binlog02!6=10006", "0!binlog02!7=10007", "0!binlog02!8=10008",
                "0!binlog02!9=10009", "0!binlog02!10=10010", "0!binlog02!11=10011" });
        createL2IndexFile(l2IndexFiles);
        DefaultDataIndexImpl<BinlogIndexKey, Long> index = new DefaultDataIndexImpl<BinlogIndexKey, Long>(
                baseDir.getAbsolutePath(), new LongIndexItemConvertor(), new BinlogIndexKeyConvertor());
        index.start();
        Assert.assertEquals(10005L, index.find(new BinlogIndexKey("binlog01", 5, 0)).longValue());
        Assert.assertEquals(10011L, index.find(new BinlogIndexKey("binlog02", 11, 0)).longValue());
        Assert.assertEquals(10000L, index.find(new BinlogIndexKey("binlog01", 0, 0)).longValue());
        Assert.assertEquals(10006L, index.find(new BinlogIndexKey("binlog02", 6, 0)).longValue());
        Assert.assertNull(index.find(new BinlogIndexKey("binlog03", 6, 0)));
        Assert.assertNull(index.find(new BinlogIndexKey("binlog00", 10, 0)));
        Assert.assertNull(index.find(new BinlogIndexKey("binlog00", 0, 4)));
    }

    @Test
    public void testAddL1Index() throws IOException {

        DefaultDataIndexImpl<BinlogIndexKey, Long> index = new DefaultDataIndexImpl<BinlogIndexKey, Long>(
                baseDir.getAbsolutePath(), new LongIndexItemConvertor(), new BinlogIndexKeyConvertor());
        index.start();
        BinlogIndexKey key = new BinlogIndexKey("bin-0001.bin", 5, 0);
        index.addL1Index(key, "1");

        File l1IndexFile = new File(baseDir, DefaultDataIndexImpl.L1INDEX_FILENAME);
        Assert.assertTrue(l1IndexFile.exists());
        Properties prop = new Properties();
        prop.load(new FileInputStream(l1IndexFile));
        Assert.assertEquals(1, prop.size());
        Assert.assertEquals("1", prop.getProperty(new BinlogIndexKeyConvertor().convertToObj(key)));

        File l2IndexFile = new File(new File(baseDir, DefaultDataIndexImpl.L2INDEX_FOLDER), "1"
                + DefaultDataIndexImpl.L2INDEX_FILESUFFIX);
        Assert.assertTrue(l2IndexFile.exists());
    }

    @Test
    public void testAddL2Index() throws IOException {

        DefaultDataIndexImpl<BinlogIndexKey, Long> index = new DefaultDataIndexImpl<BinlogIndexKey, Long>(
                baseDir.getAbsolutePath(), new LongIndexItemConvertor(), new BinlogIndexKeyConvertor());
        index.start();
        BinlogIndexKey key = new BinlogIndexKey("bin-0001.bin", 5, 0);
        index.addL1Index(key, "1");
        index.addL2Index(key, 123L);
        
        BinlogIndexKey key1 = new BinlogIndexKey("bin-0002.bin", 5, 0);
        index.addL1Index(key1, "1");
        index.addL2Index(key1, 123555L);

        File l1IndexFile = new File(baseDir, DefaultDataIndexImpl.L1INDEX_FILENAME);
        Assert.assertTrue(l1IndexFile.exists());
        Properties prop = new Properties();
        prop.load(new FileInputStream(l1IndexFile));
        Assert.assertEquals(1, prop.size());
        Assert.assertEquals("1", prop.getProperty(new BinlogIndexKeyConvertor().convertToObj(key)));

        File l2IndexFile = new File(new File(baseDir, DefaultDataIndexImpl.L2INDEX_FOLDER), "1"
                + DefaultDataIndexImpl.L2INDEX_FILESUFFIX);
        Assert.assertTrue(l2IndexFile.exists());
        prop = new Properties();
        prop.load(new FileInputStream(l2IndexFile));
        Assert.assertEquals(1, prop.size());
        Assert.assertEquals("123", prop.getProperty(new BinlogIndexKeyConvertor().convertToObj(key)));
    }

    @Test
    public void testWholeProcess() throws IOException {

        DefaultDataIndexImpl<BinlogIndexKey, Long> index = new DefaultDataIndexImpl<BinlogIndexKey, Long>(
                baseDir.getAbsolutePath(), new LongIndexItemConvertor(), new BinlogIndexKeyConvertor());
        index.start();
        BinlogIndexKey key = new BinlogIndexKey("bin-0001.bin", 5, 0);
        index.addL1Index(key, "1");
        index.addL2Index(key, 123L);

        File l1IndexFile = new File(baseDir, DefaultDataIndexImpl.L1INDEX_FILENAME);
        Assert.assertTrue(l1IndexFile.exists());
        Properties prop = new Properties();
        prop.load(new FileInputStream(l1IndexFile));
        Assert.assertEquals(1, prop.size());
        Assert.assertEquals("1", prop.getProperty(new BinlogIndexKeyConvertor().convertToObj(key)));

        File l2IndexFile = new File(new File(baseDir, DefaultDataIndexImpl.L2INDEX_FOLDER), "1"
                + DefaultDataIndexImpl.L2INDEX_FILESUFFIX);
        Assert.assertTrue(l2IndexFile.exists());
        prop = new Properties();
        prop.load(new FileInputStream(l2IndexFile));
        Assert.assertEquals(1, prop.size());
        Assert.assertEquals("123", prop.getProperty(new BinlogIndexKeyConvertor().convertToObj(key)));

        Assert.assertEquals(123L, index.find(key).longValue());
    }

    @Test
    public void testRemove() throws IOException {

        DefaultDataIndexImpl<BinlogIndexKey, Long> index = new DefaultDataIndexImpl<BinlogIndexKey, Long>(
                baseDir.getAbsolutePath(), new LongIndexItemConvertor(), new BinlogIndexKeyConvertor());
        index.start();
        BinlogIndexKey key = new BinlogIndexKey("bin-0001.bin", 5, 0);
        index.addL1Index(key, "1");
        index.addL2Index(key, 123L);

        index.removeByL2IndexName("1");

        File l1IndexFile = new File(baseDir, DefaultDataIndexImpl.L1INDEX_FILENAME);
        Assert.assertTrue(l1IndexFile.exists());
        Properties prop = new Properties();
        prop.load(new FileInputStream(l1IndexFile));
        Assert.assertEquals(0, prop.size());

        File l2IndexFile = new File(new File(baseDir, DefaultDataIndexImpl.L2INDEX_FOLDER), "1"
                + DefaultDataIndexImpl.L2INDEX_FILESUFFIX);
        Assert.assertFalse(l2IndexFile.exists());

    }

    private void createL1IndexFile(String[] lines) throws IOException {
        File file = new File(baseDir, DefaultDataIndexImpl.L1INDEX_FILENAME);
        file.createNewFile();
        BufferedWriter bw = new BufferedWriter(new FileWriter(file));
        for (String line : lines) {
            bw.write(line);
            bw.newLine();
        }
        bw.close();
    }

    private void createL2IndexFile(Map<String, String[]> data) throws IOException {
        for (Map.Entry<String, String[]> entry : data.entrySet()) {
            File file = new File(new File(baseDir, DefaultDataIndexImpl.L2INDEX_FOLDER), entry.getKey()
                    + DefaultDataIndexImpl.L2INDEX_FILESUFFIX);
            file.getParentFile().mkdirs();
            file.createNewFile();
            BufferedWriter bw = new BufferedWriter(new FileWriter(file));
            for (String line : entry.getValue()) {
                bw.write(line);
                bw.newLine();
            }
            bw.close();
        }
    }
}
