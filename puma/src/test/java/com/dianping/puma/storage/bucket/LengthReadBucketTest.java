package com.dianping.puma.storage.bucket;

import com.dianping.puma.storage.StorageBaseTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.*;

import static org.junit.Assert.assertArrayEquals;

public class LengthReadBucketTest extends StorageBaseTest {

    LengthReadBucket lengthReadBucket;

    File bucket;

    @Before
    public void setUp() throws Exception {
        super.setUp();

        bucket = new File(testDir, "bucket");
        createFile(bucket);

        lengthReadBucket = BucketFactory.newLengthReadBucket(bucket);
        lengthReadBucket.start();
    }

    @After
    public void tearDown() throws Exception {
        lengthReadBucket.stop();

        super.tearDown();
    }

    @Test
    public void testNext() throws Exception {
        DataOutputStream os = new DataOutputStream(new FileOutputStream(bucket));
        os.writeInt("apple".getBytes().length);
        os.write("apple".getBytes());
        os.writeInt("banana".getBytes().length);
        os.write("banana".getBytes());
        os.writeInt("car".getBytes().length);
        os.write("car".getBytes());
        os.writeInt("dog".getBytes().length);
        os.write("dog".getBytes());
        os.flush();

        assertArrayEquals("apple".getBytes(), lengthReadBucket.next());
        assertArrayEquals("banana".getBytes(), lengthReadBucket.next());
        assertArrayEquals("car".getBytes(), lengthReadBucket.next());
        assertArrayEquals("dog".getBytes(), lengthReadBucket.next());
    }

    @Test(expected = EOFException.class)
    public void testNextEOF() throws IOException {
        DataOutputStream os = new DataOutputStream(new FileOutputStream(bucket));
        os.writeInt("apple".getBytes().length);
        os.write("appl".getBytes());
        os.flush();

        lengthReadBucket.next();
    }

    @Test(expected = IOException.class)
    public void testNextNegativeLength() throws IOException {
        DataOutputStream os = new DataOutputStream(new FileOutputStream(bucket));
        os.writeInt(-1);
        os.write("apple".getBytes());
        os.flush();

        lengthReadBucket.next();
    }

    @Test
    public void testSkip() throws Exception {
        DataOutputStream os = new DataOutputStream(new FileOutputStream(bucket));
        os.writeInt("apple".getBytes().length);
        os.write("apple".getBytes());
        os.writeInt("banana".getBytes().length);
        os.write("banana".getBytes());
        os.writeInt("car".getBytes().length);
        os.write("car".getBytes());
        os.flush();

        assertArrayEquals("apple".getBytes(), lengthReadBucket.next());
        lengthReadBucket.skip(4 + "banana".getBytes().length);
        assertArrayEquals("car".getBytes(), lengthReadBucket.next());
    }

    @Test
    public void testCheckCompressed() throws Exception {

    }
}