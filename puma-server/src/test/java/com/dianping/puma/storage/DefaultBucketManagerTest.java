package com.dianping.puma.storage;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.dianping.puma.storage.bucket.AbstractDataBucketManager;
import com.dianping.puma.storage.bucket.DataBucket;
import com.dianping.puma.storage.bucket.DataBucketManager;
import com.dianping.puma.storage.bucket.DefaultBucketManager;
import com.dianping.puma.storage.bucket.LocalFileDataBucketManager;
import com.dianping.puma.storage.exception.StorageClosedException;

public class DefaultBucketManagerTest {

	public DefaultBucketManager bucketManager;

	protected File work = null;

	public DataBucketManager masterIndex;

	public DataBucketManager masterNullIndex;

	public DataBucketManager slaveIndex;

	public DataBucketManager slaveNullIndex;

	public ArchiveStrategy archiveStrategy;

	public CleanupStrategy cleanupStrategy;

	@Before
	public void before() throws Exception {

		for (int i = 0; i < 2; i++) {
			work = new File(System.getProperty("java.io.tmpdir", "."), "Puma/slave/20120710/bucket-" + Integer.toString(i));
			work.getParentFile().mkdirs();
			if (work.createNewFile()) {
				System.out.println("create a file: " + work.getAbsolutePath());

			}
		}

		for (int i = 0; i < 2; i++) {
			work = new File(System.getProperty("java.io.tmpdir", "."), "Puma/master/20120711/bucket-"
			      + Integer.toString(i));
			work.getParentFile().mkdirs();
			if (work.createNewFile()) {
				System.out.println("create a file: " + work.getAbsolutePath());

			}
		}

		work = new File(System.getProperty("java.io.tmpdir", "."), "Puma/null");
		if (work.mkdirs())
			System.out.println("create a file: " + work.getAbsolutePath());
		masterIndex = new LocalFileDataBucketManager();
		((AbstractDataBucketManager) masterIndex).setBaseDir(System.getProperty("java.io.tmpdir", ".") + "/Puma/master");
		((AbstractDataBucketManager) masterIndex).setBucketFilePrefix("bucket-");
		((AbstractDataBucketManager) masterIndex).setMaxBucketLengthMB(500);

		masterIndex.start();
		List<String> paths = new ArrayList<String>();
		paths.add("20120711/bucket-0");
		paths.add("20120711/bucket-1");
		masterIndex.add(paths);

		masterNullIndex = new LocalFileDataBucketManager();
		((AbstractDataBucketManager) masterNullIndex)
		      .setBaseDir(System.getProperty("java.io.tmpdir", ".") + "/Puma/null");
		((AbstractDataBucketManager) masterNullIndex).setBucketFilePrefix("bucket-");
		((AbstractDataBucketManager) masterNullIndex).setMaxBucketLengthMB(500);
		masterNullIndex.start();

		slaveIndex = new LocalFileDataBucketManager();
		((AbstractDataBucketManager) slaveIndex).setBaseDir(System.getProperty("java.io.tmpdir", ".") + "/Puma/slave");
		((AbstractDataBucketManager) slaveIndex).setBucketFilePrefix("bucket-");
		((AbstractDataBucketManager) slaveIndex).setMaxBucketLengthMB(500);

		slaveIndex.start();
		List<String> slavepaths = new ArrayList<String>();
		slavepaths.add("20120710/bucket-0");
		slavepaths.add("20120710/bucket-1");
		slaveIndex.add(slavepaths);

		slaveNullIndex = new LocalFileDataBucketManager();
		((AbstractDataBucketManager) slaveNullIndex).setBaseDir(System.getProperty("java.io.tmpdir", ".") + "/Puma/null");
		((AbstractDataBucketManager) slaveNullIndex).setBucketFilePrefix("bucket-");
		((AbstractDataBucketManager) slaveNullIndex).setMaxBucketLengthMB(500);
		slaveNullIndex.start();

	}

	@Test
	public void testGetReadBucket() throws StorageClosedException, IOException {
		bucketManager = new DefaultBucketManager(masterIndex, slaveIndex, archiveStrategy, cleanupStrategy);
		bucketManager.start();

		DataBucket bucket = bucketManager.getReadBucket(-1, true);
		Assert.assertEquals(120710, bucket.getStartingSequece().getCreationDate());
		Assert.assertEquals(0, bucket.getStartingSequece().getNumber());
		bucket.stop();

		Sequence sequence = new Sequence(120710, 0, 0, 0);
		bucket = bucketManager.getReadBucket(sequence.longValue(), true);
		Assert.assertEquals(120710, bucket.getStartingSequece().getCreationDate());
		Assert.assertEquals(0, bucket.getStartingSequece().getNumber());
		bucket.stop();

		sequence = new Sequence(120711, 1, 0, 0);
		bucket = bucketManager.getReadBucket(sequence.longValue(), true);
		Assert.assertEquals(120711, bucket.getStartingSequece().getCreationDate());
		Assert.assertEquals(1, bucket.getStartingSequece().getNumber());
		bucket.stop();

		sequence = new Sequence(120712, 0);

		try {
			bucket = bucketManager.getReadBucket(sequence.longValue(), true);
			Assert.fail();
		} catch (IOException e) {

		}

		bucketManager = new DefaultBucketManager(masterNullIndex, slaveIndex, archiveStrategy, cleanupStrategy);
		bucket = null;
		bucketManager.start();
		bucket = bucketManager.getReadBucket(-1, true);
		Assert.assertEquals(120710, bucket.getStartingSequece().getCreationDate());
		Assert.assertEquals(0, bucket.getStartingSequece().getNumber());
		bucket.stop();

		sequence = new Sequence(120710, 0, 0, 0);
		bucket = bucketManager.getReadBucket(sequence.longValue(), true);
		Assert.assertEquals(120710, bucket.getStartingSequece().getCreationDate());
		Assert.assertEquals(0, bucket.getStartingSequece().getNumber());
		bucket.stop();

		sequence = new Sequence(120711, 1, 0, 0);

		try {
			bucket = bucketManager.getReadBucket(sequence.longValue(), true);
			Assert.fail();
		} catch (IOException e) {

		}

		sequence = new Sequence(120712, 0);

		try {
			bucket = bucketManager.getReadBucket(sequence.longValue(), true);
			Assert.fail();
		} catch (IOException e) {

		}

		bucketManager = new DefaultBucketManager(masterIndex, slaveNullIndex, archiveStrategy, cleanupStrategy);
		bucketManager.start();
		bucket = bucketManager.getReadBucket(-1, true);
		Assert.assertEquals(120711, bucket.getStartingSequece().getCreationDate());
		Assert.assertEquals(0, bucket.getStartingSequece().getNumber());
		bucket.stop();

		sequence = new Sequence(120710, 0, 0, 0);
		try {
			bucket = bucketManager.getReadBucket(sequence.longValue(), true);
			Assert.fail();
		} catch (IOException e) {

		}

		sequence = new Sequence(120711, 1, 0, 0);
		bucket = bucketManager.getReadBucket(sequence.longValue(), true);
		Assert.assertEquals(120711, bucket.getStartingSequece().getCreationDate());
		Assert.assertEquals(1, bucket.getStartingSequece().getNumber());
		bucket.stop();

		sequence = new Sequence(120712, 0);

		try {
			bucket = bucketManager.getReadBucket(sequence.longValue(), true);
			Assert.fail();
		} catch (IOException e) {

		}

		bucketManager = new DefaultBucketManager(masterNullIndex, slaveNullIndex, archiveStrategy, cleanupStrategy);
		bucketManager.start();
		try {
			bucket = bucketManager.getReadBucket(-1, true);
		} catch (IOException e) {
		}

	}

	@Test
	public void testGetNextReadBucket() throws StorageClosedException, IOException {
		bucketManager = new DefaultBucketManager(masterIndex, slaveIndex, archiveStrategy, cleanupStrategy);
		bucketManager.start();

		Sequence sequence = new Sequence(120710, 0, 0, 0);
		DataBucket bucket = bucketManager.getNextReadBucket(sequence.longValue());
		Assert.assertEquals(120710, bucket.getStartingSequece().getCreationDate());
		Assert.assertEquals(1, bucket.getStartingSequece().getNumber());
		bucket.stop();

		sequence = new Sequence(120711, 0, 0, 0);
		bucket = bucketManager.getNextReadBucket(sequence.longValue());
		Assert.assertEquals(120711, bucket.getStartingSequece().getCreationDate());
		Assert.assertEquals(1, bucket.getStartingSequece().getNumber());
		bucket.stop();

		sequence = new Sequence(120711, 1);

		try {
			bucket = bucketManager.getNextReadBucket(sequence.longValue());
			Assert.fail();
		} catch (IOException e) {

		}

		bucketManager = new DefaultBucketManager(masterNullIndex, slaveIndex, archiveStrategy, cleanupStrategy);
		bucket = null;

		bucketManager.start();
		sequence = new Sequence(120710, 0, 0, 0);
		bucket = bucketManager.getNextReadBucket(sequence.longValue());
		Assert.assertEquals(120710, bucket.getStartingSequece().getCreationDate());
		Assert.assertEquals(1, bucket.getStartingSequece().getNumber());
		bucket.stop();

		sequence = new Sequence(120711, 1, 0, 0);

		try {
			bucket = bucketManager.getNextReadBucket(sequence.longValue());
			Assert.fail();
		} catch (IOException e) {

		}

		sequence = new Sequence(120712, 0);

		try {
			bucket = bucketManager.getNextReadBucket(sequence.longValue());
			Assert.fail();
		} catch (IOException e) {

		}

		bucketManager = new DefaultBucketManager(masterIndex, slaveNullIndex, archiveStrategy, cleanupStrategy);
		sequence = new Sequence(120710, 0, 0, 0);
		bucketManager.start();
		bucket = bucketManager.getNextReadBucket(sequence.longValue());
		Assert.assertEquals(120711, bucket.getStartingSequece().getCreationDate());
		Assert.assertEquals(0, bucket.getStartingSequece().getNumber());
		bucket.stop();

		sequence = new Sequence(120711, 0, 0, 0);
		bucket = bucketManager.getNextReadBucket(sequence.longValue());
		Assert.assertEquals(120711, bucket.getStartingSequece().getCreationDate());
		Assert.assertEquals(1, bucket.getStartingSequece().getNumber());
		bucket.stop();

		sequence = new Sequence(120711, 1);

		try {
			bucket = bucketManager.getNextReadBucket(sequence.longValue());
			Assert.fail();
		} catch (IOException e) {

		}

		bucketManager = new DefaultBucketManager(masterNullIndex, slaveNullIndex, archiveStrategy, cleanupStrategy);
		sequence = new Sequence(120711, 1);
		bucket = null;
		try {
			bucketManager.start();
			bucket = bucketManager.getNextReadBucket(sequence.longValue());
			Assert.fail();
		} catch (IOException e) {

		}

	}

	@Test
	public void testGetNextWriteBucket() throws StorageClosedException, IOException {
		bucketManager = new DefaultBucketManager(masterIndex, slaveIndex, archiveStrategy, cleanupStrategy);
		bucketManager.start();

		// TDODO
		DataBucket bucket = bucketManager.getNextWriteBucket();
		SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd");
		Assert.assertEquals(new Sequence(Integer.valueOf(sdf.format(new Date())), 0).getCreationDate(), bucket
		      .getStartingSequece().getCreationDate());
		Assert.assertEquals(new Sequence(Integer.valueOf(sdf.format(new Date())), 0).getNumber(), bucket
		      .getStartingSequece().getNumber());
		Assert.assertEquals(3, masterIndex.size());
		Assert.assertEquals(2, slaveIndex.size());
		bucket.stop();

		List<String> paths = new ArrayList<String>();
		Sequence sequence = new Sequence(Integer.valueOf(sdf.format(new Date())), 0);
		String path = "20" + String.valueOf(sequence.getCreationDate()) + "/bucket-0";
		paths.add(path);
		masterIndex.remove(paths);
	}

	@Test
	public void testHasNexReadBucket() throws StorageClosedException, IOException {
		bucketManager = new DefaultBucketManager(masterIndex, slaveIndex, archiveStrategy, cleanupStrategy);
		bucketManager.start();

		Sequence sequence = new Sequence(120710, 0, 0, 0);
		Assert.assertTrue(bucketManager.hasNexReadBucket(sequence.longValue()));

		sequence = new Sequence(120711, 0, 0, 0);
		Assert.assertTrue(bucketManager.hasNexReadBucket(sequence.longValue()));

		sequence = new Sequence(120711, 1);

		Assert.assertTrue(!bucketManager.hasNexReadBucket(sequence.longValue()));

		bucketManager = new DefaultBucketManager(masterNullIndex, slaveIndex, archiveStrategy, cleanupStrategy);
		bucketManager.start();
		sequence = new Sequence(120710, 0, 0, 0);
		Assert.assertTrue(bucketManager.hasNexReadBucket(sequence.longValue()));

		sequence = new Sequence(120711, 1, 0, 0);

		Assert.assertTrue(!bucketManager.hasNexReadBucket(sequence.longValue()));

		sequence = new Sequence(120712, 0);

		Assert.assertTrue(!bucketManager.hasNexReadBucket(sequence.longValue()));

		bucketManager = new DefaultBucketManager(masterIndex, slaveNullIndex, archiveStrategy, cleanupStrategy);
		bucketManager.start();
		sequence = new Sequence(120711, 0, 0, 0);
		Assert.assertTrue(bucketManager.hasNexReadBucket(sequence.longValue()));

		sequence = new Sequence(120711, 1);

		Assert.assertTrue(!bucketManager.hasNexReadBucket(sequence.longValue()));

		bucketManager = new DefaultBucketManager(masterNullIndex, slaveNullIndex, archiveStrategy, cleanupStrategy);
		bucketManager.start();
		sequence = new Sequence(120711, 1);
		Assert.assertTrue(!bucketManager.hasNexReadBucket(sequence.longValue()));
	}

	@Test
	public void testClose() {
		bucketManager = new DefaultBucketManager(masterIndex, slaveIndex, archiveStrategy, cleanupStrategy);
		bucketManager.stop();
		Sequence seq = new Sequence(120711, 1);
		try {
			bucketManager.hasNexReadBucket(seq.longValue());
			Assert.fail();
		} catch (StorageClosedException e) {
		} catch (IOException e) {
			Assert.fail();
		}
	}

	@Test
	public void testUpdateLatestSequence() {
		bucketManager = new DefaultBucketManager(masterIndex, slaveIndex, archiveStrategy, cleanupStrategy);
		bucketManager.updateLatestSequence(new Sequence(120712, 0));

	}

	@After
	public void after() throws IOException {
		masterIndex.stop();
		masterNullIndex.stop();
		slaveIndex.stop();
		slaveNullIndex.stop();
		bucketManager.stop();
		work = new File(System.getProperty("java.io.tmpdir", "."), "Puma");
		FileUtils.deleteDirectory(work);
	}
}
