package com.dianping.puma.storage;

import com.dianping.puma.core.event.RowChangedEvent;
import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.storage.data.GroupReadDataManager;
import com.dianping.puma.storage.data.GroupWriteDataManager;
import com.dianping.puma.storage.filesystem.FileSystem;
import com.dianping.puma.storage.index.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class StorageTest extends StorageBaseTest {

    SeriesReadIndexManager readIndexManager;

    GroupReadDataManager readDataManager;

    SeriesWriteIndexManager writeIndexManager;

    GroupWriteDataManager writeDataManager;

    @Override
    @Before
    public void setUp() throws IOException {
        super.setUp();

        FileSystem.testMode(testDir.getAbsolutePath());

        readIndexManager = new SeriesReadIndexManager("puma");
        readIndexManager.start();

        readDataManager = new GroupReadDataManager("puma");
        readDataManager.start();

        writeIndexManager = new SeriesWriteIndexManager("puma");
        writeIndexManager.start();

        writeDataManager = new GroupWriteDataManager("puma");
        writeDataManager.start();
    }

    @Test
    public void testFindOldest() throws IOException {
        Sequence sequence0 = writeDataManager.pageAppend(new RowChangedEvent(1, 1, "1", 1));
        writeIndexManager.pageAppend(new BinlogInfo(1, 1, "1", 1), sequence0);

        Sequence sequence1 = writeDataManager.append(new RowChangedEvent(2, 1, "1", 2));
        writeIndexManager.append(new BinlogInfo(2, 1, "1", 2), sequence1);

        Sequence sequence2 = writeDataManager.append(new RowChangedEvent(3, 1, "1", 3));
        writeIndexManager.append(new BinlogInfo(3, 1, "1", 3), sequence2);

        writeDataManager.flush();
        writeIndexManager.flush();

        Sequence sequence = readIndexManager.findOldest();
        readDataManager.open(sequence);
        assertEquals(new RowChangedEvent(1, 1, "1", 1), readDataManager.next());
        assertEquals(new RowChangedEvent(2, 1, "1", 2), readDataManager.next());
        assertEquals(new RowChangedEvent(3, 1, "1", 3), readDataManager.next());
    }

    @Test
    public void testFindLatest() throws IOException {
        Sequence sequence0 = writeDataManager.pageAppend(new RowChangedEvent(1, 1, "1", 1));
        writeIndexManager.pageAppend(new BinlogInfo(1, 1, "1", 1), sequence0);

        Sequence sequence1 = writeDataManager.append(new RowChangedEvent(2, 1, "1", 2));
        writeIndexManager.append(new BinlogInfo(2, 1, "1", 2), sequence1);

        Sequence sequence2 = writeDataManager.append(new RowChangedEvent(3, 1, "1", 3));
        writeIndexManager.append(new BinlogInfo(3, 1, "1", 3), sequence2);

        writeDataManager.flush();
        writeIndexManager.flush();

        Sequence sequence = readIndexManager.findLatest();
        readDataManager.open(sequence);
        assertEquals(new RowChangedEvent(3, 1, "1", 3), readDataManager.next());
    }

    @Test
    public void testFind0() throws IOException {
        Sequence sequence0 = writeDataManager.pageAppend(new RowChangedEvent(1, 1, "1", 1));
        writeIndexManager.pageAppend(new BinlogInfo(1, 1, "1", 1), sequence0);

        Sequence sequence1 = writeDataManager.append(new RowChangedEvent(2, 1, "1", 2));
        writeIndexManager.append(new BinlogInfo(2, 1, "1", 2), sequence1);

        Sequence sequence2 = writeDataManager.append(new RowChangedEvent(3, 1, "1", 3));
        writeIndexManager.append(new BinlogInfo(3, 1, "1", 3), sequence2);

        writeDataManager.flush();
        writeIndexManager.flush();

        Sequence sequence = readIndexManager.find(new BinlogInfo(2, 1, "1", 2));
        readDataManager.open(sequence);
        assertEquals(new RowChangedEvent(2, 1, "1", 2), readDataManager.next());
    }

    @Test
    public void testFind1() throws IOException {
        Sequence sequence0 = writeDataManager.pageAppend(new RowChangedEvent(1, 1, "1", 1));
        writeIndexManager.pageAppend(new BinlogInfo(1, 1, "1", 1), sequence0);

        Sequence sequence1 = writeDataManager.append(new RowChangedEvent(1, 1, "1", 2));
        writeIndexManager.append(new BinlogInfo(1, 1, "1", 2), sequence1);

        Sequence sequence2 = writeDataManager.append(new RowChangedEvent(1, 1, "1", 3));
        writeIndexManager.append(new BinlogInfo(1, 1, "1", 3), sequence2);

        writeDataManager.flush();
        writeIndexManager.flush();

        Sequence sequence = readIndexManager.find(new BinlogInfo(1, 1, "1", 2));
        readDataManager.open(sequence);
        assertEquals(new RowChangedEvent(1, 1, "1", 2), readDataManager.next());
    }

    @Test
    public void testCrossFile() throws IOException {
        Sequence sequence0 = writeDataManager.pageAppend(new RowChangedEvent(1, 1, "1", 1));
        writeIndexManager.pageAppend(new BinlogInfo(1, 1, "1", 1), sequence0);

        Sequence sequence1 = writeDataManager.append(new RowChangedEvent(2, 1, "1", 2));
        writeIndexManager.append(new BinlogInfo(2, 1, "1", 2), sequence1);

        Sequence sequence2 = writeDataManager.append(new RowChangedEvent(3, 1, "1", 3));
        writeIndexManager.append(new BinlogInfo(3, 1, "1", 3), sequence2);

        writeDataManager.flush();
        writeIndexManager.flush();

        Sequence sequence = readIndexManager.findOldest();
        readDataManager.open(sequence);
        assertEquals(new RowChangedEvent(1, 1, "1", 1), readDataManager.next());
        assertEquals(new RowChangedEvent(2, 1, "1", 2), readDataManager.next());
        assertEquals(new RowChangedEvent(3, 1, "1", 3), readDataManager.next());
    }

    @Test
    public void testServerIdChanged0() throws IOException {
        Sequence sequence0 = writeDataManager.pageAppend(new RowChangedEvent(1, 1, "1", 1));
        writeIndexManager.pageAppend(new BinlogInfo(1, 1, "1", 1), sequence0);

        Sequence sequence1 = writeDataManager.append(new RowChangedEvent(2, 1, "1", 2));
        writeIndexManager.append(new BinlogInfo(2, 1, "1", 2), sequence1);

        Sequence sequence2 = writeDataManager.append(new RowChangedEvent(3, 1, "1", 3));
        writeIndexManager.append(new BinlogInfo(3, 1, "1", 3), sequence2);

        Sequence sequence3 = writeDataManager.append(new RowChangedEvent(1, 2, "2", 3));
        writeIndexManager.append(new BinlogInfo(1, 2, "2", 3), sequence3);

        Sequence sequence4 = writeDataManager.append(new RowChangedEvent(3, 2, "2", 8));
        writeIndexManager.append(new BinlogInfo(3, 2, "2", 8), sequence4);

        writeDataManager.flush();
        writeIndexManager.flush();

        Sequence sequence = readIndexManager.find(new BinlogInfo(3, 2, "2", 8));
        readDataManager.open(sequence);
        assertEquals(new RowChangedEvent(3, 1, "1", 3), readDataManager.next());
        assertEquals(new RowChangedEvent(1, 2, "2", 3), readDataManager.next());
        assertEquals(new RowChangedEvent(3, 2, "2", 8), readDataManager.next());
    }

    @Test
    public void testServerIdChanged1() throws IOException {
        Sequence sequence0 = writeDataManager.pageAppend(new RowChangedEvent(1, 1, "1", 1));
        writeIndexManager.pageAppend(new BinlogInfo(1, 1, "1", 1), sequence0);

        Sequence sequence1 = writeDataManager.append(new RowChangedEvent(2, 1, "1", 2));
        writeIndexManager.append(new BinlogInfo(2, 1, "1", 2), sequence1);

        Sequence sequence2 = writeDataManager.append(new RowChangedEvent(3, 1, "1", 3));
        writeIndexManager.append(new BinlogInfo(3, 1, "1", 3), sequence2);

        Sequence sequence3 = writeDataManager.pageAppend(new RowChangedEvent(1, 2, "2", 3));
        writeIndexManager.pageAppend(new BinlogInfo(1, 2, "2", 3), sequence3);

        Sequence sequence4 = writeDataManager.append(new RowChangedEvent(3, 2, "2", 8));
        writeIndexManager.append(new BinlogInfo(3, 2, "2", 8), sequence4);

        writeDataManager.flush();
        writeIndexManager.flush();

        Sequence sequence = readIndexManager.find(new BinlogInfo(3, 2, "2", 8));
        readDataManager.open(sequence);
        assertEquals(new RowChangedEvent(3, 2, "2", 8), readDataManager.next());
    }

    @Test
    public void testServerIdChanged2() throws IOException {
        Sequence sequence0 = writeDataManager.pageAppend(new RowChangedEvent(1, 1, "1", 1));
        writeIndexManager.pageAppend(new BinlogInfo(1, 1, "1", 1), sequence0);

        Sequence sequence1 = writeDataManager.append(new RowChangedEvent(2, 1, "1", 2));
        writeIndexManager.append(new BinlogInfo(2, 1, "1", 2), sequence1);

        Sequence sequence2 = writeDataManager.pageAppend(new RowChangedEvent(3, 1, "1", 3));
        writeIndexManager.pageAppend(new BinlogInfo(3, 1, "1", 3), sequence2);

        Sequence sequence3 = writeDataManager.append(new RowChangedEvent(1, 2, "2", 3));
        writeIndexManager.append(new BinlogInfo(1, 2, "2", 3), sequence3);

        Sequence sequence4 = writeDataManager.append(new RowChangedEvent(3, 2, "2", 8));
        writeIndexManager.append(new BinlogInfo(3, 2, "2", 8), sequence4);

        writeDataManager.flush();
        writeIndexManager.flush();

        Sequence sequence = readIndexManager.find(new BinlogInfo(3, 2, "2", 8));
        readDataManager.open(sequence);
        assertEquals(new RowChangedEvent(3, 1, "1", 3), readDataManager.next());
        assertEquals(new RowChangedEvent(1, 2, "2", 3), readDataManager.next());
        assertEquals(new RowChangedEvent(3, 2, "2", 8), readDataManager.next());
    }

    @Override
    @After
    public void tearDown() throws IOException {
        if (readIndexManager != null) {
            readIndexManager.stop();
        }

        if (readDataManager != null) {
            readDataManager.stop();
        }

        if (writeIndexManager != null) {
            writeIndexManager.stop();
        }

        if (writeDataManager != null) {
            writeDataManager.stop();
        }

        super.tearDown();
    }
}
