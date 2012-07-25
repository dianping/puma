package com.dianping.puma.storage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.dianping.puma.storage.exception.StorageClosedException;

public class LocalBucketIndexTest {

	protected File					work				= null;
	protected LocalFileBucketIndex	localBucketIndex	= new LocalFileBucketIndex();

	@Before
	public void before() {
		work = new File(System.getProperty("java.io.tmpdir", "."), "Puma/20120710/bucket-0");
		work.getParentFile().mkdirs();
		try {
			if (work.createNewFile())
				System.out.println("create a file!");

			work = new File(System.getProperty("java.io.tmpdir", "."), "Puma/20120710/bucket-1");

			if (work.createNewFile()) {
				System.out.println("create a file!");

			}
		} catch (IOException e1) {
			System.out.println("failed to create file");
		}

		localBucketIndex.setBaseDir(System.getProperty("java.io.tmpdir", ".") + "Puma");
		localBucketIndex.setBucketFilePrefix("bucket-");
		localBucketIndex.setMaxBucketLengthMB(500);

	}

	@Test
	public void testInit() {

		localBucketIndex.init();

		Assert.assertEquals("20120710/bucket-0", localBucketIndex.index.get().get(new Sequence(120710, 0)));
		Assert.assertEquals("20120710/bucket-1", localBucketIndex.index.get().get(new Sequence(120710, 1)));

	}

	@Test
	public void testAddBucket() {

		localBucketIndex.init();

		work = new File(System.getProperty("java.io.tmpdir", "."), "Puma/20120711/bucket-0");
		work.getParentFile().mkdirs();

		try {
			if (work.createNewFile())
				System.out.println("create a file!");
		} catch (IOException e1) {
			System.out.println("failed to create file");
		}

		Sequence sequence = new Sequence(120711, 0);

		Bucket bucket = null;
		try {
			bucket = new LocalFileBucket(work, sequence, 10);
			localBucketIndex.add(bucket);

		} catch (FileNotFoundException e) {
			System.out.println("failed to create localfilebucket");
		} catch (StorageClosedException e) {
			e.printStackTrace();
		}

		Assert.assertEquals("20120711/bucket-0", localBucketIndex.index.get().get(sequence));
		try {
			bucket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@Test
	public void testBulkGetRemainN() {

		try {

			for (int i = 0; i < 4; i++) {
				work = new File(System.getProperty("java.io.tmpdir", "."), "Puma/20120711/bucket-"
						+ Integer.toString(i));
				work.getParentFile().mkdirs();
				if (work.createNewFile()) {
					System.out.println("create a file: " + work.getName());

				}
			}
		} catch (IOException e1) {
			System.out.println("failed to create file");
		}

		this.localBucketIndex.init();
		try {

			int num = 4;
			List<String> results = this.localBucketIndex.bulkGetRemainN(num);
			Assert.assertEquals(6 - num, results.size());
			Assert.assertEquals("20120710/bucket-0", results.get(0));
			Assert.assertEquals("20120710/bucket-1", results.get(1));

			num = 3;
			results = this.localBucketIndex.bulkGetRemainN(num);
			Assert.assertEquals(6 - num, results.size());
			Assert.assertEquals("20120710/bucket-0", results.get(0));
			Assert.assertEquals("20120710/bucket-1", results.get(1));
			Assert.assertEquals("20120711/bucket-0", results.get(2));

			num = 7;
			results = this.localBucketIndex.bulkGetRemainN(num);
			Assert.assertEquals(0, results.size());

		} catch (StorageClosedException e) {
			e.printStackTrace();
		}

	}

	@Test
	public void testGetNextReadBucket() {
		try {

			for (int i = 0; i < 4; i++) {
				work = new File(System.getProperty("java.io.tmpdir", "."), "Puma/20120711/bucket-"
						+ Integer.toString(i));
				work.getParentFile().mkdirs();
				if (work.createNewFile()) {
					System.out.println("create a file: " + work.getName());

				}
			}
		} catch (IOException e1) {
			System.out.println("failed to create file");
		}
		this.localBucketIndex.init();

		Sequence seq = new Sequence(120710, 0);
		try {
			Bucket bucket = this.localBucketIndex.getNextReadBucket(seq);
			Assert.assertEquals(120710, bucket.getStartingSequece().getCreationDate());
			Assert.assertEquals(1, bucket.getStartingSequece().getNumber());

			seq = new Sequence(120710, 1);

			bucket = this.localBucketIndex.getNextReadBucket(seq);
			Assert.assertEquals(120711, bucket.getStartingSequece().getCreationDate());
			Assert.assertEquals(0, bucket.getStartingSequece().getNumber());

			seq = new Sequence(120711, 3);
			bucket = this.localBucketIndex.getNextReadBucket(seq);
			Assert.assertEquals(null, bucket);

		} catch (StorageClosedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testGetNextWriteBucket() {
		try {

			for (int i = 0; i < 4; i++) {
				work = new File(System.getProperty("java.io.tmpdir", "."), "Puma/20120711/bucket-"
						+ Integer.toString(i));
				work.getParentFile().mkdirs();
				if (work.createNewFile()) {
					System.out.println("create a file: " + work.getName());

				}
			}
		} catch (IOException e1) {
			System.out.println("failed to create file");
		}
		
		this.localBucketIndex.init();
		
		
		try {
			Bucket bucket = this.localBucketIndex.getNextWriteBucket();

			Assert.assertEquals(new Sequence(getNowCreationDate(), 0).getCreationDate(), bucket.getStartingSequece().getCreationDate());
			Assert.assertEquals(3, bucket.getStartingSequece().getNumber());

		} catch (StorageClosedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@After
	public void after() {
		localBucketIndex.close();
		work = new File(System.getProperty("java.io.tmpdir", "."), "Puma");

		for (File file : work.listFiles()) {
			for (File files : file.listFiles()) {
				if (files.isFile())
					files.delete();
			}
			file.delete();
		}
		work.delete();
	}
}
