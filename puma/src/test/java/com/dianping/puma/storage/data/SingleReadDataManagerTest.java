package com.dianping.puma.storage.data;

import com.dianping.puma.core.event.RowChangedEvent;
import com.dianping.puma.core.util.sql.DMLType;
import com.dianping.puma.storage.Sequence;
import com.dianping.puma.storage.StorageBaseTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class SingleReadDataManagerTest extends StorageBaseTest {

    SingleReadDataManager singleReadDataManager;

    SingleWriteDataManager singleWriteDataManager;

    File bucket;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        bucket = new File(testDir, "bucket");
        createFile(bucket);

        singleReadDataManager = DataManagerFactory.newSingleReadDataManager(bucket);
        singleReadDataManager.start();

        singleWriteDataManager = DataManagerFactory.newSingleWriteDataManager(bucket, "20151010", 0);
        singleWriteDataManager.start();
    }

    @Test
    public void testPosition() throws Exception {

    }

    @Test
    public void testOpenAndNext() throws IOException {
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

    @Test(expected = EOFException.class)
    public void testOpenAndNextEOF() throws IOException {
        singleReadDataManager.open(new Sequence(new com.dianping.puma.storage.Sequence(2015, 0, 0)));
        singleReadDataManager.next();
    }

    @Override
    @After
    public void tearDown() throws Exception {
        if (singleReadDataManager != null) {
            singleReadDataManager.stop();
        }

        if (singleWriteDataManager != null) {
            singleWriteDataManager.stop();
        }

        super.tearDown();
    }
}