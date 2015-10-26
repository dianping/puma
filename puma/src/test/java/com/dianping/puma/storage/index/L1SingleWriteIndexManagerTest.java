package com.dianping.puma.storage.index;

import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.storage.Sequence;
import com.dianping.puma.storage.StorageBaseTest;
import com.dianping.puma.storage.index.L1IndexKey;
import com.dianping.puma.storage.index.L1IndexValue;
import com.dianping.puma.storage.index.L1SingleWriteIndexManager;
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

		l1SingleWriteIndexManager = IndexManagerFactory.newL1SingleWriteIndexManager(bucket);
		l1SingleWriteIndexManager.start();
	}

	@Test
	public void testEncode() throws Exception {
		BinlogInfo binlogInfo0 = new BinlogInfo(1, 2, "mysql-bin.3", 4);
		Sequence sequence0 = new Sequence(5, 6);
		String string0 = "1!2!mysql-bin.3!4=5-Bucket-6";
		assertArrayEquals(string0.getBytes(), l1SingleWriteIndexManager.encode(binlogInfo0, sequence0));

		BinlogInfo binlogInfo1 = new BinlogInfo(2, 3, "mysql-bin.4", 5);
		Sequence sequence1 = new Sequence(6, 7);
		String string1 = "2!3!mysql-bin.4!5=6-Bucket-7";
		assertArrayEquals(string1.getBytes(), l1SingleWriteIndexManager.encode(binlogInfo1, sequence1));
	}

	@Test
	public void testAppendAndFlush() throws Exception {
		BufferedReader bufferedReader = new BufferedReader(new FileReader(bucket));

		BinlogInfo binlogInfo0 = new BinlogInfo(1, 2, "mysql-bin.3", 4);
		Sequence sequence0 = new Sequence(5, 6);
		l1SingleWriteIndexManager.append(binlogInfo0, sequence0);
		l1SingleWriteIndexManager.flush();

		assertEquals("1!2!mysql-bin.3!4=5-Bucket-6", bufferedReader.readLine());

		BinlogInfo binlogInfo1 = new BinlogInfo(2, 3, "mysql-bin.4", 5);
		Sequence sequence1 = new Sequence(6, 7);
		l1SingleWriteIndexManager.append(binlogInfo1, sequence1);
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