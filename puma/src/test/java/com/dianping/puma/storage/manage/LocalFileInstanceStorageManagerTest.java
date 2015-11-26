package com.dianping.puma.storage.manage;

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
public class LocalFileInstanceStorageManagerTest {
    private static File baseDir = new File(System.getProperty("java.io.tmpdir", "."), "puma");

    private LocalFileInstanceStorageManager localFileInstanceStorageManager;

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
        localFileInstanceStorageManager = new LocalFileInstanceStorageManager();
        localFileInstanceStorageManager.init();
    }

    @After
    public void tearDown() throws Exception {
        FileUtils.deleteDirectory(baseDir);
    }

    @Test
    public void testSetAndGet() throws Exception {
        localFileInstanceStorageManager.setBinlogInfo("test", binlogInfo);
        Assert.assertNull(localFileInstanceStorageManager.getBinlogInfo("not_exist"));
        Assert.assertEquals(localFileInstanceStorageManager.getBinlogInfo("test"), binlogInfo);
    }

    @Test
    public void testRemove() throws Exception {
        localFileInstanceStorageManager.setBinlogInfo("test1", binlogInfo);
        localFileInstanceStorageManager.remove("test1");

        Assert.assertNull(localFileInstanceStorageManager.getBinlogInfo("test1"));
    }

    @Test
    public void testRename() throws Exception {
        localFileInstanceStorageManager.setBinlogInfo("test1", binlogInfo);
        localFileInstanceStorageManager.rename("test1", "test2");

        Assert.assertNull(localFileInstanceStorageManager.getBinlogInfo("test1"));
        Assert.assertEquals(localFileInstanceStorageManager.getBinlogInfo("test2"), binlogInfo);
    }
}