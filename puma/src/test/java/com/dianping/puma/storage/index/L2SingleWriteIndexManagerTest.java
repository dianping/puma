package com.dianping.puma.storage.index;

import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.storage.Sequence;
import com.dianping.puma.storage.StorageBaseTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;

public class L2SingleWriteIndexManagerTest extends StorageBaseTest {

	L2SingleWriteIndexManager l2SingleWriteIndexManager;

	L2SingleReadIndexManager l2SingleReadIndexManager;

	File bucket;

	@Override @Before
	public void setUp() throws Exception {
		super.setUp();

		bucket = new File(testDir, "bucket");
		createFile(bucket);

		l2SingleWriteIndexManager = IndexManagerFactory.newL2SingleWriteIndexManager(bucket, "20151015", 0);
		l2SingleWriteIndexManager.start();

		l2SingleReadIndexManager = IndexManagerFactory.newL2SingleReadIndexManager(bucket);
		l2SingleReadIndexManager.start();
	}

	@Test
	public void testAppendAndFlush() throws Exception {
		l2SingleWriteIndexManager.append(new BinlogInfo(0, 1, "2", 3), new Sequence(2015, 0, 0));
		l2SingleWriteIndexManager.append(new BinlogInfo(1, 2, "3", 4), new Sequence(2015, 0, 10));
		l2SingleWriteIndexManager.append(new BinlogInfo(2, 3, "4", 5), new Sequence(2015, 0, 20));
		l2SingleWriteIndexManager.flush();

		Sequence sequence = l2SingleReadIndexManager.findLatest();
		assertEquals(new Sequence(2015, 0, 20), sequence);
	}

	@Override @After
	public void tearDown() throws Exception {
		if (l2SingleWriteIndexManager != null) {
			l2SingleWriteIndexManager.stop();
		}

		if (l2SingleReadIndexManager != null) {
			l2SingleReadIndexManager.stop();
		}

		super.tearDown();
	}
}