package com.dianping.puma.storage.data.impl;

import com.dianping.puma.storage.Sequence;
import com.dianping.puma.storage.data.factory.DataBucketFactory;
import com.dianping.puma.storage.data.ReadDataBucket;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

public class LocalFileDataBucketManagerTest {

	private File tempDir;

	private LocalFileDataBucketManager manager;

	@Before
	public void before() throws IOException {
		tempDir = new File(System.getProperty("java.io.tmpdir"), "test");

		FileUtils.deleteDirectory(tempDir);
		createNewFolder(tempDir);

		manager = new LocalFileDataBucketManager(tempDir.getName(), "test");
		manager.start();
		createNewFolder(manager.rootDir());
	}

	@Test
	public void testFindReadDataBucket_0() throws Exception {
		// Functionality.

		// Case 0: 20150925(Bucket-0, Bucket-1), 20150926(Bucket-0, Bucket-1, Bucket-2)
		File file_0;
		file_0 = new File(manager.rootDir(), "20150925");
		createNewFolder(file_0);
		createNewFile(new File(file_0, "Bucket-0"));
		createNewFile(new File(file_0, "Bucket-1"));

		file_0 = new File(manager.rootDir(), "20150926");
		createNewFolder(file_0);
		createNewFile(new File(file_0, "Bucket-0"));
		createNewFile(new File(file_0, "Bucket-1"));
		createNewFile(new File(file_0, "Bucket-2"));

		ReadDataBucket expected_0 = DataBucketFactory.newLocalFileReadDataBucket(
				new Sequence(20150925, 0, 0),
				new File(new File(manager.rootDir(), "20150925"), "Bucket-0"));
		ReadDataBucket result_0 = manager.findReadDataBucket(new Sequence(20150925, 0, 100));
		assertTrue(EqualsBuilder.reflectionEquals(expected_0, result_0));

		ReadDataBucket expected_1 = DataBucketFactory.newLocalFileReadDataBucket(
				new Sequence(20150926, 1, 0),
				new File(new File(manager.rootDir(), "20150926"), "Bucket-1"));
		ReadDataBucket result_1 = manager.findReadDataBucket(new Sequence(20150926, 1, 0));
		assertTrue(EqualsBuilder.reflectionEquals(expected_1, result_1));

		ReadDataBucket expected_2 = DataBucketFactory.newLocalFileReadDataBucket(
				new Sequence(20150926, 1, 0),
				new File(new File(manager.rootDir(), "20150926"), "Bucket-1"));
		ReadDataBucket result_2 = manager.findReadDataBucket(new Sequence(20150926, 1, 20000));
		assertTrue(EqualsBuilder.reflectionEquals(expected_2, result_2));

		ReadDataBucket expected_3 = DataBucketFactory.newLocalFileReadDataBucket(
				new Sequence(20150925, 0, 0),
				new File(new File(manager.rootDir(), "20150925"), "Bucket-0"));
		ReadDataBucket result_3 = manager.findReadDataBucket(new Sequence(20150925, 0, 1000));
		assertTrue(EqualsBuilder.reflectionEquals(expected_3, result_3));
	}

	@Test
	public void testFindReadDataBucket_1() throws IOException {
		// Corner.

		// Case 0: 20150925(Bucket-0, Bucket-1), 20150926(Bucket-0, Bucket-1, Bucket-2)
		File file_0;
		file_0 = new File(manager.rootDir(), "20150925");
		createNewFolder(file_0);
		createNewFile(new File(file_0, "Bucket-0"));
		createNewFile(new File(file_0, "Bucket-1"));

		file_0 = new File(manager.rootDir(), "20150926");
		createNewFolder(file_0);
		createNewFile(new File(file_0, "Bucket-0"));
		createNewFile(new File(file_0, "Bucket-1"));
		createNewFile(new File(file_0, "Bucket-2"));

		ReadDataBucket result_0 = manager.findReadDataBucket(new Sequence(20150926, 3, 100));
		assertNull(result_0);

		ReadDataBucket result_1 = manager.findReadDataBucket(new Sequence(20150924, 3, 100));
		assertNull(result_1);

		ReadDataBucket result_2 = manager.findReadDataBucket(new Sequence(20150925, 5, 100));
		assertNull(result_2);
	}

	@Test
	public void testFindNextReadDataBucket() throws Exception {
		// Functionality.

		// Case 0: 20150925(Bucket-0, Bucket-1), 20150926(Bucket-0, Bucket-1, Bucket-2)
		File file_0;
		file_0 = new File(manager.rootDir(), "20150925");
		createNewFolder(file_0);
		createNewFile(new File(file_0, "Bucket-0"));
		createNewFile(new File(file_0, "Bucket-1"));

		file_0 = new File(manager.rootDir(), "20150926");
		createNewFolder(file_0);
		createNewFile(new File(file_0, "Bucket-0"));
		createNewFile(new File(file_0, "Bucket-1"));
		createNewFile(new File(file_0, "Bucket-2"));

		ReadDataBucket expected_0 = DataBucketFactory.newLocalFileReadDataBucket(
				new Sequence(20150925, 1, 0),
				new File(new File(manager.rootDir(), "20150925"), "Bucket-1"));
		ReadDataBucket result_0 = manager.findNextReadDataBucket(new Sequence(20150925, 0, 100));
		assertTrue(EqualsBuilder.reflectionEquals(expected_0, result_0));

		ReadDataBucket expected_1 = DataBucketFactory.newLocalFileReadDataBucket(
				new Sequence(20150926, 2, 0),
				new File(new File(manager.rootDir(), "20150926"), "Bucket-2"));
		ReadDataBucket result_1 = manager.findNextReadDataBucket(new Sequence(20150926, 1, 0));
		assertTrue(EqualsBuilder.reflectionEquals(expected_1, result_1));

		ReadDataBucket expected_2 = DataBucketFactory.newLocalFileReadDataBucket(
				new Sequence(20150926, 2, 0),
				new File(new File(manager.rootDir(), "20150926"), "Bucket-2"));
		ReadDataBucket result_2 = manager.findNextReadDataBucket(new Sequence(20150926, 1, 20000));
		assertTrue(EqualsBuilder.reflectionEquals(expected_2, result_2));

		ReadDataBucket expected_3 = DataBucketFactory.newLocalFileReadDataBucket(
				new Sequence(20150925, 1, 0),
				new File(new File(manager.rootDir(), "20150925"), "Bucket-1"));
		ReadDataBucket result_3 = manager.findNextReadDataBucket(new Sequence(20150925, 0, 1000));
		assertTrue(EqualsBuilder.reflectionEquals(expected_3, result_3));
	}

	@Test
	public void testLoadIndex() throws Exception {

	}

	@Test
	public void testBuildSequence_0() throws Exception {
		// Functionality.

		// Case 0: 20150810, Bucket-5
		File dateFile_0 = new File(tempDir.getName() + "puma", "20150810");
		File bucketFile_0 = new File(dateFile_0, "Bucket-5");
		Sequence expected_0 = new Sequence(20150810, 5);
		assertTrue(EqualsBuilder.reflectionEquals(expected_0, manager.buildSequence(dateFile_0, bucketFile_0)));

		// Case 1: 20161230, Bucket-0
		File dateFile_1 = new File(tempDir.getName() + "puma", "20161230");
		File bucketFile_1 = new File(dateFile_1, "Bucket-0");
		Sequence expected_1 = new Sequence(20161230, 0);
		assertTrue(EqualsBuilder.reflectionEquals(expected_1, manager.buildSequence(dateFile_1, bucketFile_1)));
	}

	@Test(expected = IOException.class)
	public void testBuildSequence_1() throws Exception {
		// Exception thrown.

		// Case 0: 2015-07-10, Bucket-0
		File dateFile = new File(tempDir.getName() + "puma", "2015-07-10");
		File bucketFile = new File(dateFile, "Bucket-0");
		manager.buildSequence(dateFile, bucketFile);
	}

	@Test(expected = IOException.class)
	public void testBuildSequence_2() throws Exception {
		// Exception thrown.

		// Case 0: 20150710, bucket-0
		File dateFile = new File(tempDir.getName() + "puma", "20150710");
		File bucketFile = new File(dateFile, "bucket-0");
		manager.buildSequence(dateFile, bucketFile);
	}

	@After
	public void after() {
		try {
			manager.stop();
			FileUtils.deleteDirectory(tempDir);
		} catch (IOException io) {
			throw new RuntimeException("failed to delete temp directory.");
		}
	}

	protected void createNewFile(File file) throws IOException {
		if (!file.createNewFile()) {
			throw new RuntimeException("failed to create new file.");
		}
	}

	protected void createNewFolder(File file) throws IOException {
		if (!file.mkdirs()) {
			throw new RuntimeException("failed to create new folder.");
		}
	}
}