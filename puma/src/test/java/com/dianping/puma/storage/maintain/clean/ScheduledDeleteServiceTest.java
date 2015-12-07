package com.dianping.puma.storage.maintain.clean;

import com.dianping.puma.core.event.EventFactory;
import com.dianping.puma.core.event.RowChangedEvent;
import com.dianping.puma.core.util.sql.DMLType;
import com.dianping.puma.storage.Sequence;
import com.dianping.puma.storage.StorageBaseTest;
import com.dianping.puma.storage.data.GroupReadDataManager;
import com.dianping.puma.storage.data.GroupWriteDataManager;
import com.dianping.puma.storage.filesystem.FileSystem;
import com.dianping.puma.storage.index.SeriesReadIndexManager;
import com.dianping.puma.storage.index.SeriesWriteIndexManager;
import com.dianping.puma.storage.utils.DateUtils;
import junit.framework.Assert;
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
    public void testDeleAteOldData() throws Exception {
        SeriesWriteIndexManager indexManager = new SeriesWriteIndexManager("test");
        indexManager.start();
        GroupWriteDataManager dataManager = new GroupWriteDataManager("test");
        dataManager.start();

        DateUtils.changeGetNowTime("20150101");
        RowChangedEvent event1 = EventFactory.dml(1, 1, "f.1", 1, "a", "b", false, false, DMLType.INSERT);
        Sequence sequence1 = dataManager.append(event1);
        indexManager.append(event1.getBinlogInfo(), sequence1);

        DateUtils.changeGetNowTime("20150102");
        RowChangedEvent event2 = EventFactory.dml(2, 1, "f.1", 2, "a", "b", false, false, DMLType.INSERT);
        Sequence sequence2 = dataManager.append(event2);
        indexManager.append(event2.getBinlogInfo(), sequence2);

        DateUtils.changeGetNowTime("20150103");
        RowChangedEvent event3 = EventFactory.dml(3, 1, "f.1", 3, "a", "b", false, false, DMLType.INSERT);
        Sequence sequence3 = dataManager.append(event3);
        indexManager.append(event3.getBinlogInfo(), sequence3);

        DateUtils.changeGetNowTime("20150104");
        indexManager.flush();
        dataManager.flush();

        scheduledDeleteService.scheduledDelete();

        RowChangedEvent event4 = EventFactory.dml(3, 1, "f.1", 3, "a", "b", false, false, DMLType.INSERT);
        Sequence sequence4 = dataManager.append(event4);
        indexManager.append(event4.getBinlogInfo(), sequence4);
        indexManager.flush();
        dataManager.flush();

        SeriesReadIndexManager readIndexManager = new SeriesReadIndexManager("test");
        readIndexManager.start();
        GroupReadDataManager readDataManager = new GroupReadDataManager("test");
        readDataManager.start();

        Sequence seq = readIndexManager.findOldest();
        readDataManager.open(seq);

        Assert.assertEquals(readDataManager.next(), event2);
        Assert.assertEquals(readDataManager.next(), event3);
        Assert.assertEquals(readDataManager.next(), event4);
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