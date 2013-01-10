package com.dianping.puma.storage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
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
		System.out.println("************************************************************");
		System.out.println("**************************Before****************************");
		System.out.println("************************************************************");

		work = new File(System.getProperty("java.io.tmpdir", "."), "Puma/20120710/bucket-0");
		work.getParentFile().mkdirs();
		try {
			if (work.createNewFile())
				System.out.println("create a file! " + work.getAbsolutePath());

			work = new File(System.getProperty("java.io.tmpdir", "."), "Puma/20120710/bucket-1");

			if (work.createNewFile()) {
				System.out.println("create a file! " + work.getAbsolutePath());

			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		localBucketIndex.setBaseDir(System.getProperty("java.io.tmpdir", ".") + "/Puma");
		localBucketIndex.setBucketFilePrefix("bucket-");
		localBucketIndex.setMaxBucketLengthMB(500);

		System.out.println("*************************************************************");
		System.out.println("****************************End******************************");
		System.out.println("*************************************************************");
	}

	@Test
	public void testInit() throws Exception {

		System.out.println("************************************************************");
		System.out.println("***************************Init*****************************");
		System.out.println("************************************************************");
		localBucketIndex.start();

		System.out.println("*************************");
		System.out.println(localBucketIndex.getIndex().get());
		System.out.println("*************************");

		Assert.assertEquals("20120710/bucket-0", localBucketIndex.getIndex().get().get(new Sequence(120710, 0)));
		Assert.assertEquals("20120710/bucket-1", localBucketIndex.getIndex().get().get(new Sequence(120710, 1)));

		System.out.println("*************************************************************");
		System.out.println("****************************End******************************");
		System.out.println("*************************************************************");
	}

	@Test
	public void testAddBucket() throws Exception {
		System.out.println("*************************************************************");
		System.out.println("***********************testAddBucket*************************");
		System.out.println("*************************************************************");
		localBucketIndex.start();

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
			bucket = new LocalFileBucket(work, sequence, 10, "20120711/bucket-0");
			localBucketIndex.add(bucket);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (StorageClosedException e) {
			e.printStackTrace();
		}

		Assert.assertEquals("20120711/bucket-0", localBucketIndex.getIndex().get().get(sequence));
		try {
			bucket.stop();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("*************************************************************");
		System.out.println("****************************End******************************");
		System.out.println("*************************************************************");
	}

	@Test
	public void testAddBucketList() throws Exception {
		System.out.println("*************************************************************");
		System.out.println("*********************testAddBucketList***********************");
		System.out.println("*************************************************************");

		this.localBucketIndex.start();

		try {

			for (int i = 0; i < 2; i++) {
				work = new File(System.getProperty("java.io.tmpdir", "."), "Puma/20120711/bucket-"
						+ Integer.toString(i));
				work.getParentFile().mkdirs();
				if (work.createNewFile()) {
					System.out.println("create a file: " + work.getAbsolutePath());

				}
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		List<String> paths = new ArrayList<String>();
		paths.add("20120711/bucket-0");
		paths.add("20120711/bucket-1");

		try {
			this.localBucketIndex.add(paths);
		} catch (StorageClosedException e) {
			e.printStackTrace();
		}

		Assert.assertEquals(4, this.localBucketIndex.getIndex().get().size());
		Assert.assertEquals(120710, this.localBucketIndex.getIndex().get().firstEntry().getKey().getCreationDate());
		Assert.assertEquals(0, this.localBucketIndex.getIndex().get().firstEntry().getKey().getNumber());

		Assert.assertEquals(120711, this.localBucketIndex.getIndex().get().lastEntry().getKey().getCreationDate());
		Assert.assertEquals(1, this.localBucketIndex.getIndex().get().lastEntry().getKey().getNumber());

		System.out.println("*************************************************************");
		System.out.println("****************************End******************************");
		System.out.println("*************************************************************");
	}

	@Test
	public void testBulkGetRemainN() throws Exception {
		System.out.println("************************************************************");
		System.out.println("********************testBulkGetRemainN**********************");
		System.out.println("************************************************************");
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

		this.localBucketIndex.start();
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
		System.out.println("*************************************************************");
		System.out.println("****************************End******************************");
		System.out.println("*************************************************************");
	}

	@Test
	public void testBulkGetRemainNDay() throws Exception {
		System.out.println("***********************************************************");
		System.out.println("******************testBulkGetRemainNDay********************");
		System.out.println("***********************************************************");
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		List<String> folders = new ArrayList<String>();
		try {
			for (int i = 0; i <= 10; i++) {
				cal.add(Calendar.DAY_OF_MONTH, i == 0 ? 0 : -1);
				String folder = sdf.format(cal.getTime());
				folders.add(folder);
				work = new File(System.getProperty("java.io.tmpdir", "."), "Puma/" + folder + "/bucket-0");
				work.getParentFile().mkdirs();
				if (work.createNewFile()) {
					System.out.println("create a file: " + work.getAbsolutePath());

				}
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		this.localBucketIndex.start();
		try {

			int num = 4;
			List<String> results = this.localBucketIndex.bulkGetRemainNDay(num);
			Assert.assertEquals(13 - num, results.size());
			Assert.assertEquals("20120710/bucket-0", results.get(0));
			Assert.assertEquals("20120710/bucket-1", results.get(1));
			for (int i = 2; i < results.size(); i++) {
				Assert.assertEquals(folders.get(folders.size() - (i - 2) - 1) + "/bucket-0", results.get(i));
			}

			num = 10;
			results = this.localBucketIndex.bulkGetRemainNDay(num);
			Assert.assertEquals(13 - num, results.size());
			Assert.assertEquals("20120710/bucket-0", results.get(0));
			Assert.assertEquals("20120710/bucket-1", results.get(1));
			for (int i = 2; i < results.size(); i++) {
				Assert.assertEquals(folders.get(folders.size() - (i - 2) - 1) + "/bucket-0", results.get(i));
			}

		} catch (StorageClosedException e) {
			e.printStackTrace();
		}
		System.out.println("*************************************************************");
		System.out.println("****************************End******************************");
		System.out.println("*************************************************************");
	}

	@Test
	public void testGetNextReadBucket() throws Exception {
		System.out.println("*************************************************************");
		System.out.println("*******************testGetNextReadBucket*********************");
		System.out.println("*************************************************************");
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
		this.localBucketIndex.start();

		Sequence seq = new Sequence(120710, 0);
		try {
			Bucket bucket = this.localBucketIndex.getNextReadBucket(seq);
			Assert.assertEquals(120710, bucket.getStartingSequece().getCreationDate());
			Assert.assertEquals(1, bucket.getStartingSequece().getNumber());
			bucket.stop();

			seq = new Sequence(120710, 1);

			bucket = this.localBucketIndex.getNextReadBucket(seq);
			Assert.assertEquals(120711, bucket.getStartingSequece().getCreationDate());
			Assert.assertEquals(0, bucket.getStartingSequece().getNumber());

			bucket.stop();

			seq = new Sequence(120711, 3);
			bucket = this.localBucketIndex.getNextReadBucket(seq);
			Assert.assertEquals(null, bucket);

		} catch (StorageClosedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("*************************************************************");
		System.out.println("****************************End******************************");
		System.out.println("*************************************************************");
	}

	@Test
	public void testGetNextWriteBucket() throws Exception {
		System.out.println("**************************************************************");
		System.out.println("*******************testGetNextWriteBucket*********************");
		System.out.println("**************************************************************");
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

		this.localBucketIndex.start();

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

			bucket.stop();
			bucket2.stop();

		} catch (StorageClosedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("*************************************************************");
		System.out.println("****************************End******************************");
		System.out.println("*************************************************************");
	}

	@Test
	public void testHasNexReadBucket() throws Exception {
		System.out.println("**************************************************************");
		System.out.println("********************testHasNexReadBucket**********************");
		System.out.println("**************************************************************");
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
		this.localBucketIndex.start();

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
		System.out.println("*************************************************************");
		System.out.println("****************************End******************************");
		System.out.println("*************************************************************");
	}

	@Test
	public void testGetReadBucketEmpty() throws Exception {
		work = new File(System.getProperty("java.io.tmpdir", "."), "Puma");

		try {
			FileUtils.deleteDirectory(work);
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		this.localBucketIndex.start();

		try {
			Bucket bucket = this.localBucketIndex.getReadBucket(-1, true);

			Assert.assertEquals(null, bucket);
			bucket = this.localBucketIndex.getReadBucket(-2, true);
			Assert.assertEquals(null, bucket);

			Sequence seq = new Sequence(120710, 0);
			bucket = this.localBucketIndex.getReadBucket(seq.longValue(), true);
			Assert.assertEquals(null, bucket);

		} catch (StorageClosedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@Test
	public void testGetReadBucket() throws Exception {
		System.out.println("*************************************************************");
		System.out.println("*********************testGetReadBucket***********************");
		System.out.println("*************************************************************");
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
		this.localBucketIndex.start();
		try {
			Bucket bucket = this.localBucketIndex.getReadBucket(-1, true);
			Assert.assertEquals(120710, bucket.getStartingSequece().getCreationDate());
			Assert.assertEquals(0, bucket.getStartingSequece().getNumber());
			bucket.stop();

			bucket = this.localBucketIndex.getReadBucket(-2, true);
			Assert.assertEquals(null, bucket);

			Sequence seq = new Sequence(120711, 3);
			this.localBucketIndex.updateLatestSequence(seq);
			bucket = this.localBucketIndex.getReadBucket(-2, true);
			Assert.assertEquals(120711, bucket.getStartingSequece().getCreationDate());
			Assert.assertEquals(3, bucket.getStartingSequece().getNumber());
			bucket.stop();

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

				bucket = this.localBucketIndex.getReadBucket(seq.longValue(), true);
				Assert.assertEquals(120711, bucket.getStartingSequece().getCreationDate());
				Assert.assertEquals(3, bucket.getStartingSequece().getNumber());
				bucket.stop();

			} catch (IOException e) {
				e.printStackTrace();
			}

			bucket = this.localBucketIndex.getReadBucket(seq.longValue(), true);
			Assert.assertEquals(120711, bucket.getStartingSequece().getCreationDate());
			Assert.assertEquals(3, bucket.getStartingSequece().getNumber());
			bucket.stop();

		} catch (StorageClosedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("*************************************************************");
		System.out.println("****************************End******************************");
		System.out.println("*************************************************************");
	}

	@Test
	public void testSize() throws Exception {
		System.out.println("************************************************************");
		System.out.println("*************************testSize***************************");
		System.out.println("************************************************************");
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
		this.localBucketIndex.start();
		Assert.assertEquals(6, this.localBucketIndex.size());
		System.out.println("*************************************************************");
		System.out.println("****************************End******************************");
		System.out.println("*************************************************************");
	}

	@Test
	public void testClose() throws Exception {
		System.out.println("*************************************************************");
		System.out.println("*************************testClose***************************");
		System.out.println("*************************************************************");
		this.localBucketIndex.start();
		this.localBucketIndex.stop();

		try {
			this.localBucketIndex.getReadBucket(-1, true);
			Assert.fail();
		} catch (StorageClosedException e) {

		} catch (IOException e) {
			Assert.fail();
		}

		Assert.assertTrue(this.localBucketIndex.isStop());
		System.out.println("*************************************************************");
		System.out.println("****************************End******************************");
		System.out.println("*************************************************************");
	}

	@Test
	public void testGetBaseDir() throws Exception {
		System.out.println("**************************************************************");
		System.out.println("***********************testGetBaseDir*************************");
		System.out.println("**************************************************************");
		this.localBucketIndex.start();
		Assert.assertEquals((System.getProperty("java.io.tmpdir", ".").toString() + "/Puma"),
				this.localBucketIndex.getBaseDir());
		System.out.println("*************************************************************");
		System.out.println("****************************End******************************");
		System.out.println("*************************************************************");

	}

	@Test
	public void testCopyFromLocal() throws Exception {
		System.out.println("***************************************************************");
		System.out.println("**********************testCopyFromLocal************************");
		System.out.println("***************************************************************");
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

		this.localBucketIndex.start();
		try {
			this.localBucketIndex.copyFromLocal(System.getProperty("java.io.tmpdir", ".").toString() + "/Puma/copy",
					"20120710/bucket-0");
		} catch (StorageClosedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		Assert.assertEquals(2, this.localBucketIndex.getIndex().get().size());

		work = new File(System.getProperty("java.io.tmpdir", "."), "Puma/copy/20120713/bucket-0");
		work.getParentFile().mkdirs();
		try {
			if (work.createNewFile())
				System.out.println("create a file!");

		} catch (IOException e1) {
			e1.printStackTrace();
		}

		this.localBucketIndex.start();
		try {
			this.localBucketIndex.copyFromLocal(System.getProperty("java.io.tmpdir", ".") + "/Puma/copy",
					"20120713/bucket-0");
		} catch (StorageClosedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		localBucketIndex.start();

		Assert.assertEquals(3, this.localBucketIndex.getIndex().get().size());
		Assert.assertEquals(120713, this.localBucketIndex.getIndex().get().lastKey().getCreationDate());
		Assert.assertEquals(0, this.localBucketIndex.getIndex().get().lastKey().getNumber());

		work = new File(System.getProperty("java.io.tmpdir", "."), "Puma/copy");

		for (File file : work.listFiles()) {
			for (File files : file.listFiles()) {
				if (files.isFile())
					files.delete();
			}
			file.delete();
		}
		work.delete();

		System.out.println("*************************************************************");
		System.out.println("****************************End******************************");
		System.out.println("*************************************************************");
	}

	@Test
	public void testRemoveBucket() throws Exception {
		System.out.println("**********************************************************");
		System.out.println("********************testRemoveBucket**********************");
		System.out.println("**********************************************************");
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		List<String> folders = new ArrayList<String>();
		try {
			for (int i = 0; i <= 10; i++) {
				cal.add(Calendar.DAY_OF_MONTH, i == 0 ? 0 : -1);
				String folder = sdf.format(cal.getTime());
				folders.add(folder);
				work = new File(System.getProperty("java.io.tmpdir", "."), "Puma/" + folder + "/bucket-0");
				work.getParentFile().mkdirs();
				if (work.createNewFile()) {
					System.out.println("create a file: " + work.getAbsolutePath());

				}
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		this.localBucketIndex.start();
		try {

			int num = 4;
			List<String> results = this.localBucketIndex.bulkGetRemainNDay(num);
			Assert.assertEquals(13 - num, results.size());
			Assert.assertEquals("20120710/bucket-0", results.get(0));
			Assert.assertEquals("20120710/bucket-1", results.get(1));
			for (int i = 2; i < results.size(); i++) {
				Assert.assertEquals(folders.get(folders.size() - (i - 2) - 1) + "/bucket-0", results.get(i));
			}

			for (String path : results) {
				this.localBucketIndex.removeBucket(path);
			}

			Assert.assertFalse(new File(System.getProperty("java.io.tmpdir", "."), "Puma/20120710/bucket-0").exists());
			Assert.assertFalse(new File(System.getProperty("java.io.tmpdir", "."), "Puma/20120710/bucket-1").exists());
			for (int i = 2; i < results.size(); i++) {
				Assert.assertFalse(new File(System.getProperty("java.io.tmpdir", "."), results.get(i) + "/bucket-0")
						.exists());
			}

			Assert.assertFalse(new File(System.getProperty("java.io.tmpdir", "."), "Puma/20120710/").exists());
			for (int i = 2; i < results.size(); i++) {
				Assert.assertFalse(new File(System.getProperty("java.io.tmpdir", "."), results.get(i)).exists());
			}

		} catch (StorageClosedException e) {
			e.printStackTrace();
		}
		System.out.println("*************************************************************");
		System.out.println("****************************End******************************");
		System.out.println("*************************************************************");
	}

	@Test
	public void testRemove() throws Exception {
		System.out.println("**************************************************************");
		System.out.println("*************************testRemove***************************");
		System.out.println("**************************************************************");
		this.localBucketIndex.start();
		List<String> paths = new ArrayList<String>();
		paths.add("20120710/bucket-0");
		try {
			this.localBucketIndex.remove(paths);
		} catch (StorageClosedException e) {
			e.printStackTrace();
		}
		Assert.assertEquals(1, this.localBucketIndex.getIndex().get().size());
		Assert.assertEquals(120710, this.localBucketIndex.getIndex().get().firstKey().getCreationDate());
		Assert.assertEquals(1, this.localBucketIndex.getIndex().get().firstKey().getNumber());

		paths = new ArrayList<String>();
		paths.add("20120710/bucket-1");
		try {
			this.localBucketIndex.remove(paths);
		} catch (StorageClosedException e) {
			e.printStackTrace();
		}
		Assert.assertEquals(0, this.localBucketIndex.getIndex().get().size());
		System.out.println("*************************************************************");
		System.out.println("****************************End******************************");
		System.out.println("*************************************************************");
	}

	@Test
	public void testUpdateLatestSequence() throws Exception {
		System.out.println("**************************************************************");
		System.out.println("******************testUpdateLatestSequence********************");
		System.out.println("**************************************************************");
		this.localBucketIndex.start();
		Sequence seq = new Sequence(120710, 1);
		this.localBucketIndex.updateLatestSequence(seq);
		Assert.assertEquals(120710, this.localBucketIndex.getLatestSequence().get().getCreationDate());
		Assert.assertEquals(1, this.localBucketIndex.getLatestSequence().get().getNumber());
		System.out.println("*************************************************************");
		System.out.println("****************************End******************************");
		System.out.println("*************************************************************");
	}

	@After
	public void after() {
		System.out.println("*************************************************************");
		System.out.println("***************************after*****************************");
		System.out.println("*************************************************************");
		localBucketIndex.stop();

		work = new File(System.getProperty("java.io.tmpdir", "."), "Puma");

		try {
			FileUtils.deleteDirectory(work);
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("*************************************************************");
		System.out.println("****************************End******************************");
		System.out.println("*************************************************************");
	}
}
