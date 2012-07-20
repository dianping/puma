package com.dianping.puma.storage;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.security.SecurityUtil;
import org.apache.hadoop.security.UserGroupInformation;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class HDFSBucketTest {

	protected HDFSBucket	hdfsBucket;

	@Before
	public void before() throws IOException {
		// { Configuration hdfsConfig = new Configuration();
		// hdfsConfig.set("hadoop.security.authentication", "kerberos");
		// hdfsConfig.set("dfs.namenode.kerberos.principal",
		// "hadoop/test86.hadoop@DIANPING.COM");
		// hdfsConfig.set("test.hadoop.principal", "workcron@DIANPING.COM");
		// hdfsConfig.set("test.hadoop.keytab.file", "E:/.keytab");
		// hdfsConfig.set("fs.default.name", "hdfs://test86.hadoop/");
		// hdfsConfig.setInt("io.file.buffer.size", 1048576);
		//
		// SecurityUtil.login(hdfsConfig, "test.hadoop.keytab.file",
		// "test.hadoop.principal" );
		// FileSystem fileSystem = FileSystem.get(hdfsConfig);
		// Path file =new Path("/tmp/Puma/7-43/20120710/bucket-0");
		// fileSystem.mkdirs(file.getParent());
		// fileSystem.createNewFile(file);
		//		
		//		
		// Sequence sequence = new Sequence(120710, 0, 0);
		//		
		// HDFSBucket hdfsBucket= new HDFSBucket(fileSystem,"/tmp/",
		// "Puma/7-43/20120710/bucket-0", sequence);

		Configuration conf = new Configuration();
		conf.set("hadoop.security.authentication", "kerberos");
		conf.set("dfs.namenode.kerberos.principal", "hadoop/test86.hadoop@DIANPING.COM");
		conf.set("test.hadoop.principal", "workcron@DIANPING.COM");
		conf.set("test.hadoop.keytab.file", "E://.keytab");
		conf.set("fs.default.name", "hdfs://test86.hadoop/");
		UserGroupInformation.setConfiguration(conf);
		SecurityUtil.login(conf, "test.hadoop.keytab.file", "test.hadoop.principal");

		Path file = new Path("/tmp/dfs_write");
		FileSystem fs = FileSystem.get(conf);
		fs.getConf().setInt("io.file.buffer.size", 1048576);
		FSDataOutputStream f = fs.create(file);
		if ( f != null)
			System.out.println("succeed");
		f.close();
		fs.delete(file, false);
		fs.close();
	}

	@Test
	public void testGetStartingSequece() {

	}

	@After
	public void after() {

	}

}
