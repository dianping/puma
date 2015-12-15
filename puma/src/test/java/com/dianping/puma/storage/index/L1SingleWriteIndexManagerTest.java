package com.dianping.puma.storage.index;

import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.storage.Sequence;
import com.dianping.puma.storage.StorageBaseTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.*;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class L1SingleWriteIndexManagerTest extends StorageBaseTest {

    L1SingleWriteIndexManager l1SingleWriteIndexManager;

    File bucket;

    @Before
    public void setUp() throws Exception {
        super.setUp();

        bucket = new File(testDir, "bucket");
        createFile(bucket);

        l1SingleWriteIndexManager = IndexManagerFactory.newL1SingleWriteIndexManager(bucket);
        l1SingleWriteIndexManager.start();
    }

    @Test
    public void testEncode() throws Exception {
        BinlogInfo binlogInfo0 = new BinlogInfo(1, 2, "mysql-bin.3", 4);
        Sequence sequence0 = new Sequence(5, 6);
        String string0 = "1!2!mysql-bin.3!4=5-Bucket-6";
        assertArrayEquals(string0.getBytes(), l1SingleWriteIndexManager.encode(binlogInfo0, sequence0));

        BinlogInfo binlogInfo1 = new BinlogInfo(2, 3, "mysql-bin.4", 5);
        Sequence sequence1 = new Sequence(6, 7);
        String string1 = "2!3!mysql-bin.4!5=6-Bucket-7";
        assertArrayEquals(string1.getBytes(), l1SingleWriteIndexManager.encode(binlogInfo1, sequence1));
    }

    @Test
    public void testCleanOutdated() throws Exception {
        BufferedWriter writer = new BufferedWriter(new FileWriter(bucket));
        writer.write("1!2!mysql-bin.3!2=20000101-Bucket-2");
        writer.newLine();
        writer.write("1!2!mysql-bin.3!3=30000101-Bucket-3");
        writer.newLine();
        writer.flush();
        writer.close();

        BinlogInfo binlogInfo0 = new BinlogInfo(1, 2, "mysql-bin.3", 4);
        Sequence sequence0 = new Sequence(30000101, 6);
        l1SingleWriteIndexManager.append(binlogInfo0, sequence0);
        l1SingleWriteIndexManager.flush();

        BufferedReader bufferedReader1 = new BufferedReader(new FileReader(bucket));
        assertEquals("1!2!mysql-bin.3!3=30000101-Bucket-3", bufferedReader1.readLine());
        assertEquals("1!2!mysql-bin.3!4=30000101-Bucket-6", bufferedReader1.readLine());
        bufferedReader1.close();
    }

    @Test
    public void testAppendAndFlush() throws Exception {
        BinlogInfo binlogInfo0 = new BinlogInfo(1, 2, "mysql-bin.3", 4);
        Sequence sequence0 = new Sequence(30000101, 6);
        l1SingleWriteIndexManager.append(binlogInfo0, sequence0);
        l1SingleWriteIndexManager.flush();

        BufferedReader bufferedReader1 = new BufferedReader(new FileReader(bucket));
        assertEquals("1!2!mysql-bin.3!4=30000101-Bucket-6", bufferedReader1.readLine());
        bufferedReader1.close();

        BinlogInfo binlogInfo1 = new BinlogInfo(2, 3, "mysql-bin.4", 5);
        Sequence sequence1 = new Sequence(30000102, 7);
        l1SingleWriteIndexManager.append(binlogInfo1, sequence1);
        l1SingleWriteIndexManager.flush();

        BufferedReader bufferedReader2 = new BufferedReader(new FileReader(bucket));
        bufferedReader2.readLine();
        assertEquals("2!3!mysql-bin.4!5=30000102-Bucket-7", bufferedReader2.readLine());
        bufferedReader2.close();
    }

    @After
    public void tearDown() throws Exception {
        if (l1SingleWriteIndexManager != null) {
            l1SingleWriteIndexManager.stop();
        }

        super.tearDown();
    }
}