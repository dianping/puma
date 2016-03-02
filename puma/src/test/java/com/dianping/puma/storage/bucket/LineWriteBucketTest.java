package com.dianping.puma.storage.bucket;

import com.dianping.puma.storage.StorageBaseTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import static org.junit.Assert.*;

public class LineWriteBucketTest extends StorageBaseTest {

	LineWriteBucket lineWriteBucket;

	File bucket;

	@Before
	public void setUp() throws Exception {
		super.setUp();

		bucket = new File(testDir, "bucket");
		createFile(bucket);

		lineWriteBucket = BucketFactory.newLineWriteBucket(bucket, 1024, 32);
		lineWriteBucket.start();
	}

	@After
	public void tearDown() throws Exception {
		if (lineWriteBucket != null) {
			lineWriteBucket.stop();
		}

		super.tearDown();
	}

	@Test
	public void testAppendAndFlush() throws Exception {
		lineWriteBucket.append("apple".getBytes());
		lineWriteBucket.append("banana".getBytes());
		lineWriteBucket.append("car".getBytes());
		lineWriteBucket.append("dog".getBytes());
		lineWriteBucket.flush();

		BufferedReader reader = new BufferedReader(new FileReader(bucket));
		assertEquals("apple", reader.readLine());
		assertEquals("banana", reader.readLine());
		assertEquals("car", reader.readLine());
		assertEquals("dog", reader.readLine());
	}

	@Test
	public void testHasRemainingForWrite() throws Exception {
		lineWriteBucket.append("1234567".getBytes());
		lineWriteBucket.flush();
		assertTrue(lineWriteBucket.hasRemainingForWrite());

		lineWriteBucket.append("1234567".getBytes());
		lineWriteBucket.flush();
		assertTrue(lineWriteBucket.hasRemainingForWrite());

		lineWriteBucket.append("1234567".getBytes());
		lineWriteBucket.flush();
		assertTrue(lineWriteBucket.hasRemainingForWrite());

		lineWriteBucket.append("12345".getBytes());
		lineWriteBucket.flush();
		assertTrue(lineWriteBucket.hasRemainingForWrite());

		lineWriteBucket.append("6".getBytes());
		lineWriteBucket.flush();
		assertFalse(lineWriteBucket.hasRemainingForWrite());

		lineWriteBucket.append("1234567".getBytes());
		lineWriteBucket.flush();
		assertFalse(lineWriteBucket.hasRemainingForWrite());
	}
}