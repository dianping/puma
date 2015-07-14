package com.dianping.puma.storage;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import junit.framework.Assert;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.security.SecurityUtil;
import org.apache.hadoop.security.UserGroupInformation;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.dianping.puma.core.codec.JsonEventCodec;
import com.dianping.puma.core.event.DdlEvent;
import com.dianping.puma.core.util.ByteArrayUtils;
import com.dianping.puma.storage.exception.StorageClosedException;

public class HDFSBucketTest {

	protected HDFSDataBucket			hdfsBucket;
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
		Path file = new Path("/tmp/Puma/7-43/20120710/bucket-0");
		fsoutput = fileSystem.create(file);
		if (fsoutput != null) {
			System.out.println("Succeed to create a file");
		}

	}

	@Test
	public void testGetStartingSequece() {
		Sequence sequence = new Sequence(120710, 0, 0);

		try {
			hdfsBucket = new HDFSDataBucket(fileSystem, "/tmp", "Puma/7-43/20120710/bucket-0", sequence, false);
		} catch (IOException e) {
			e.printStackTrace();
		}

		Sequence seq = hdfsBucket.getStartingSequece();

		Assert.assertEquals(0, seq.getNumber());
		Assert.assertEquals(0, seq.getOffset());
		Assert.assertEquals(120710, seq.getCreationDate());
	}

	@Test
	public void testAppend() {
		boolean flag = false;
		try {
			Sequence sequence = new Sequence(120710, 0, 0);

			hdfsBucket = new HDFSDataBucket(fileSystem, "/tmp", "Puma/7-43/20120710/bucket-0", sequence, false);
			this.hdfsBucket.append(null);
		} catch (StorageClosedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (UnsupportedOperationException e) {
			flag = true;
		}
		Assert.assertTrue(flag);
	}

	@Test
	public void testGettingCurrentWritingSequence() {
		boolean flag = false;
		try {
			Sequence sequence = new Sequence(120710, 0, 0);

			try {
				hdfsBucket = new HDFSDataBucket(fileSystem, "/tmp", "Puma/7-43/20120710/bucket-0", sequence, false);
			} catch (IOException e) {
				e.printStackTrace();
			}
			this.hdfsBucket.getCurrentWritingSeq();
		} catch (UnsupportedOperationException e) {
			flag = true;
		}
		Assert.assertTrue(flag);
	}

	@Test
	public void testGetNext() {
		Sequence sequence = new Sequence(120710, 0, 0);
		DdlEvent event = new DdlEvent();
		event.setSql("CREATE TABLE products (proeduct VARCHAR(10))");
		event.setDatabase("cat");
		event.setExecuteTime(0);
		event.setSeq(sequence.longValue());
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
			fsoutput.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

		ByteArrayOutputStream os = new ByteArrayOutputStream();
		ObjectMapper om = new ObjectMapper();

		try {
			os.write(0);
			os.write(om.writeValueAsBytes(event));
			os.flush();
			byte[] datas = os.toByteArray();
			os.close();

			try {

				hdfsBucket = new HDFSDataBucket(fileSystem, "/tmp", "Puma/7-43/20120710/bucket-0", sequence, false);
				assertequalByteArray(datas, this.hdfsBucket.getNext());
			} catch (StorageClosedException e) {
				e.printStackTrace();
			}

		} catch (JsonGenerationException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@Test
	public void testSeek() {
		boolean thrown = false;
		Sequence newSeq = null;

		Sequence seq = new Sequence(120710, 0, 0);
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

			try {
				hdfsBucket = new HDFSDataBucket(fileSystem, "/tmp", "Puma/7-43/20120710/bucket-0", seq, false);
				this.hdfsBucket.skip(newSeq.getOffset());

				try {
					ByteArrayOutputStream os = new ByteArrayOutputStream();

					ObjectMapper om = new ObjectMapper();

					os.write(0);
					os.write(om.writeValueAsBytes(event));
					os.flush();
					byte[] datas = os.toByteArray();
					os.close();

					assertequalByteArray(datas, this.hdfsBucket.getNext());

				} catch (StorageClosedException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			} catch (StorageClosedException e) {
				e.printStackTrace();
			}

		} catch (IOException e) {
			e.printStackTrace();
		}


	}

	@Test
	public void testClose() {
		Sequence sequence = new Sequence(120710, 0, 0);
		HDFSDataBucket bucket = null;
		try {
			bucket = new HDFSDataBucket(fileSystem, "/tmp", "Puma/7-43/20120710/bucket-0", sequence, false);
			bucket.stop();

		} catch (FileNotFoundException e) {
			System.out.println("failed to create localfilebucket");
		} catch (IOException e) {
			e.printStackTrace();
		}

		boolean thrown = false;
		try {
			bucket.checkClosed();
		} catch (StorageClosedException e) {
			thrown = true;
		}

		Assert.assertTrue(thrown);
	}

	@Test
	public void testDoHasRemainingForWrite() {

		Sequence sequence = new Sequence(120710, 0, 0);

		try {
			hdfsBucket = new HDFSDataBucket(fileSystem, "/tmp", "Puma/7-43/20120710/bucket-0", sequence, false);
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			this.hdfsBucket.doHasRemainingForWrite();
			Assert.fail();
		} catch (IOException e) {
			e.printStackTrace();
			Assert.fail();
		} catch (UnsupportedOperationException e) {
		}
	}

	protected void assertequalByteArray(byte[] expectedValue, byte[] actualValue) {
		Assert.assertEquals(expectedValue.length, actualValue.length);
		for (int i = 0; i < expectedValue.length; i++) {
			Assert.assertEquals(expectedValue[i], actualValue[i]);
		}

	}

	@After
	public void after() {

		try {
			fsoutput.close();

			this.fileSystem.delete(new Path("/tmp/Puma"), true);
			if (hdfsBucket != null) {
				this.hdfsBucket.stop();
			}
			this.fileSystem.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
