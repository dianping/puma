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

import com.dianping.puma.core.codec.JsonEventCodec;
import com.dianping.puma.storage.exception.StorageClosedException;

public class DefaultBucketManagerTest {

	public DefaultBucketManager	bucketManager;
	protected File				work	= null;
	public BucketIndex			masterIndex;
	public BucketIndex			masterNullIndex;
	public BucketIndex			slaveIndex;
	public BucketIndex			slaveNullIndex;
	public ArchiveStrategy		archiveStrategy;
	public CleanupStrategy		cleanupStrategy;
	public BinlogIndexManager	binlogIndexManager;

	@Before
	public void before() throws Exception {

		for (int i = 0; i < 2; i++) {
			work = new File(System.getProperty("java.io.tmpdir", "."), "Puma/slave/20120710/bucket-"
					+ Integer.toString(i));
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
		masterIndex = new LocalFileBucketIndex();
		((AbstractBucketIndex) masterIndex).setBaseDir(System.getProperty("java.io.tmpdir", ".") + "/Puma/master");
		((AbstractBucketIndex) masterIndex).setBucketFilePrefix("bucket-");
		((AbstractBucketIndex) masterIndex).setMaxBucketLengthMB(500);

		masterIndex.start();
		List<String> paths = new ArrayList<String>();
		paths.add("20120711/bucket-0");
		paths.add("20120711/bucket-1");
		masterIndex.add(paths);

		masterNullIndex = new LocalFileBucketIndex();
		((AbstractBucketIndex) masterNullIndex).setBaseDir(System.getProperty("java.io.tmpdir", ".") + "/Puma/null");
		((AbstractBucketIndex) masterNullIndex).setBucketFilePrefix("bucket-");
		((AbstractBucketIndex) masterNullIndex).setMaxBucketLengthMB(500);
		masterNullIndex.start();

		slaveIndex = new LocalFileBucketIndex();
		((AbstractBucketIndex) slaveIndex).setBaseDir(System.getProperty("java.io.tmpdir", ".") + "/Puma/slave");
		((AbstractBucketIndex) slaveIndex).setBucketFilePrefix("bucket-");
		((AbstractBucketIndex) slaveIndex).setMaxBucketLengthMB(500);

		slaveIndex.start();
		List<String> slavepaths = new ArrayList<String>();
		slavepaths.add("20120710/bucket-0");
		slavepaths.add("20120710/bucket-1");
		slaveIndex.add(slavepaths);

		slaveNullIndex = new LocalFileBucketIndex();
		((AbstractBucketIndex) slaveNullIndex).setBaseDir(System.getProperty("java.io.tmpdir", ".") + "/Puma/null");
		((AbstractBucketIndex) slaveNullIndex).setBucketFilePrefix("bucket-");
		((AbstractBucketIndex) slaveNullIndex).setMaxBucketLengthMB(500);
		slaveNullIndex.start();
		
		binlogIndexManager = new BinlogIndexManager();
		binlogIndexManager.setMainbinlogIndexFileName("binlogIndex");
		binlogIndexManager.setMainbinlogIndexFileNameBasedir(System.getProperty("java.io.tmpdir", ".") + "/Puma");
		binlogIndexManager.setSubBinlogIndexBaseDir(System.getProperty("java.io.tmpdir", ".") + "/binlogindex");
		binlogIndexManager.setSubBinlogIndexPrefix("index-");
		binlogIndexManager.setBucketFilePrefix("bucket-");
		binlogIndexManager.setCodec(new JsonEventCodec());

	}

	@Test
	public void testGetReadBucket() throws StorageClosedException, IOException {
		bucketManager = new DefaultBucketManager(masterIndex, slaveIndex, binlogIndexManager, archiveStrategy, cleanupStrategy);
		bucketManager.start();

		Bucket bucket = bucketManager.getReadBucket(-1);
		Assert.assertEquals(120710, bucket.getStartingSequece().getCreationDate());
		Assert.assertEquals(0, bucket.getStartingSequece().getNumber());
		bucket.stop();

		Sequence sequence = new Sequence(120710, 0, 0);
		bucket = bucketManager.getReadBucket(sequence.longValue());
		Assert.assertEquals(120710, bucket.getStartingSequece().getCreationDate());
		Assert.assertEquals(0, bucket.getStartingSequece().getNumber());
		bucket.stop();

		sequence = new Sequence(120711, 1, 0);
		bucket = bucketManager.getReadBucket(sequence.longValue());
		Assert.assertEquals(120711, bucket.getStartingSequece().getCreationDate());
		Assert.assertEquals(1, bucket.getStartingSequece().getNumber());
		bucket.stop();

		sequence = new Sequence(120712, 0);

		try {
			bucket = bucketManager.getReadBucket(sequence.longValue());
			Assert.fail();
		} catch (IOException e) {

		}

		bucketManager = new DefaultBucketManager(masterNullIndex, slaveIndex, binlogIndexManager, archiveStrategy, cleanupStrategy);
		bucket = null;
		bucketManager.start();
		bucket = bucketManager.getReadBucket(-1);
		Assert.assertEquals(120710, bucket.getStartingSequece().getCreationDate());
		Assert.assertEquals(0, bucket.getStartingSequece().getNumber());
		bucket.stop();

		sequence = new Sequence(120710, 0, 0);
		bucket = bucketManager.getReadBucket(sequence.longValue());
		Assert.assertEquals(120710, bucket.getStartingSequece().getCreationDate());
		Assert.assertEquals(0, bucket.getStartingSequece().getNumber());
		bucket.stop();

		sequence = new Sequence(120711, 1, 0);

		try {
			bucket = bucketManager.getReadBucket(sequence.longValue());
			Assert.fail();
		} catch (IOException e) {

		}

		sequence = new Sequence(120712, 0);

		try {
			bucket = bucketManager.getReadBucket(sequence.longValue());
			Assert.fail();
		} catch (IOException e) {

		}

		bucketManager = new DefaultBucketManager(masterIndex, slaveNullIndex, binlogIndexManager, archiveStrategy, cleanupStrategy);
		bucketManager.start();
		bucket = bucketManager.getReadBucket(-1);
		Assert.assertEquals(120711, bucket.getStartingSequece().getCreationDate());
		Assert.assertEquals(0, bucket.getStartingSequece().getNumber());
		bucket.stop();

		sequence = new Sequence(120710, 0, 0);
		try {
			bucket = bucketManager.getReadBucket(sequence.longValue());
			Assert.fail();
		} catch (IOException e) {

		}

		sequence = new Sequence(120711, 1, 0);
		bucket = bucketManager.getReadBucket(sequence.longValue());
		Assert.assertEquals(120711, bucket.getStartingSequece().getCreationDate());
		Assert.assertEquals(1, bucket.getStartingSequece().getNumber());
		bucket.stop();

		sequence = new Sequence(120712, 0);

		try {
			bucket = bucketManager.getReadBucket(sequence.longValue());
			Assert.fail();
		} catch (IOException e) {

		}

		bucketManager = new DefaultBucketManager(masterNullIndex, slaveNullIndex, binlogIndexManager, archiveStrategy, cleanupStrategy);
		bucketManager.start();
		try {
			bucket = bucketManager.getReadBucket(-1);
		} catch (IOException e) {
		}

	}

	@Test
	public void testGetNextReadBucket() throws StorageClosedException, IOException {
		bucketManager = new DefaultBucketManager(masterIndex, slaveIndex, binlogIndexManager, archiveStrategy, cleanupStrategy);
		bucketManager.start();

		Sequence sequence = new Sequence(120710, 0, 0);
		Bucket bucket = bucketManager.getNextReadBucket(sequence.longValue());
		Assert.assertEquals(120710, bucket.getStartingSequece().getCreationDate());
		Assert.assertEquals(1, bucket.getStartingSequece().getNumber());
		bucket.stop();

		sequence = new Sequence(120711, 0, 0);
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

		bucketManager = new DefaultBucketManager(masterNullIndex, slaveIndex, binlogIndexManager, archiveStrategy, cleanupStrategy);
		bucket = null;

		bucketManager.start();
		sequence = new Sequence(120710, 0, 0);
		bucket = bucketManager.getNextReadBucket(sequence.longValue());
		Assert.assertEquals(120710, bucket.getStartingSequece().getCreationDate());
		Assert.assertEquals(1, bucket.getStartingSequece().getNumber());
		bucket.stop();

		sequence = new Sequence(120711, 1, 0);

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

		bucketManager = new DefaultBucketManager(masterIndex, slaveNullIndex, binlogIndexManager, archiveStrategy, cleanupStrategy);
		sequence = new Sequence(120710, 0, 0);
		bucketManager.start();
		bucket = bucketManager.getNextReadBucket(sequence.longValue());
		Assert.assertEquals(120711, bucket.getStartingSequece().getCreationDate());
		Assert.assertEquals(0, bucket.getStartingSequece().getNumber());
		bucket.stop();

		sequence = new Sequence(120711, 0, 0);
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

		bucketManager = new DefaultBucketManager(masterNullIndex, slaveNullIndex, binlogIndexManager, archiveStrategy, cleanupStrategy);
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
		bucketManager = new DefaultBucketManager(masterIndex, slaveIndex, binlogIndexManager, archiveStrategy, cleanupStrategy);
		bucketManager.start();

		// TDODO
		Bucket bucket = bucketManager.getNextWriteBucket();
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
		bucketManager = new DefaultBucketManager(masterIndex, slaveIndex, binlogIndexManager, archiveStrategy, cleanupStrategy);
		bucketManager.start();

		Sequence sequence = new Sequence(120710, 0, 0);
		Assert.assertTrue(bucketManager.hasNexReadBucket(sequence.longValue()));

		sequence = new Sequence(120711, 0, 0);
		Assert.assertTrue(bucketManager.hasNexReadBucket(sequence.longValue()));

		sequence = new Sequence(120711, 1);

		Assert.assertTrue(!bucketManager.hasNexReadBucket(sequence.longValue()));

		bucketManager = new DefaultBucketManager(masterNullIndex, slaveIndex, binlogIndexManager, archiveStrategy, cleanupStrategy);
		bucketManager.start();
		sequence = new Sequence(120710, 0, 0);
		Assert.assertTrue(bucketManager.hasNexReadBucket(sequence.longValue()));

		sequence = new Sequence(120711, 1, 0);

		Assert.assertTrue(!bucketManager.hasNexReadBucket(sequence.longValue()));

		sequence = new Sequence(120712, 0);

		Assert.assertTrue(!bucketManager.hasNexReadBucket(sequence.longValue()));

		bucketManager = new DefaultBucketManager(masterIndex, slaveNullIndex, binlogIndexManager, archiveStrategy, cleanupStrategy);
		bucketManager.start();
		sequence = new Sequence(120711, 0, 0);
		Assert.assertTrue(bucketManager.hasNexReadBucket(sequence.longValue()));

		sequence = new Sequence(120711, 1);

		Assert.assertTrue(!bucketManager.hasNexReadBucket(sequence.longValue()));

		bucketManager = new DefaultBucketManager(masterNullIndex, slaveNullIndex, binlogIndexManager, archiveStrategy, cleanupStrategy);
		bucketManager.start();
		sequence = new Sequence(120711, 1);
		Assert.assertTrue(!bucketManager.hasNexReadBucket(sequence.longValue()));
	}

	@Test
	public void testClose() {
		bucketManager = new DefaultBucketManager(masterIndex, slaveIndex, binlogIndexManager, archiveStrategy, cleanupStrategy);
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
		bucketManager = new DefaultBucketManager(masterIndex, slaveIndex, binlogIndexManager, archiveStrategy, cleanupStrategy);
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
