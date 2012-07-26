package com.dianping.puma.storage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.dianping.puma.core.codec.JsonEventCodec;
import com.dianping.puma.core.event.DdlEvent;
import com.dianping.puma.core.util.ByteArrayUtils;
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
			e1.printStackTrace();
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
			e1.printStackTrace();
		}

		Sequence sequence = new Sequence(120711, 0);

		Bucket bucket = null;
		try {
			bucket = new LocalFileBucket(work, sequence, 10);
			localBucketIndex.add(bucket);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
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
	
//	@Test
//	public void testAddBucketList()
//	{
//		this.localBucketIndex.init();
//		
//		try {
//
//			for (int i = 0; i < 2; i++) {
//				work = new File(System.getProperty("java.io.tmpdir", "."), "Puma/20120711/bucket-"
//						+ Integer.toString(i));
//				work.getParentFile().mkdirs();
//				if (work.createNewFile()) {
//					System.out.println("create a file: " + work.getName());
//
//				}
//			}
//		} catch (IOException e1) {
//			System.out.println("failed to create file");
//		}
//		
//		List<String> paths= new ArrayList<String>();
//		paths.add("20120711/bucket-0");
//		
//		this.localBucketIndex.add(paths);
//	}

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
			e1.printStackTrace();
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
			e1.printStackTrace();
		}
		this.localBucketIndex.init();

		Sequence seq = new Sequence(120710, 0);
		try {
			Bucket bucket = this.localBucketIndex.getNextReadBucket(seq);
			Assert.assertEquals(120710, bucket.getStartingSequece().getCreationDate());
			Assert.assertEquals(1, bucket.getStartingSequece().getNumber());
			bucket.close();

			seq = new Sequence(120710, 1);

			bucket = this.localBucketIndex.getNextReadBucket(seq);
			Assert.assertEquals(120711, bucket.getStartingSequece().getCreationDate());
			Assert.assertEquals(0, bucket.getStartingSequece().getNumber());

			bucket.close();

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
			e1.printStackTrace();
		}

		this.localBucketIndex.init();

		try {
			Bucket bucket = this.localBucketIndex.getNextWriteBucket();

			SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd");

			Assert.assertEquals(new Sequence(Integer.valueOf(sdf.format(new Date())), 0).getCreationDate(), bucket
					.getStartingSequece().getCreationDate());
			Assert.assertEquals(new Sequence(Integer.valueOf(sdf.format(new Date())), 0).getNumber(), bucket
					.getStartingSequece().getNumber());
			this.localBucketIndex.add(bucket);

			Bucket bucket2 = this.localBucketIndex.getNextWriteBucket();

			Assert.assertEquals(new Sequence(Integer.valueOf(sdf.format(new Date())), 0).getCreationDate(), bucket2
					.getStartingSequece().getCreationDate());
			Assert.assertEquals(1, bucket2.getStartingSequece().getNumber());

			bucket.close();
			bucket2.close();

		} catch (StorageClosedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@Test
	public void testHasNexReadBucket() {
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
			e1.printStackTrace();
		}
		this.localBucketIndex.init();

		try {
			Sequence seq = new Sequence(120710, 0);

			Assert.assertTrue(this.localBucketIndex.hasNexReadBucket(seq));

			seq = new Sequence(120710, 1);
			Assert.assertTrue(this.localBucketIndex.hasNexReadBucket(seq));

			seq = new Sequence(120711, 3);
			Assert.assertFalse(this.localBucketIndex.hasNexReadBucket(seq));
		} catch (StorageClosedException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testGetReadBucket() {
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
			e1.printStackTrace();
		}
		this.localBucketIndex.init();
		try {
			Bucket bucket = this.localBucketIndex.getReadBucket(-1);
			Assert.assertEquals(120710, bucket.getStartingSequece().getCreationDate());
			Assert.assertEquals(0, bucket.getStartingSequece().getNumber());
			bucket.close();

			bucket = this.localBucketIndex.getReadBucket(-2);
			Assert.assertEquals(null, bucket);

			Sequence seq = new Sequence(120711, 3);
			this.localBucketIndex.updateLatestSequence(seq);
			bucket = this.localBucketIndex.getReadBucket(-2);
			Assert.assertEquals(120711, bucket.getStartingSequece().getCreationDate());
			Assert.assertEquals(3, bucket.getStartingSequece().getNumber());
			bucket.close();

			DdlEvent event = new DdlEvent();
			event.setSql("CREATE TABLE products (proeduct VARCHAR(10))");
			event.setDatabase("cat");
			event.setExecuteTime(0);
			event.setSeq(seq.longValue());
			event.setTable(null);

			JsonEventCodec codec = new JsonEventCodec();
			byte[] data = null;
			try {
				data = codec.encode(event);
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				bos.write(ByteArrayUtils.intToByteArray(data.length));
				bos.write(data);

				RandomAccessFile file = new RandomAccessFile(new File(System.getProperty("java.io.tmpdir", "."),
						"Puma/20120711/bucket-3"), "rw");
				file.write(bos.toByteArray());
				bos.close();
				file.close();

				bucket = this.localBucketIndex.getReadBucket(seq.longValue());
				Assert.assertEquals(120711, bucket.getStartingSequece().getCreationDate());
				Assert.assertEquals(3, bucket.getStartingSequece().getNumber());
				bucket.close();

			} catch (IOException e) {
				e.printStackTrace();
			}

			bucket = this.localBucketIndex.getReadBucket(seq.longValue());
			Assert.assertEquals(120711, bucket.getStartingSequece().getCreationDate());
			Assert.assertEquals(3, bucket.getStartingSequece().getNumber());
			bucket.close();

		} catch (StorageClosedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@Test
	public void testSize() {
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
			e1.printStackTrace();
		}
		this.localBucketIndex.init();
		Assert.assertEquals(6, this.localBucketIndex.size());
	}

	@Test
	public void testClose() {
		this.localBucketIndex.init();
		this.localBucketIndex.close();

		Assert.assertTrue(this.localBucketIndex.stop);
	}

	@Test
	public void testGetBaseDir() {
		this.localBucketIndex.init();
		Assert.assertEquals((System.getProperty("java.io.tmpdir", ".").toString() + "Puma"), this.localBucketIndex
				.getBaseDir());

	}

	@Test
	public void testCopyFromLocal() {
		work = new File(System.getProperty("java.io.tmpdir", "."), "Puma/copy/20120710/bucket-0");
		work.getParentFile().mkdirs();
		try {
			if (work.createNewFile())
				System.out.println("create a file!");

			work = new File(System.getProperty("java.io.tmpdir", "."), "Puma/copy/20120710/bucket-1");

			if (work.createNewFile()) {
				System.out.println("create a file!");

			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		this.localBucketIndex.init();
		try {
			this.localBucketIndex.copyFromLocal(System.getProperty("java.io.tmpdir", ".").toString() + "Puma/copy",
					"20120710/bucket-0");
		} catch (StorageClosedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		Assert.assertEquals(2, this.localBucketIndex.index.get().size());

		work = new File(System.getProperty("java.io.tmpdir", "."), "Puma/copy/20120713/bucket-0");
		work.getParentFile().mkdirs();
		try {
			if (work.createNewFile())
				System.out.println("create a file!");

		} catch (IOException e1) {
			e1.printStackTrace();
		}

		this.localBucketIndex.init();
		try {
			this.localBucketIndex.copyFromLocal(System.getProperty("java.io.tmpdir", ".").toString() + "Puma/copy",
					"20120713/bucket-0");
		} catch (StorageClosedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		localBucketIndex.init();
		

		Assert.assertEquals(3, this.localBucketIndex.index.get().size());
		Assert.assertEquals(120713, this.localBucketIndex.index.get().lastKey().getCreationDate());
		Assert.assertEquals(0, this.localBucketIndex.index.get().lastKey().getNumber());

		work = new File(System.getProperty("java.io.tmpdir", "."), "Puma/copy");

		for (File file : work.listFiles()) {
			for (File files : file.listFiles()) {
				if (files.isFile())
					files.delete();
			}
			file.delete();
		}
		work.delete();
	}
	@Test
	public void testRemove()
	{
		this.localBucketIndex.init();
		List<String> paths= new ArrayList<String>();
		paths.add("20120710/bucket-0");
		try {
			this.localBucketIndex.remove(paths);
		} catch (StorageClosedException e) {
			e.printStackTrace();
		}
		Assert.assertEquals(1, this.localBucketIndex.index.get().size());
		Assert.assertEquals(120710,  this.localBucketIndex.index.get().firstKey().getCreationDate());
		Assert.assertEquals(1, this.localBucketIndex.index.get().firstKey().getNumber());
		
		paths= new ArrayList<String>();
		paths.add("20120710/bucket-1");
		try {
			this.localBucketIndex.remove(paths);
		} catch (StorageClosedException e) {
			e.printStackTrace();
		}
		Assert.assertEquals(0, this.localBucketIndex.index.get().size());
	}

	@Test
	public void testUpdateLatestSequence()
	{
		this.localBucketIndex.init();
		Sequence seq = new Sequence(120710, 1);
		this.localBucketIndex.updateLatestSequence(seq);
		Assert.assertEquals(120710, this.localBucketIndex.latestSequence.get().getCreationDate());
		Assert.assertEquals(1, this.localBucketIndex.latestSequence.get().getNumber());
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
