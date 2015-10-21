package com.dianping.puma.storage.index.impl;

import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.storage.Sequence;
import com.dianping.puma.storage.StorageBaseTest;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.*;

import static org.junit.Assert.*;

public class L1SingleReadIndexManagerTest extends StorageBaseTest {

	L1SingleReadIndexManager l1SingleReadIndexManager;

	File bucket;

	@Before
	public void setUp() throws IOException {
		super.setUp();

		bucket = new File(testDir, "bucket");
		createFile(bucket);

		l1SingleReadIndexManager = new L1SingleReadIndexManager(bucket.getAbsolutePath());
		l1SingleReadIndexManager.start();
	}

	@After
	public void tearDown() throws IOException {
		if (l1SingleReadIndexManager != null) {
			l1SingleReadIndexManager.stop();
		}

		super.tearDown();
	}

	@Test
	public void testDecode() throws Exception {
		String string0 = "1441100813!3013306141!mysql-bin.002217!268451845=20150901-Bucket-0";
		Pair<L1IndexKey, L1IndexValue> pair0 = l1SingleReadIndexManager.decode(string0.getBytes());
		assertTrue(EqualsBuilder.reflectionEquals(
				new L1IndexKey(new BinlogInfo().setTimestamp(1441100813).setBinlogFile("mysql-bin.002217")
						.setBinlogPosition(268451845).setEventIndex(
								0).setServerId(3013306141L)),
				pair0.getLeft()
		));
		assertTrue(EqualsBuilder.reflectionEquals(new L1IndexValue(new Sequence(20150901, 0, 0)), pair0.getRight()));

		String string1 = "1!2!mysql-bin.3!4=5-Bucket-6";
		Pair<L1IndexKey, L1IndexValue> pair1 = l1SingleReadIndexManager.decode(string1.getBytes());
		assertTrue(EqualsBuilder.reflectionEquals(
				new L1IndexKey(new BinlogInfo().setTimestamp(1).setBinlogFile("mysql-bin.3").setBinlogPosition(4)
						.setEventIndex(0).setServerId(2)),
				pair1.getLeft()
		));
		assertTrue(EqualsBuilder.reflectionEquals(new L1IndexValue(new Sequence(5, 6, 0)), pair1.getRight()));
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

		assertTrue(EqualsBuilder.reflectionEquals(
				new Sequence(5, 6, 0),
				l1SingleReadIndexManager.findOldest().getSequence()));
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

		assertTrue(EqualsBuilder.reflectionEquals(
				new Sequence(7, 8, 0),
				l1SingleReadIndexManager.findLatest().getSequence()));
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
				l1SingleReadIndexManager.find(new L1IndexKey(
						new BinlogInfo().setTimestamp(1).setServerId(2).setBinlogFile("mysql-bin.3")
								.setBinlogPosition(4).setEventIndex(0))).getSequence(),
				new Sequence(5, 6, 0)
		));

		assertTrue(EqualsBuilder.reflectionEquals(
				l1SingleReadIndexManager.find(new L1IndexKey(
						new BinlogInfo().setTimestamp(2).setServerId(3).setBinlogFile("mysql-bin.4")
								.setBinlogPosition(5).setEventIndex(0))).getSequence(),
				new Sequence(6, 7, 0)
		));

		assertTrue(EqualsBuilder.reflectionEquals(
				l1SingleReadIndexManager.find(new L1IndexKey(
						new BinlogInfo().setTimestamp(3).setServerId(4).setBinlogFile("mysql-bin.5")
								.setBinlogPosition(6).setEventIndex(0))).getSequence(),
				new Sequence(7, 8, 0)
		));
	}

	@Test
	public void testFindNull() throws Exception {
		assertNull(l1SingleReadIndexManager.find(new L1IndexKey(
				new BinlogInfo().setTimestamp(2).setServerId(3).setBinlogFile("mysql-bin.4")
						.setBinlogPosition(5).setEventIndex(0))));
	}
}