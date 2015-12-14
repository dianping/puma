package com.dianping.puma.storage.cleanup;

import com.dianping.puma.storage.StorageBaseTest;
import com.dianping.puma.storage.filesystem.FileSystem;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ScheduledDeleteServiceTest extends StorageBaseTest {

    ScheduledDeleteService scheduledDeleteService = new ScheduledDeleteService();

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        FileSystem.changeBasePath(testDir.getAbsolutePath());
        scheduledDeleteService.setDeleteStrategy(new ExpiredDeleteStrategy());
    }

    @Test
    public void testDeleteDirectory() throws Exception {
        File directory0 = new File(testDir, "directory0");
        createDirectory(directory0);

        assertTrue(directory0.exists());
        scheduledDeleteService.deleteDirectory(directory0);
        assertFalse(directory0.exists());

        File directory1 = new File(testDir, "directory1/hello");
        createDirectory(directory1);

        assertTrue(directory1.exists());
        scheduledDeleteService.deleteDirectory(directory1);
        assertFalse(directory1.exists());
    }

    @Override
    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }
}