package com.dianping.puma.storage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.dianping.puma.storage.exception.StorageClosedException;

public class DefaultBucketManagerTest {

	public DefaultBucketManager	bucketManager;
	protected File				work	= null;
	public BucketIndex			masterIndex;
	public BucketIndex			masterNullIndex;
	public BucketIndex			slaveIndex;
	public BucketIndex			slaveNullIndex;
	public ArchiveStrategy		archiveStrategy;

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
			System.out.println("create a file! " + work.getAbsolutePath());
		masterIndex = new LocalFileBucketIndex();
		((AbstractBucketIndex) masterIndex).setBaseDir(System.getProperty("java.io.tmpdir", ".") + "/Puma/master");
		((AbstractBucketIndex) masterIndex).setBucketFilePrefix("bucket-");
		((AbstractBucketIndex) masterIndex).setMaxBucketLengthMB(500);

		masterIndex.init();
		List<String> paths = new ArrayList<String>();
		paths.add("20120711/bucket-0");
		paths.add("20120711/bucket-1");
		masterIndex.add(paths);

		masterNullIndex = new LocalFileBucketIndex();
		((AbstractBucketIndex) masterNullIndex).setBaseDir(System.getProperty("java.io.tmpdir", ".") + "/Puma/null");
		((AbstractBucketIndex) masterNullIndex).setBucketFilePrefix("bucket-");
		((AbstractBucketIndex) masterNullIndex).setMaxBucketLengthMB(500);
		masterNullIndex.init();

		slaveIndex = new LocalFileBucketIndex();
		((AbstractBucketIndex) slaveIndex).setBaseDir(System.getProperty("java.io.tmpdir", ".") + "/Puma/slave");
		((AbstractBucketIndex) slaveIndex).setBucketFilePrefix("bucket-");
		((AbstractBucketIndex) slaveIndex).setMaxBucketLengthMB(500);

		slaveIndex.init();
		List<String> slavepaths = new ArrayList<String>();
		slavepaths.add("20120710/bucket-0");
		slavepaths.add("20120710/bucket-1");
		slaveIndex.add(slavepaths);

		slaveNullIndex = new LocalFileBucketIndex();
		((AbstractBucketIndex) slaveNullIndex).setBaseDir(System.getProperty("java.io.tmpdir", ".") + "/Puma/null");
		((AbstractBucketIndex) slaveNullIndex).setBucketFilePrefix("bucket-");
		((AbstractBucketIndex) slaveNullIndex).setMaxBucketLengthMB(500);
		slaveNullIndex.init();

	}

	@Test
	public void testGetReadBucket() throws StorageClosedException, IOException {
		bucketManager = new DefaultBucketManager(500, masterIndex, slaveIndex, archiveStrategy);

		Bucket bucket = bucketManager.getReadBucket(-1);
		Assert.assertEquals(120710, bucket.getStartingSequece().getCreationDate());
		Assert.assertEquals(0, bucket.getStartingSequece().getNumber());
		bucket.close();

		Sequence sequence = new Sequence(120710, 0, 0);
		bucket = bucketManager.getReadBucket(sequence.longValue());
		Assert.assertEquals(120710, bucket.getStartingSequece().getCreationDate());
		Assert.assertEquals(0, bucket.getStartingSequece().getNumber());
		bucket.close();

		sequence = new Sequence(120711, 1, 0);
		bucket = bucketManager.getReadBucket(sequence.longValue());
		Assert.assertEquals(120711, bucket.getStartingSequece().getCreationDate());
		Assert.assertEquals(1, bucket.getStartingSequece().getNumber());
		bucket.close();

		sequence = new Sequence(120712, 0);

		try {
			bucket = bucketManager.getReadBucket(sequence.longValue());
			Assert.fail();
		} catch (IOException e) {

		}
		
		bucketManager = new DefaultBucketManager(500, masterNullIndex, slaveIndex, archiveStrategy);
		bucket=null;
		bucket = bucketManager.getReadBucket(-1);
		Assert.assertEquals(120710, bucket.getStartingSequece().getCreationDate());
		Assert.assertEquals(0, bucket.getStartingSequece().getNumber());
		bucket.close();

		sequence = new Sequence(120710, 0, 0);
		bucket = bucketManager.getReadBucket(sequence.longValue());
		Assert.assertEquals(120710, bucket.getStartingSequece().getCreationDate());
		Assert.assertEquals(0, bucket.getStartingSequece().getNumber());
		bucket.close();

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

	}

	@After
	public void after() throws IOException {
		masterIndex.close();
		masterNullIndex.close();
		slaveIndex.close();
		slaveNullIndex.close();
		bucketManager.close();
		work = new File(System.getProperty("java.io.tmpdir", "."), "Puma");
		FileUtils.deleteDirectory(work);
	}

}