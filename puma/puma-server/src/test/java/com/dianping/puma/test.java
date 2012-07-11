package com.dianping.puma;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.security.SecurityUtil;
import org.apache.hadoop.security.UserGroupInformation;

public class test
{
	public static void main(String[] args) throws IOException {
		Configuration conf = new Configuration();
		conf.set("hadoop.security.authentication", "kerberos");
		conf.set("dfs.namenode.kerberos.principal", "hadoop/test86.hadoop@DIANPING.COM");
		conf.set("test.hadoop.principal", "workcron@DIANPING.COM");
		conf.set("test.hadoop.keytab.file", "C:/Users/zhiying.lin/Desktop/.keytab");
		conf.set("fs.default.name", "hdfs://test86.hadoop/");
		UserGroupInformation.setConfiguration(conf);
		SecurityUtil.login(conf, "test.hadoop.keytab.file", "test.hadoop.principal");
		
		Path file = new Path("/tmp/dfs_write");
		FileSystem fs = FileSystem.get(conf);
		fs.getConf().setInt("io.file.buffer.size", 1048576);
		FSDataOutputStream out = fs.create(file);
		out.writeBytes("test dfs write");

		out.close();
		
	}
	
}