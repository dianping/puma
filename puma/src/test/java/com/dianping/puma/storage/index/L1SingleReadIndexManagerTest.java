package com.dianping.puma.storage.index;

import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.storage.Sequence;
import com.dianping.puma.storage.StorageBaseTest;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class L1SingleReadIndexManagerTest extends StorageBaseTest {

	L1SingleReadIndexManager l1SingleReadIndexManager;

	File bucket;

	@Before
	public void setUp() throws Exception {
		super.setUp();

		bucket = new File(testDir, "bucket");
		createFile(bucket);

		l1SingleReadIndexManager = IndexManagerFactory.newL1SingleReadIndexManager(bucket);
		l1SingleReadIndexManager.start();
	}

	@After
	public void tearDown() throws Exception {
		if (l1SingleReadIndexManager != null) {
			l1SingleReadIndexManager.stop();
		}

		super.tearDown();
	}

	@Test
	public void testDecode() throws Exception {
		String string1 = "1!2!mysql-bin.3!4=5-Bucket-6";
		Pair<BinlogInfo, Sequence> pair1 = l1SingleReadIndexManager.decode(string1.getBytes());
		assertTrue(EqualsBuilder.reflectionEquals(
				new BinlogInfo(1, 2, "mysql-bin.3", 4),
				pair1.getLeft()
		));
		assertTrue(EqualsBuilder.reflectionEquals(new Sequence(5, 6, 0), pair1.getRight()));
	}

	@Test(expected = IOException.class)
	public void testDecodeException0() throws Exception {
		String string = "1!2!mysql-bin.3!4!5-Bucket-6";
		l1SingleReadIndexManager.decode(string.getBytes());
	}

	@Test(expected = IOException.class)
	public void testDecodeException1() throws Exception {
		String string = "2!mysql-bin.3!4=5-Bucket-6";
		l1SingleReadIndexManager.decode(string.getBytes());
	}

	@Test
	public void testFindOldest() throws Exception {
		BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(bucket));
		bufferedWriter.write("1!2!mysql-bin.3!4=5-Bucket-6");
		bufferedWriter.newLine();
		bufferedWriter.write("2!3!mysql-bin.4!5=6-Bucket-7");
		bufferedWriter.newLine();
		bufferedWriter.write("3!4!mysql-bin.5!6=7-Bucket-8");
		bufferedWriter.newLine();
		bufferedWriter.flush();

		assertTrue(EqualsBuilder.reflectionEquals(new Sequence(5, 6, 0), l1SingleReadIndexManager.findOldest()));
	}

	@Test
	public void testFindOldestNull() throws Exception {
		assertNull(l1SingleReadIndexManager.findOldest());
	}

	@Test(expected = IOException.class)
	public void testFindOldestException() throws Exception {
		BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(bucket));
		bufferedWriter.write("2!mysql-bin.3!4=5-Bucket-6");
		bufferedWriter.newLine();
		bufferedWriter.write("2!3!mysql-bin.4!5=6-Bucket-7");
		bufferedWriter.newLine();
		bufferedWriter.write("3!4!mysql-bin.5!6=7-Bucket-8");
		bufferedWriter.newLine();
		bufferedWriter.flush();

		l1SingleReadIndexManager.findOldest();
	}

	@Test
	public void testFindLatest() throws Exception {
		BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(bucket));
		bufferedWriter.write("1!2!mysql-bin.3!4=5-Bucket-6");
		bufferedWriter.newLine();
		bufferedWriter.write("2!3!mysql-bin.4!5=6-Bucket-7");
		bufferedWriter.newLine();
		bufferedWriter.write("3!4!mysql-bin.5!6=7-Bucket-8");
		bufferedWriter.newLine();
		bufferedWriter.flush();

		assertTrue(EqualsBuilder.reflectionEquals(new Sequence(7, 8, 0), l1SingleReadIndexManager.findLatest()));
	}

	@Test
	public void testFindLatestNull() throws Exception {
		assertNull(l1SingleReadIndexManager.findLatest());
	}

	@Test(expected = IOException.class)
	public void testFindLatestException() throws Exception {
		BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(bucket));
		bufferedWriter.write("1!2!mysql-bin.3!4=5-Bucket-6");
		bufferedWriter.newLine();
		bufferedWriter.write("2!3!mysql-bin.4!5=6-Bucket-7");
		bufferedWriter.newLine();
		bufferedWriter.write("4!mysql-bin.5!6=7-Bucket-8");
		bufferedWriter.newLine();
		bufferedWriter.flush();

		l1SingleReadIndexManager.findLatest();
	}

	@Test
	public void testFind() throws Exception {
		BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(bucket));
		bufferedWriter.write("1!2!mysql-bin.3!4=5-Bucket-6");
		bufferedWriter.newLine();
		bufferedWriter.write("2!3!mysql-bin.4!5=6-Bucket-7");
		bufferedWriter.newLine();
		bufferedWriter.write("3!4!mysql-bin.5!6=7-Bucket-8");
		bufferedWriter.newLine();
		bufferedWriter.flush();

		assertTrue(EqualsBuilder.reflectionEquals(
				l1SingleReadIndexManager.find(new BinlogInfo(2, 3, "mysql-bin.4", 5)),
				new Sequence(5, 6, 0)
		));
	}
}