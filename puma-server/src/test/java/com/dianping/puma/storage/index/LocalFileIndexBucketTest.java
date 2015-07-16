package com.dianping.puma.storage.index;

import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.dianping.puma.storage.Sequence;

/**
 * 
 * @author damonzhu
 *
 */
public class LocalFileIndexBucketTest {

	private File file;

	private IndexItemConvertor<L2Index> valueConvertor = new L2IndexItemConvertor();

	@Before
	public void setup() throws IOException {
		File path = new File(System.getProperty("java.io.tmpdir", "."), "puma");
		if (!path.exists()) {
			path.mkdir();
		}

		this.file = new File(System.getProperty("java.io.tmpdir", "."), "puma/20150713-Bucket-15.l2idx");
		this.file.createNewFile();

		DataOutputStream output = new DataOutputStream(new FileOutputStream(this.file));

		for (int i = 0; i < 1000; i++) {
			L2Index l2Index = new L2Index();
			l2Index.setBinlogIndexKey(new BinlogIndexKey("mysql-binlog.0000001", 4L + i, 1L));
			l2Index.setDatabase("dianping");
			l2Index.setTable("receipt");
			l2Index.setDdl(false);
			l2Index.setDml(true);
			l2Index.setSequence(new Sequence(123123L + i,1));
			byte[] convertToObj = (byte[]) valueConvertor.convertToObj(l2Index);

			output.write(convertToObj.length);
			output.write(convertToObj);
		}

		output.close();
	}

	@After
	public void cleanUp() {
		if (this.file.exists()) {
			this.file.delete();
		}
	}

	@Test
	public void testGetNext() throws IOException {
		LocalFileIndexBucket<BinlogIndexKey, L2Index> indexBucket = new LocalFileIndexBucket<BinlogIndexKey, L2Index>(
		      this.file, valueConvertor);
		indexBucket.start();

		int i = 0;
		try {
			for (; i < 1001; i++) {
				L2Index next = indexBucket.next();

				Assert.assertEquals("mysql-binlog.0000001", next.getBinlogIndexKey().getBinlogFile());
				Assert.assertEquals(4L + i, next.getBinlogIndexKey().getBinlogPos());
				Assert.assertEquals(1L, next.getBinlogIndexKey().getServerId());
				Assert.assertEquals("dianping", next.getDatabase());
				Assert.assertEquals("receipt", next.getTable());
				Assert.assertEquals(false, next.isDdl());
				Assert.assertEquals(true, next.isDml());
				Assert.assertEquals(123123L + i, next.getSequence().longValue());
				Assert.assertEquals(1, next.getSequence().getLen());
			}
		} catch (EOFException eof) {
			Assert.assertEquals(i, 1000);
		}
	}

	@Test
	public void testLocate() throws IOException {
		BinlogIndexKey findKey = new BinlogIndexKey("mysql-binlog.0000001", 4L + 10, 1L);

		LocalFileIndexBucket<BinlogIndexKey, L2Index> indexBucket = new LocalFileIndexBucket<BinlogIndexKey, L2Index>(
		      this.file, valueConvertor);
		indexBucket.start();

		indexBucket.locate(findKey);
		
		for (int i = 11; i < 1000; i++) {
			L2Index next = indexBucket.next();

			Assert.assertEquals("mysql-binlog.0000001", next.getBinlogIndexKey().getBinlogFile());
			Assert.assertEquals(4L + i, next.getBinlogIndexKey().getBinlogPos());
			Assert.assertEquals(1L, next.getBinlogIndexKey().getServerId());
			Assert.assertEquals("dianping", next.getDatabase());
			Assert.assertEquals("receipt", next.getTable());
			Assert.assertEquals(false, next.isDdl());
			Assert.assertEquals(true, next.isDml());
			Assert.assertEquals(123123L + i, next.getSequence().longValue());
		}
	}
}
