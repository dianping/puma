package com.dianping.puma.storage.data;

import com.dianping.puma.core.event.RowChangedEvent;
import com.dianping.puma.core.util.sql.DMLType;
import com.dianping.puma.storage.Sequence;
import com.dianping.puma.storage.StorageBaseTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class SingleWriteDataManagerTest extends StorageBaseTest {

    SingleWriteDataManager singleWriteDataManager;

    SingleReadDataManager singleReadDataManager;

    File bucket;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        bucket = new File(testDir, "bucket");
        createFile(bucket);

        singleWriteDataManager = DataManagerFactory.newSingleWriteDataManager(bucket, "20151010", 0);
        singleWriteDataManager.start();

        singleReadDataManager = DataManagerFactory.newSingleReadDataManager(bucket);
        singleReadDataManager.start();
    }

    @Test
    public void testAppendAndFlush() throws IOException {
        singleWriteDataManager.append(
                new RowChangedEvent(0, 1, "2", 3).setDmlType(DMLType.INSERT)
        );

        singleWriteDataManager.append(
                new RowChangedEvent(1, 2, "3", 4).setDmlType(DMLType.INSERT)
        );

        singleWriteDataManager.append(
                new RowChangedEvent(2, 3, "4", 5).setDmlType(DMLType.INSERT)
        );

        singleWriteDataManager.flush();

        singleReadDataManager.open(new Sequence(2015, 0, 0));
        assertEquals(singleReadDataManager.next(), new RowChangedEvent(0, 1, "2", 3).setDmlType(DMLType.INSERT));
        assertEquals(singleReadDataManager.next(), new RowChangedEvent(1, 2, "3", 4).setDmlType(DMLType.INSERT));
        assertEquals(singleReadDataManager.next(), new RowChangedEvent(2, 3, "4", 5).setDmlType(DMLType.INSERT));
    }

    @Test
    public void testHasRemainingForWrite() throws Exception {

    }

    @Override
    @After
    public void tearDown() throws Exception {
        if (singleWriteDataManager != null) {
            singleWriteDataManager.stop();
        }

        if (singleReadDataManager != null) {
            singleReadDataManager.stop();
        }

        super.tearDown();
    }
}