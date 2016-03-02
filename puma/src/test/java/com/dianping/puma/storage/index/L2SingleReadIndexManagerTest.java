package com.dianping.puma.storage.index;

import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.storage.Sequence;
import com.dianping.puma.storage.StorageBaseTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class L2SingleReadIndexManagerTest extends StorageBaseTest {

	L2SingleReadIndexManager l2SingleReadIndexManager;

	L2SingleWriteIndexManager l2SingleWriteIndexManager;

	File bucket;

	@Override @Before
	public void setUp() throws Exception {
		super.setUp();

		bucket = new File(testDir, "bucket");
		createFile(bucket);

		l2SingleReadIndexManager = IndexManagerFactory.newL2SingleReadIndexManager(bucket);
		l2SingleReadIndexManager.start();

		l2SingleWriteIndexManager = IndexManagerFactory.newL2SingleWriteIndexManager(bucket, "20151015", 0);
		l2SingleWriteIndexManager.start();
	}

	@Test
	public void testFindOldest() throws Exception {

		l2SingleWriteIndexManager.append(new BinlogInfo(0, 1, "f.2", 3), new Sequence(2015, 0, 0));
		l2SingleWriteIndexManager.append(new BinlogInfo(1, 2, "f.3", 4), new Sequence(2015, 0, 10));
		l2SingleWriteIndexManager.append(new BinlogInfo(2, 3, "f.4", 5), new Sequence(2015, 0, 20));

		l2SingleWriteIndexManager.flush();

		Sequence sequence = l2SingleReadIndexManager.findOldest();
		assertEquals(new Sequence(2015, 0, 0), sequence);
	}

	@Test
	public void testFindOldestNull() throws Exception {
		assertNull(l2SingleReadIndexManager.findOldest());
	}

	@Test
	public void testFindLatest() throws Exception {

		l2SingleWriteIndexManager.append(new BinlogInfo(0, 1, "f.2", 3), new Sequence(2015, 0, 0));
		l2SingleWriteIndexManager.append(new BinlogInfo(1, 2, "f.3", 4), new Sequence(2015, 0, 10));
		l2SingleWriteIndexManager.append(new BinlogInfo(2, 3, "f.4", 5), new Sequence(2015, 0, 20));

		l2SingleWriteIndexManager.flush();

		Sequence sequence = l2SingleReadIndexManager.findLatest();
		assertEquals(new Sequence(2015, 0, 20), sequence);
	}

	@Test
	public void testFindLatestNull() throws Exception {
		assertNull(l2SingleReadIndexManager.findLatest());
	}

	@Test(expected = IOException.class)
	public void testFind() throws IOException {

		l2SingleWriteIndexManager.append(new BinlogInfo(0, 1, "f.2", 3), new Sequence(2015, 0, 0));
		l2SingleWriteIndexManager.append(new BinlogInfo(1, 2, "f.3", 4), new Sequence(2015, 0, 10));
		l2SingleWriteIndexManager.append(new BinlogInfo(2, 3, "f.4", 5), new Sequence(2015, 0, 20));
		l2SingleWriteIndexManager.flush();

		l2SingleReadIndexManager.find(new BinlogInfo(1, 1, "f.2", 3));
	}

	@Override @After
	public void tearDown() throws Exception {
		if (l2SingleReadIndexManager != null) {
			l2SingleReadIndexManager.stop();
		}

		if (l2SingleWriteIndexManager != null) {
			l2SingleWriteIndexManager.stop();
		}

		super.tearDown();
	}
}