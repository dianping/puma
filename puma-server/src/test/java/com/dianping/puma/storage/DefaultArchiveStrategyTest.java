/**
 * Project: puma-server
 *
 * File Created at 2012-7-26
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

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.dianping.puma.storage.bucket.LocalFileDataBucketManager;

import java.io.File;

/**
 * @author Leo Liang
 */
public class DefaultArchiveStrategyTest {
    private File masterBaseDir;
    private File slaveBaseDir;

    @Before
    public void before() {
        masterBaseDir = new File(System.getProperty("java.io.tmpdir", "."), "Puma");
        slaveBaseDir = new File(System.getProperty("java.io.tmpdir", "."), "PumaBak");
    }

    @Test
    public void testArchive() throws Exception {
        DefaultArchiveStrategy archiveStrategy = new DefaultArchiveStrategy();
        archiveStrategy.setServerName("test");
        archiveStrategy.setMaxMasterFileCount(5);
        LocalFileDataBucketManager masterIndex = new LocalFileDataBucketManager();
        masterIndex.setBaseDir(masterBaseDir.getAbsolutePath());
        masterIndex.setBucketFilePrefix("bucket-");
        LocalFileDataBucketManager slaveIndex = new LocalFileDataBucketManager();
        slaveIndex.setBaseDir(slaveBaseDir.getAbsolutePath());
        slaveIndex.setBucketFilePrefix("bucket-");

        for (int i = 0; i < 12; i++) {
            File masterFile = new File(masterBaseDir, "20120726/bucket-" + i);
            masterFile.getParentFile().mkdirs();
            masterFile.createNewFile();
        }

        masterIndex.setMaster(true);
        masterIndex.start();
        slaveIndex.start();
        slaveIndex.setMaster(false);

        archiveStrategy.archive(masterIndex, slaveIndex);

        Assert.assertEquals(5, masterIndex.size());
        Assert.assertEquals(7, slaveIndex.size());
        for (int i = 7; i < 12; i++) {
            Assert.assertEquals("20120726/bucket-" + i, masterIndex.getIndex().get().get(new Sequence(120726, i)));
            File masterFile = new File(masterBaseDir, "20120726/bucket-" + i);
            Assert.assertTrue(masterFile.exists());
            File slaveFile = new File(slaveBaseDir, "20120726/bucket-" + i);
            Assert.assertFalse(slaveFile.exists());
        }
        for (int i = 0; i < 7; i++) {
            Assert.assertEquals("20120726/bucket-" + i, slaveIndex.getIndex().get().get(new Sequence(120726, i)));
            File slaveFile = new File(slaveBaseDir, "20120726/bucket-" + i);
            Assert.assertTrue(slaveFile.exists());
            File masterFile = new File(masterBaseDir, "20120726/bucket-" + i);
            Assert.assertFalse(masterFile.exists());
        }

    }

    @Test
    public void testArchive2() throws Exception {
        DefaultArchiveStrategy archiveStrategy = new DefaultArchiveStrategy();
        archiveStrategy.setServerName("test");
        archiveStrategy.setMaxMasterFileCount(12);
        LocalFileDataBucketManager masterIndex = new LocalFileDataBucketManager();
        masterIndex.setBaseDir(masterBaseDir.getAbsolutePath());
        masterIndex.setBucketFilePrefix("bucket-");
        LocalFileDataBucketManager slaveIndex = new LocalFileDataBucketManager();
        slaveIndex.setBaseDir(slaveBaseDir.getAbsolutePath());
        slaveIndex.setBucketFilePrefix("bucket-");

        for (int i = 0; i < 12; i++) {
            File masterFile = new File(masterBaseDir, "20120726/bucket-" + i);
            masterFile.getParentFile().mkdirs();
            masterFile.createNewFile();
        }

        masterIndex.start();
        masterIndex.setMaster(true);
        slaveIndex.start();
        slaveIndex.setMaster(false);

        archiveStrategy.archive(masterIndex, slaveIndex);

        Assert.assertEquals(12, masterIndex.size());
        Assert.assertEquals(0, slaveIndex.size());
        for (int i = 0; i < 12; i++) {
            Assert.assertEquals("20120726/bucket-" + i, masterIndex.getIndex().get().get(new Sequence(120726, i)));
            File masterFile = new File(masterBaseDir, "20120726/bucket-" + i);
            Assert.assertTrue(masterFile.exists());
            File slaveFile = new File(slaveBaseDir, "20120726/bucket-" + i);
            Assert.assertFalse(slaveFile.exists());
        }
        for (int i = 0; i < 12; i++) {
            Assert.assertNull(slaveIndex.getIndex().get().get(new Sequence(120726, i)));
        }

    }

    @Test
    public void testArchive3() throws Exception {
        DefaultArchiveStrategy archiveStrategy = new DefaultArchiveStrategy();
        archiveStrategy.setServerName("test");
        archiveStrategy.setMaxMasterFileCount(13);
        LocalFileDataBucketManager masterIndex = new LocalFileDataBucketManager();
        masterIndex.setBaseDir(masterBaseDir.getAbsolutePath());
        masterIndex.setBucketFilePrefix("bucket-");
        LocalFileDataBucketManager slaveIndex = new LocalFileDataBucketManager();
        slaveIndex.setBaseDir(slaveBaseDir.getAbsolutePath());
        slaveIndex.setBucketFilePrefix("bucket-");

        for (int i = 0; i < 12; i++) {
            File masterFile = new File(masterBaseDir, "20120726/bucket-" + i);
            masterFile.getParentFile().mkdirs();
            masterFile.createNewFile();
        }

        masterIndex.start();
        masterIndex.setMaster(true);
        slaveIndex.start();
        slaveIndex.setMaster(false);

        archiveStrategy.archive(masterIndex, slaveIndex);

        Assert.assertEquals(12, masterIndex.size());
        Assert.assertEquals(0, slaveIndex.size());
        for (int i = 0; i < 12; i++) {
            Assert.assertEquals("20120726/bucket-" + i, masterIndex.getIndex().get().get(new Sequence(120726, i)));
            File masterFile = new File(masterBaseDir, "20120726/bucket-" + i);
            Assert.assertTrue(masterFile.exists());
            File slaveFile = new File(slaveBaseDir, "20120726/bucket-" + i);
            Assert.assertFalse(slaveFile.exists());
        }
        for (int i = 0; i < 12; i++) {
            Assert.assertNull(slaveIndex.getIndex().get().get(new Sequence(120726, i)));
        }

    }

    @Test
    public void testArchive4() throws Exception {
        DefaultArchiveStrategy archiveStrategy = new DefaultArchiveStrategy();
        archiveStrategy.setServerName("test");
        archiveStrategy.setMaxMasterFileCount(5);
        LocalFileDataBucketManager masterIndex = new LocalFileDataBucketManager();
        masterIndex.setBaseDir(masterBaseDir.getAbsolutePath());
        masterIndex.setBucketFilePrefix("bucket-");
        LocalFileDataBucketManager slaveIndex = new LocalFileDataBucketManager();
        slaveIndex.setBaseDir(slaveBaseDir.getAbsolutePath());
        slaveIndex.setBucketFilePrefix("bucket-");

        for (int i = 0; i < 12; i++) {
            File masterFile = new File(masterBaseDir, "20120726/bucket-" + i);
            masterFile.getParentFile().mkdirs();
            masterFile.createNewFile();
        }

        for (int i = 0; i < 12; i++) {
            File slaveFile = new File(slaveBaseDir, "20120725/bucket-" + i);
            slaveFile.getParentFile().mkdirs();
            slaveFile.createNewFile();
        }

        masterIndex.start();
        masterIndex.setMaster(true);
        slaveIndex.start();
        slaveIndex.setMaster(false);

        archiveStrategy.archive(masterIndex, slaveIndex);

        Assert.assertEquals(5, masterIndex.size());
        Assert.assertEquals(19, slaveIndex.size());
        for (int i = 7; i < 12; i++) {
            Assert.assertEquals("20120726/bucket-" + i, masterIndex.getIndex().get().get(new Sequence(120726, i)));
            File masterFile = new File(masterBaseDir, "20120726/bucket-" + i);
            Assert.assertTrue(masterFile.exists());
            File slaveFile = new File(slaveBaseDir, "20120726/bucket-" + i);
            Assert.assertFalse(slaveFile.exists());
        }
        for (int i = 0; i < 7; i++) {
            Assert.assertEquals("20120726/bucket-" + i, slaveIndex.getIndex().get().get(new Sequence(120726, i)));
            File slaveFile = new File(slaveBaseDir, "20120726/bucket-" + i);
            Assert.assertTrue(slaveFile.exists());
            File masterFile = new File(masterBaseDir, "20120726/bucket-" + i);
            Assert.assertFalse(masterFile.exists());
        }

        for (int i = 0; i < 12; i++) {
            Assert.assertEquals("20120725/bucket-" + i, slaveIndex.getIndex().get().get(new Sequence(120725, i)));
            File slaveFile = new File(slaveBaseDir, "20120725/bucket-" + i);
            Assert.assertTrue(slaveFile.exists());

        }

    }

    @After
    public void after() throws Exception {
        if (masterBaseDir != null && masterBaseDir.exists() && masterBaseDir.isDirectory()) {
            FileUtils.deleteDirectory(masterBaseDir);
        }
        if (slaveBaseDir != null && slaveBaseDir.exists() && slaveBaseDir.isDirectory()) {
            FileUtils.deleteDirectory(slaveBaseDir);
        }
    }
}
