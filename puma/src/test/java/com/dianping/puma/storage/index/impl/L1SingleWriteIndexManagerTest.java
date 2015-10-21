package com.dianping.puma.storage.index.impl;

import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.storage.Sequence;
import com.dianping.puma.storage.StorageBaseTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import static org.junit.Assert.*;

public class L1SingleWriteIndexManagerTest extends StorageBaseTest {

	L1SingleWriteIndexManager l1SingleWriteIndexManager;

	File bucket;

	@Before
	public void setUp() throws IOException {
		super.setUp();

		bucket = new File(testDir, "bucket");
		createFile(bucket);

		l1SingleWriteIndexManager = new L1SingleWriteIndexManager(bucket.getAbsolutePath());
		l1SingleWriteIndexManager.start();
	}

	@Test
	public void testEncode() throws Exception {
		L1IndexKey l1IndexKey0 = new L1IndexKey(
				new BinlogInfo().setTimestamp(1).setServerId(2).setBinlogFile("mysql-bin.3").setBinlogPosition(4)
						.setEventIndex(0));
		L1IndexValue l1IndexValue0 = new L1IndexValue(new Sequence(5, 6));
		String string0 = "1!2!mysql-bin.3!4=5-Bucket-6";
		assertArrayEquals(string0.getBytes(), l1SingleWriteIndexManager.encode(l1IndexKey0, l1IndexValue0));

		L1IndexKey l1IndexKey1 = new L1IndexKey(
				new BinlogInfo().setTimestamp(2).setServerId(3).setBinlogFile("mysql-bin.4").setBinlogPosition(5)
						.setEventIndex(0));
		L1IndexValue l1IndexValue1 = new L1IndexValue(new Sequence(6, 7));
		String string1 = "2!3!mysql-bin.4!5=6-Bucket-7";
		assertArrayEquals(string1.getBytes(), l1SingleWriteIndexManager.encode(l1IndexKey1, l1IndexValue1));
	}

	@Test
	public void testAppendAndFlush() throws Exception {
		BufferedReader bufferedReader = new BufferedReader(new FileReader(bucket));

		L1IndexKey l1IndexKey0 = new L1IndexKey(
				new BinlogInfo().setTimestamp(1).setServerId(2).setBinlogFile("mysql-bin.3").setBinlogPosition(4)
						.setEventIndex(0));
		L1IndexValue l1IndexValue0 = new L1IndexValue(new Sequence(5, 6));
		l1SingleWriteIndexManager.append(l1IndexKey0, l1IndexValue0);
		l1SingleWriteIndexManager.flush();

		assertEquals("1!2!mysql-bin.3!4=5-Bucket-6", bufferedReader.readLine());

		L1IndexKey l1IndexKey1 = new L1IndexKey(
				new BinlogInfo().setTimestamp(2).setServerId(3).setBinlogFile("mysql-bin.4").setBinlogPosition(5)
						.setEventIndex(0));
		L1IndexValue l1IndexValue1 = new L1IndexValue(new Sequence(6, 7));
		l1SingleWriteIndexManager.append(l1IndexKey1, l1IndexValue1);
		l1SingleWriteIndexManager.flush();

		assertEquals("2!3!mysql-bin.4!5=6-Bucket-7", bufferedReader.readLine());
	}

	@After
	public void tearDown() throws IOException {
		if (l1SingleWriteIndexManager != null) {
			l1SingleWriteIndexManager.stop();
		}

		super.tearDown();
	}
}