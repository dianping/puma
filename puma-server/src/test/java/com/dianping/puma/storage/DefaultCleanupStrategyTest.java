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
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Properties;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.dianping.puma.storage.bucket.LocalFileDataBucketManager;
import com.dianping.puma.storage.index.BinlogIndexKey;
import com.dianping.puma.storage.index.BinlogIndexKeyConvertor;
import com.dianping.puma.storage.index.DefaultDataIndexImpl;
import com.dianping.puma.storage.index.LongIndexItemConvertor;

/**
 * TODO Comment of DefaultCleanupStrategyTest
 * 
 * @author Leo Liang
 * 
 */
public class DefaultCleanupStrategyTest {

    private File baseDir;
    private File binlogIndexBaseDir;

    @Before
    public void before() {
        baseDir = new File(System.getProperty("java.io.tmpdir", "."), "Puma");
        binlogIndexBaseDir = new File(System.getProperty("java.io.tmpdir", "."), "binlogIndex");
    }

    @Test
    public void testCleanup() throws Exception {
        int preservedDay = 5;
        DefaultCleanupStrategy defaultCleanupStrategy = new DefaultCleanupStrategy();
        defaultCleanupStrategy.setPreservedDay(preservedDay);
        LocalFileDataBucketManager index = new LocalFileDataBucketManager();
        index.setBaseDir(baseDir.getAbsolutePath());
        index.setBucketFilePrefix("bucket-");

        DefaultDataIndexImpl<BinlogIndexKey, Long> binlogIndex = new DefaultDataIndexImpl<BinlogIndexKey, Long>(
                binlogIndexBaseDir.getAbsolutePath(), new LongIndexItemConvertor(), new BinlogIndexKeyConvertor());

        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        binlogIndex.start();
        defaultCleanupStrategy.addDataIndex(binlogIndex);

        for (int i = 0; i <= 12; i++) {
            cal.add(Calendar.DAY_OF_MONTH, i == 0 ? 0 : -1);
            File file = new File(baseDir, sdf.format(cal.getTime()) + "/bucket-0");
            file.getParentFile().mkdirs();
            file.createNewFile();
            binlogIndex.addL1Index(new BinlogIndexKey("dd", i, i), sdf.format(cal.getTime()) + "-bucket-0");
            binlogIndex.addL2Index(new BinlogIndexKey("dd", i, i), (long) i);
        }

        index.start();

        defaultCleanupStrategy.cleanup(index);

        Assert.assertEquals(preservedDay, index.size());

        sdf = new SimpleDateFormat("yyMMdd");

        Properties binlogIndexL1 = new Properties();
        binlogIndexL1.load(new FileInputStream(new File(binlogIndexBaseDir, "l1Index.l1idx")));
        Assert.assertEquals(preservedDay, binlogIndexL1.size());

        cal = Calendar.getInstance();
        for (int i = 0; i < preservedDay; i++) {
            cal.add(Calendar.DAY_OF_MONTH, i == 0 ? 0 : -1);

            File file = new File(baseDir, "20" + sdf.format(cal.getTime()) + "/bucket-0");
            Assert.assertTrue(file.exists());
            Assert.assertNotNull(index.getReadBucket(
                    new Sequence(Integer.valueOf(sdf.format(cal.getTime())), 0).longValue(), true));
            File binlogIndexL2 = new File(new File(binlogIndexBaseDir, "l2Index"), "20" + sdf.format(cal.getTime())
                    + "-bucket-0" + ".l2idx");
            Assert.assertTrue(binlogIndexL2.exists());
            Assert.assertEquals("20" + sdf.format(cal.getTime()) + "-bucket-0",
                    binlogIndexL1.get(i + "!" + "dd" + "!" + i));
        }

        cal = Calendar.getInstance();
        for (int i = preservedDay; i <= 12; i++) {
            cal.add(Calendar.DAY_OF_MONTH, -1 * preservedDay);

            File file = new File(baseDir, "20" + sdf.format(cal.getTime()) + "/bucket-0");
            Assert.assertFalse(file.exists());
            Assert.assertNull(index.getReadBucket(
                    new Sequence(Integer.valueOf(sdf.format(cal.getTime())), 0).longValue(), true));
            File binlogIndexL2 = new File(new File(binlogIndexBaseDir, "l2Index"), "20" + sdf.format(cal.getTime())
                    + "-bucket-0" + ".l2idx");
            Assert.assertFalse(binlogIndexL2.exists());
        }

    }

    @After
    public void after() throws Exception {
        if (baseDir != null && baseDir.exists() && baseDir.isDirectory()) {
            FileUtils.deleteDirectory(baseDir);
        }
        if (binlogIndexBaseDir != null && binlogIndexBaseDir.exists() && binlogIndexBaseDir.isDirectory()) {
            FileUtils.deleteDirectory(binlogIndexBaseDir);
        }
    }

}
