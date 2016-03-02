package com.dianping.puma.storage.bucket;

import com.dianping.puma.storage.StorageBaseTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;

import static org.junit.Assert.*;

public class LengthWriteBucketTest extends StorageBaseTest {

	LengthWriteBucket lengthWriteBucket;

	File bucket;

	@Before
	public void setUp() throws Exception {
		super.setUp();

		bucket = new File(testDir, "bucket");
		createFile(bucket);

		lengthWriteBucket = BucketFactory.newLengthWriteBucket(bucket, 1024, 32);
		lengthWriteBucket.start();
	}

	@After
	public void tearDown() throws Exception {
		lengthWriteBucket.stop();

		super.tearDown();
	}

	@Test
	public void testAppendAndFlush() throws Exception {
		lengthWriteBucket.append("apple".getBytes());
		lengthWriteBucket.append("banana".getBytes());
		lengthWriteBucket.append("car".getBytes());
		lengthWriteBucket.append("dog".getBytes());
		lengthWriteBucket.flush();

		DataInputStream is = new DataInputStream(new FileInputStream(bucket));
		byte[] data;

		assertEquals("apple".getBytes().length, is.readInt());
		data = new byte["apple".getBytes().length];
		is.readFully(data);
		assertArrayEquals("apple".getBytes(), data);

		assertEquals("banana".getBytes().length, is.readInt());
		data = new byte["banana".getBytes().length];
		is.readFully(data);
		assertArrayEquals("banana".getBytes(), data);

		assertEquals("car".getBytes().length, is.readInt());
		data = new byte["car".getBytes().length];
		is.readFully(data);
		assertArrayEquals("car".getBytes(), data);

		assertEquals("dog".getBytes().length, is.readInt());
		data = new byte["dog".getBytes().length];
		is.readFully(data);
		assertArrayEquals("dog".getBytes(), data);
	}

	@Test
	public void testHasRemainingForWrite() throws Exception {
		lengthWriteBucket.append("1234".getBytes());
		assertTrue(lengthWriteBucket.hasRemainingForWrite());

		lengthWriteBucket.append("1234".getBytes());
		assertTrue(lengthWriteBucket.hasRemainingForWrite());

		lengthWriteBucket.append("1234".getBytes());
		assertTrue(lengthWriteBucket.hasRemainingForWrite());

		lengthWriteBucket.append("123".getBytes());
		assertTrue(lengthWriteBucket.hasRemainingForWrite());

		lengthWriteBucket.append("1".getBytes());
		assertFalse(lengthWriteBucket.hasRemainingForWrite());

		lengthWriteBucket.append("1234".getBytes());
		assertFalse(lengthWriteBucket.hasRemainingForWrite());
	}
}