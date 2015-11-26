package com.dianping.puma.storage.holder.impl;

import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.storage.filesystem.FileSystem;
import junit.framework.Assert;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

/**
 * Dozer @ 2015-11
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public class DefaultBinlogInfoHolderTest {
    private static File baseDir = new File(System.getProperty("java.io.tmpdir", "."), "puma");

    private DefaultBinlogInfoHolder binlogInfoHolder;

    private BinlogInfo binlogInfo = new BinlogInfo()
            .setBinlogFile("xxx")
            .setBinlogPosition(123)
            .setServerId(321)
            .setEventIndex(2)
            .setTimestamp(1234567890);

    @Before
    public void setUp() throws Exception {
        if (baseDir.exists()) {
            baseDir.delete();
        }
        baseDir.mkdirs();

        FileSystem.changeBasePath(baseDir.getAbsolutePath());
        binlogInfoHolder = new DefaultBinlogInfoHolder();
        binlogInfoHolder.init();
    }

    @After
    public void tearDown() throws Exception {
        FileUtils.deleteDirectory(baseDir);
    }

    @Test
    public void testSetAndGet() throws Exception {
        binlogInfoHolder.setBinlogInfo("test", binlogInfo);
        Assert.assertNull(binlogInfoHolder.getBinlogInfo("not_exist"));
        Assert.assertEquals(binlogInfoHolder.getBinlogInfo("test"), binlogInfo);
    }

    @Test
    public void testRemove() throws Exception {
        binlogInfoHolder.setBinlogInfo("test1", binlogInfo);
        binlogInfoHolder.remove("test1");

        Assert.assertNull(binlogInfoHolder.getBinlogInfo("test1"));
    }

    @Test
    public void testRename() throws Exception {
        binlogInfoHolder.setBinlogInfo("test1", binlogInfo);
        binlogInfoHolder.rename("test1", "test2");

        Assert.assertNull(binlogInfoHolder.getBinlogInfo("test1"));
        Assert.assertEquals(binlogInfoHolder.getBinlogInfo("test2"), binlogInfo);
    }
}