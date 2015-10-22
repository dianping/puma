package com.dianping.puma.storage.index;

import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.storage.Sequence;
import com.dianping.puma.storage.StorageBaseTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

public class L2SingleReadIndexManagerTest extends StorageBaseTest {

	L2SingleReadIndexManager l2SingleReadIndexManager;

	L2SingleWriteIndexManager l2SingleWriteIndexManager;

	File bucket;

	@Override @Before
	public void setUp() throws IOException {
		super.setUp();

		bucket = new File(testDir, "bucket");
		createFile(bucket);

		l2SingleReadIndexManager = IndexManagerFactory.newL2SingleReadIndexManager(bucket.getAbsolutePath());
		l2SingleReadIndexManager.start();

		l2SingleWriteIndexManager = IndexManagerFactory.newL2SingleWriteIndexManager(bucket.getAbsolutePath());
		l2SingleWriteIndexManager.start();
	}

	@Test
	public void testFindOldest() throws Exception {
		l2SingleWriteIndexManager.append(
				new L2IndexKey(new BinlogInfo(0, 1, "2", 3)),
				new L2IndexValue(new Sequence(2015, 0, 0))
		);

		l2SingleWriteIndexManager.append(
				new L2IndexKey(new BinlogInfo(1, 2, "3", 4)),
				new L2IndexValue(new Sequence(2015, 0, 10))
		);

		l2SingleWriteIndexManager.append(
				new L2IndexKey(new BinlogInfo(2, 3, "4", 5)),
				new L2IndexValue(new Sequence(2015, 0, 20))
		);

		l2SingleWriteIndexManager.flush();

		L2IndexValue l2IndexValue = l2SingleReadIndexManager.findOldest();
		assertEquals(new Sequence(2015, 0, 0), l2IndexValue.getSequence());
	}

	@Test
	public void testFindOldestNull() throws Exception {
		assertNull(l2SingleReadIndexManager.findOldest());
	}

	@Test
	public void testFindLatest() throws Exception {
		l2SingleWriteIndexManager.append(
				new L2IndexKey(new BinlogInfo(0, 1, "2", 3)),
				new L2IndexValue(new Sequence(2015, 0, 0))
		);

		l2SingleWriteIndexManager.append(
				new L2IndexKey(new BinlogInfo(1, 2, "3", 4)),
				new L2IndexValue(new Sequence(2015, 0, 10))
		);

		l2SingleWriteIndexManager.append(
				new L2IndexKey(new BinlogInfo(2, 3, "4", 5)),
				new L2IndexValue(new Sequence(2015, 0, 20))
		);

		l2SingleWriteIndexManager.flush();

		L2IndexValue l2IndexValue = l2SingleReadIndexManager.findLatest();
		assertEquals(new Sequence(2015, 0, 20), l2IndexValue.getSequence());
	}

	@Test
	public void testFindLatestNull() throws Exception {
		assertNull(l2SingleReadIndexManager.findLatest());
	}

	@Test
	public void testFind() throws IOException {
		l2SingleWriteIndexManager.append(
				new L2IndexKey(new BinlogInfo(0, 1, "2", 3)),
				new L2IndexValue(new Sequence(2015, 0, 0))
		);

		l2SingleWriteIndexManager.append(
				new L2IndexKey(new BinlogInfo(1, 1, "2", 3)),
				new L2IndexValue(new Sequence(2015, 0, 10))
		);

		l2SingleWriteIndexManager.append(
				new L2IndexKey(new BinlogInfo(2, 1, "2", 3)),
				new L2IndexValue(new Sequence(2015, 0, 20))
		);

		l2SingleWriteIndexManager.flush();

		L2IndexValue l2IndexValue = l2SingleReadIndexManager.find(new L2IndexKey(new BinlogInfo(1, 1, "2", 3)));
		assertEquals(new Sequence(2015, 0, 0), l2IndexValue.getSequence());
	}

	@Test
	public void testFindNull() throws IOException {
		assertNull(l2SingleReadIndexManager.find(new L2IndexKey(new BinlogInfo(1, 2, "3", 4))));
	}

	@Override @After
	public void tearDown() throws IOException {
		if (l2SingleReadIndexManager != null) {
			l2SingleReadIndexManager.stop();
		}

		if (l2SingleWriteIndexManager != null) {
			l2SingleWriteIndexManager.stop();
		}

		super.tearDown();
	}
}