package com.dianping.puma.storage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.security.SecurityUtil;
import org.apache.hadoop.security.UserGroupInformation;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.dianping.puma.core.codec.JsonEventCodec;
import com.dianping.puma.core.event.DdlEvent;
import com.dianping.puma.core.util.ByteArrayUtils;
import com.dianping.puma.storage.exception.StorageClosedException;

public class HDFSBucketIndexTest {

	protected File					work			= null;
	protected HDFSBucketIndex		hdfsBucketIndex	= new HDFSBucketIndex();
	protected FileSystem			fileSystem;
	protected FSDataOutputStream	fsoutput;

	@Before
	public void before() throws IOException {
		Configuration hdfsConfig = new Configuration();
		hdfsConfig.set("hadoop.security.authentication", "kerberos");
		hdfsConfig.set("dfs.namenode.kerberos.principal", "hadoop/test86.hadoop@DIANPING.COM");
		hdfsConfig.set("test.hadoop.principal", "workcron@DIANPING.COM");
		hdfsConfig.set("test.hadoop.keytab.file", "/etc/.keytab");
		hdfsConfig.set("fs.default.name", "hdfs://test86.hadoop/");
		hdfsConfig.setInt("io.file.buffer.size", 1048576);

		UserGroupInformation.setConfiguration(hdfsConfig);
		SecurityUtil.login(hdfsConfig, "test.hadoop.keytab.file", "test.hadoop.principal");
		fileSystem = FileSystem.get(hdfsConfig);
		Path file = new Path("/tmp/Puma/20120710/bucket-0");
		fsoutput = fileSystem.create(file);
		if (fsoutput != null) {
			System.out.println("Succed to create a file " + file.getParent().getName() + " \\ " + file.getName());
		}
		fsoutput.close();
		file = new Path("/tmp/Puma/20120710/bucket-1");
		fsoutput = fileSystem.create(file);
		if (fsoutput != null) {
			System.out.println("Succed to create a file " + file.getParent().getName() + " \\ " + file.getName());
		}
		fsoutput.close();

		this.hdfsBucketIndex.setBaseDir("/tmp/Puma");
		this.hdfsBucketIndex.setBucketFilePrefix("bucket-");
		this.hdfsBucketIndex.setMaxBucketLengthMB(500);
	}

	@Test
	public void testInit() {

		try {
			this.hdfsBucketIndex.init();
		} catch (Exception e) {
			e.printStackTrace();
		}

		Assert.assertEquals(2, this.hdfsBucketIndex.index.get().size());
		Assert.assertEquals("20120710/bucket-0", hdfsBucketIndex.index.get().get(new Sequence(120710, 0)));
		Assert.assertEquals("20120710/bucket-1", hdfsBucketIndex.index.get().get(new Sequence(120710, 1)));
	}

	@Test
	public void testAddBucket() {

		try {
			this.hdfsBucketIndex.init();
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		Path file = new Path("/tmp/Puma/20120711/bucket-0");
		try {
			fsoutput = fileSystem.create(file);
			if (fsoutput != null) {
				System.out.println("Succed to create a file " + file.getParent().getName() + " \\ " + file.getName());
			}
			fsoutput.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		Sequence sequence = new Sequence(120711, 0);

		Bucket bucket = null;
		try {
			bucket = new HDFSBucket(fileSystem, "/tmp", "Puma/20120711/bucket-0", sequence);
			hdfsBucketIndex.add(bucket);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (StorageClosedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		Assert.assertEquals("20120711/bucket-0", hdfsBucketIndex.index.get().get(sequence));
		try {
			bucket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testAddBucketList() {

		try {
			this.hdfsBucketIndex.init();
		} catch (Exception e2) {
			e2.printStackTrace();
		}

		for (int i = 0; i < 2; i++) {
			Path file = new Path("/tmp/Puma/20120711/bucket-" + Integer.toString(i));
			try {
				fsoutput = fileSystem.create(file);
				if (fsoutput != null) {
					System.out.println("Succed to create a file " + file.getParent().getName() + " \\ "
							+ file.getName());
				}
				fsoutput.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}

		}

		List<String> paths = new ArrayList<String>();
		paths.add("20120711/bucket-0");
		paths.add("20120711/bucket-1");

		try {
			this.hdfsBucketIndex.add(paths);
		} catch (StorageClosedException e) {
			e.printStackTrace();
		}

		Assert.assertEquals(4, this.hdfsBucketIndex.index.get().size());
		Assert.assertEquals(120710, this.hdfsBucketIndex.index.get().firstEntry().getKey().getCreationDate());
		Assert.assertEquals(0, this.hdfsBucketIndex.index.get().firstEntry().getKey().getNumber());

		Assert.assertEquals(120711, this.hdfsBucketIndex.index.get().lastEntry().getKey().getCreationDate());
		Assert.assertEquals(1, this.hdfsBucketIndex.index.get().lastEntry().getKey().getNumber());

	}

	@Test
	public void testBulkGetRemainN() {

		for (int i = 0; i < 4; i++) {
			Path file = new Path("/tmp/Puma/20120711/bucket-" + Integer.toString(i));
			try {
				fsoutput = fileSystem.create(file);
				if (fsoutput != null) {
					System.out.println("Succed to create a file " + file.getParent().getName() + " \\ "
							+ file.getName());
				}
				fsoutput.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}

		}

		try {
			this.hdfsBucketIndex.init();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		try {

			int num = 4;
			List<String> results = this.hdfsBucketIndex.bulkGetRemainN(num);
			Assert.assertEquals(6 - num, results.size());
			Assert.assertEquals("20120710/bucket-0", results.get(0));
			Assert.assertEquals("20120710/bucket-1", results.get(1));

			num = 3;
			results = this.hdfsBucketIndex.bulkGetRemainN(num);
			Assert.assertEquals(6 - num, results.size());
			Assert.assertEquals("20120710/bucket-0", results.get(0));
			Assert.assertEquals("20120710/bucket-1", results.get(1));
			Assert.assertEquals("20120711/bucket-0", results.get(2));

			num = 7;
			results = this.hdfsBucketIndex.bulkGetRemainN(num);
			Assert.assertEquals(0, results.size());

		} catch (StorageClosedException e) {
			e.printStackTrace();
		}

	}

	@Test
	public void testGetNextReadBucket() {

		for (int i = 0; i < 4; i++) {
			Path file = new Path("/tmp/Puma/20120711/bucket-" + Integer.toString(i));
			try {
				fsoutput = fileSystem.create(file);
				if (fsoutput != null) {
					System.out.println("Succed to create a file " + file.getParent().getName() + " \\ "
							+ file.getName());
				}
				fsoutput.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}

		}
		try {
			this.hdfsBucketIndex.init();
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		Sequence seq = new Sequence(120710, 0);
		try {
			Bucket bucket = this.hdfsBucketIndex.getNextReadBucket(seq);
			Assert.assertEquals(120710, bucket.getStartingSequece().getCreationDate());
			Assert.assertEquals(1, bucket.getStartingSequece().getNumber());
			bucket.close();

			seq = new Sequence(120710, 1);

			bucket = this.hdfsBucketIndex.getNextReadBucket(seq);
			Assert.assertEquals(120711, bucket.getStartingSequece().getCreationDate());
			Assert.assertEquals(0, bucket.getStartingSequece().getNumber());

			bucket.close();

			seq = new Sequence(120711, 3);
			bucket = this.hdfsBucketIndex.getNextReadBucket(seq);
			Assert.assertEquals(null, bucket);

		} catch (StorageClosedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@Test
	public void testGetNextWriteBucket() {
		for (int i = 0; i < 4; i++) {
			Path file = new Path("/tmp/Puma/20120711/bucket-" + Integer.toString(i));
			try {
				fsoutput = fileSystem.create(file);
				if (fsoutput != null) {
					System.out.println("Succed to create a file " + file.getParent().getName() + " \\ "
							+ file.getName());
				}
				fsoutput.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}

		try {
			this.hdfsBucketIndex.init();
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		try {
			Bucket bucket = this.hdfsBucketIndex.getNextWriteBucket();
			Assert.assertEquals(null, bucket);
			if (bucket != null) {
				bucket.close();
			}

		} catch (StorageClosedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testHasNexReadBucket() {
		for (int i = 0; i < 4; i++) {
			Path file = new Path("/tmp/Puma/20120711/bucket-" + Integer.toString(i));
			try {
				fsoutput = fileSystem.create(file);
				if (fsoutput != null) {
					System.out.println("Succed to create a file " + file.getParent().getName() + " \\ "
							+ file.getName());
				}
				fsoutput.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		try {
			this.hdfsBucketIndex.init();
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		try {
			Sequence seq = new Sequence(120710, 0);

			Assert.assertTrue(this.hdfsBucketIndex.hasNexReadBucket(seq));

			seq = new Sequence(120710, 1);
			Assert.assertTrue(this.hdfsBucketIndex.hasNexReadBucket(seq));

			seq = new Sequence(120711, 3);
			Assert.assertFalse(this.hdfsBucketIndex.hasNexReadBucket(seq));
		} catch (StorageClosedException e) {
			e.printStackTrace();
		}
	}

	public void testGetReadBucketEmpty() {
		try {
			if (this.fileSystem.delete(new Path("/tmp/Puma"), true)) {
				System.out.println("Clear file");
			}
		} catch (IOException e2) {
			e2.printStackTrace();
		}

		try {
			this.hdfsBucketIndex.init();
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		try {
			Bucket bucket = this.hdfsBucketIndex.getReadBucket(-1);

			Assert.assertEquals(null, bucket);
			bucket = this.hdfsBucketIndex.getReadBucket(-2);
			Assert.assertEquals(null, bucket);

			Sequence seq = new Sequence(120710, 0);
			bucket = this.hdfsBucketIndex.getReadBucket(seq.longValue());
			Assert.assertEquals(null, bucket);

		} catch (StorageClosedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@Test
	public void testGetReadBucket() {
		Sequence newSeq = null;

		for (int i = 0; i < 4; i++) {
			Path file = new Path("/tmp/Puma/20120711/bucket-" + Integer.toString(i));
			try {
				fsoutput = fileSystem.create(file);
				if (fsoutput != null) {
					System.out.println("Succed to create a file " + file.getParent().getName() + " \\ "
							+ file.getName());
				}
				fsoutput.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		try {
			this.hdfsBucketIndex.init();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		try {
			Bucket bucket = this.hdfsBucketIndex.getReadBucket(-1);
			Assert.assertEquals(120710, bucket.getStartingSequece().getCreationDate());
			Assert.assertEquals(0, bucket.getStartingSequece().getNumber());
			bucket.close();

			bucket = this.hdfsBucketIndex.getReadBucket(-2);
			Assert.assertEquals(null, bucket);

			Sequence seq = new Sequence(120711, 3);
			this.hdfsBucketIndex.updateLatestSequence(seq);
			bucket = this.hdfsBucketIndex.getReadBucket(-2);
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
				fsoutput.write(ByteArrayUtils.intToByteArray(data.length));
				fsoutput.write(data);
				fsoutput.flush();
				newSeq = seq.addOffset(fsoutput.size());

				event.setSeq(newSeq.longValue());
				data = codec.encode(event);
				fsoutput.write(ByteArrayUtils.intToByteArray(data.length));
				fsoutput.write(data);
				fsoutput.flush();

				fsoutput.close();

				bucket = this.hdfsBucketIndex.getReadBucket(seq.longValue());
				Assert.assertEquals(120711, bucket.getStartingSequece().getCreationDate());
				Assert.assertEquals(3, bucket.getStartingSequece().getNumber());
				bucket.close();

			} catch (IOException e) {
				e.printStackTrace();
			}

			bucket = this.hdfsBucketIndex.getReadBucket(seq.longValue());
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

		for (int i = 0; i < 4; i++) {
			Path file = new Path("/tmp/Puma/20120711/bucket-" + Integer.toString(i));
			try {
				fsoutput = fileSystem.create(file);
				if (fsoutput != null) {
					System.out.println("Succed to create a file " + file.getParent().getName() + " \\ "
							+ file.getName());
				}
				fsoutput.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		try {
			this.hdfsBucketIndex.init();
		} catch (Exception e) {
			e.printStackTrace();
		}
		Assert.assertEquals(6, this.hdfsBucketIndex.size());
	}

	@Test
	public void testClose() {
		try {
			this.hdfsBucketIndex.init();
		} catch (Exception e) {
			e.printStackTrace();
		}
		this.hdfsBucketIndex.close();

		try {
			this.hdfsBucketIndex.getReadBucket(-1);
			Assert.fail();
		} catch (StorageClosedException e) {

		} catch (IOException e) {
			Assert.fail();
		}

		Assert.assertTrue(this.hdfsBucketIndex.stop);
	}

	@Test
	public void testGetBaseDir() {
		try {
			this.hdfsBucketIndex.init();
		} catch (Exception e) {
			e.printStackTrace();
		}
		Assert.assertEquals("/tmp/Puma", this.hdfsBucketIndex.getBaseDir());

	}

	@Test
	public void testCopyFromLocal() throws Exception {
		File work = new File(System.getProperty("java.io.tmpdir", "."), "Puma/copy/20120710/bucket-0");
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

		this.hdfsBucketIndex.init();
		try {
			this.hdfsBucketIndex.copyFromLocal(System.getProperty("java.io.tmpdir", ".") + "/Puma/copy",
					"20120710/bucket-0");
		} catch (StorageClosedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		Assert.assertEquals(2, this.hdfsBucketIndex.index.get().size());

		work = new File(System.getProperty("java.io.tmpdir", "."), "Puma/copy/20120713/bucket-0");
		work.getParentFile().mkdirs();
		try {
			if (work.createNewFile())
				System.out.println("create a file!");

		} catch (IOException e1) {
			e1.printStackTrace();
		}

		this.hdfsBucketIndex.init();
		try {
			this.hdfsBucketIndex.copyFromLocal(System.getProperty("java.io.tmpdir", ".") + "/Puma/copy",
					"20120713/bucket-0");
		} catch (StorageClosedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		hdfsBucketIndex.init();

		Assert.assertEquals(3, this.hdfsBucketIndex.index.get().size());
		Assert.assertEquals(120713, this.hdfsBucketIndex.index.get().lastKey().getCreationDate());
		Assert.assertEquals(0, this.hdfsBucketIndex.index.get().lastKey().getNumber());

		work = new File(System.getProperty("java.io.tmpdir", "."), "Puma/copy");
		FileUtils.deleteDirectory(work);
	}

	@Test
	public void testRemove() throws Exception {
		this.hdfsBucketIndex.init();
		List<String> paths = new ArrayList<String>();
		paths.add("20120710/bucket-0");
		try {
			this.hdfsBucketIndex.remove(paths);
		} catch (StorageClosedException e) {
			e.printStackTrace();
		}
		Assert.assertEquals(1, this.hdfsBucketIndex.index.get().size());
		Assert.assertEquals(120710, this.hdfsBucketIndex.index.get().firstKey().getCreationDate());
		Assert.assertEquals(1, this.hdfsBucketIndex.index.get().firstKey().getNumber());

		paths = new ArrayList<String>();
		paths.add("20120710/bucket-1");
		try {
			this.hdfsBucketIndex.remove(paths);
		} catch (StorageClosedException e) {
			e.printStackTrace();
		}
		Assert.assertEquals(0, this.hdfsBucketIndex.index.get().size());
	}

	@Test
	public void testUpdateLatestSequence() throws Exception {
		this.hdfsBucketIndex.init();
		Sequence seq = new Sequence(120710, 1);
		this.hdfsBucketIndex.updateLatestSequence(seq);
		Assert.assertEquals(120710, this.hdfsBucketIndex.latestSequence.get().getCreationDate());
		Assert.assertEquals(1, this.hdfsBucketIndex.latestSequence.get().getNumber());
	}

	
	@After
	public void after() {
		try {
			this.fsoutput.close();
			if (this.fileSystem.delete(new Path("/tmp/Puma"), true)) {
				System.out.println("Clear file");
			}
			;
			this.fileSystem.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}