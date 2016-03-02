package com.dianping.puma.storage.bucket;

import com.dianping.puma.storage.StorageBaseTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.*;

import static org.junit.Assert.assertArrayEquals;

public class LineReadBucketTest extends StorageBaseTest {

	LineReadBucket lineReadBucket;

	File bucket;

	@Before
	public void setUp() throws Exception {
		super.setUp();

		bucket = new File(testDir, "bucket");
		createFile(bucket);

		lineReadBucket = BucketFactory.newLineReadBucket(bucket);
		lineReadBucket.start();
	}

	@After
	public void tearDown() throws Exception {
		deleteFile(bucket);
		lineReadBucket.stop();

		super.tearDown();
	}

	@Test
	public void testNext() throws Exception {
		BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(bucket));
		bufferedWriter.write("apple");
		bufferedWriter.newLine();
		bufferedWriter.write("banana");
		bufferedWriter.newLine();
		bufferedWriter.write("car");
		bufferedWriter.newLine();
		bufferedWriter.write("dog");
		bufferedWriter.newLine();
		bufferedWriter.flush();

		assertArrayEquals("apple".getBytes(), lineReadBucket.next());
		assertArrayEquals("banana".getBytes(), lineReadBucket.next());
		assertArrayEquals("car".getBytes(), lineReadBucket.next());
		assertArrayEquals("dog".getBytes(), lineReadBucket.next());
	}

	@Test(expected = EOFException.class)
	public void testNextEOF() throws Exception {
		BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(bucket));
		bufferedWriter.write("apple");
		bufferedWriter.newLine();
		bufferedWriter.flush();

		assertArrayEquals("apple".getBytes(), lineReadBucket.next());
		lineReadBucket.next();
	}

	@Test(expected = EOFException.class)
	public void testNextNull() throws IOException {
		lineReadBucket.next();
	}

	@Test
	public void testSkip() throws Exception {
		BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(bucket));
		bufferedWriter.write("apple");
		bufferedWriter.newLine();
		bufferedWriter.write("banana");
		bufferedWriter.newLine();
		bufferedWriter.write("car");
		bufferedWriter.newLine();
		bufferedWriter.write("dog");
		bufferedWriter.newLine();
		bufferedWriter.flush();

		lineReadBucket.skip(1);
		assertArrayEquals("pple".getBytes(), lineReadBucket.next());
		lineReadBucket.skip(2);
		assertArrayEquals("nana".getBytes(), lineReadBucket.next());
		lineReadBucket.skip(1);
		assertArrayEquals("ar".getBytes(), lineReadBucket.next());
		lineReadBucket.skip(0);
		assertArrayEquals("dog".getBytes(), lineReadBucket.next());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSkipNegative() throws IOException {
		lineReadBucket.skip(-1);
	}
}