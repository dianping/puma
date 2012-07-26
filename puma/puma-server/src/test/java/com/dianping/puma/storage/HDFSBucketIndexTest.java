package com.dianping.puma.storage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

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
		hdfsConfig.set("test.hadoop.keytab.file", "E:/.keytab");
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
				System.out.println("Succed to create a file " + file.getParent().getName()+ " \\ "+file.getName());
			}
			fsoutput.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		

		Sequence sequence = new Sequence(120711, 0);

		Bucket bucket = null;
		try {
			bucket = new HDFSBucket(fileSystem,"/tmp", "Puma/20120711/bucket-0", sequence);
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
