package com.dianping.puma.storage.data;

import com.dianping.puma.core.event.RowChangedEvent;
import com.dianping.puma.storage.Sequence;
import com.dianping.puma.storage.StorageBaseTest;
import com.dianping.puma.storage.bucket.BucketFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

public class SingleWriteDataManagerTest extends StorageBaseTest {

	SingleWriteDataManager singleWriteDataManager;

	SingleReadDataManager singleReadDataManager;

	File bucket;

	@Override @Before
	public void setUp() throws IOException {
		super.setUp();

		bucket = new File(testDir, "bucket");
		createFile(bucket);

		singleWriteDataManager = DataManagerFactory.newSingleWriteDataManager(bucket.getAbsolutePath());
		singleWriteDataManager.start();

		singleReadDataManager = DataManagerFactory.newSingleReadDataManager(bucket.getAbsolutePath());
		singleReadDataManager.start();
	}

	@Test
	public void testAppendAndFlush() throws IOException {
		singleWriteDataManager.append(
				new DataKeyImpl(new Sequence(2015, 0, 0)),
				new DataValueImpl(new RowChangedEvent(0, 1, "2", 3))
		);

		singleWriteDataManager.append(
				new DataKeyImpl(new Sequence(2015, 0, 0)),
				new DataValueImpl(new RowChangedEvent(1, 2, "3", 4))
		);

		singleWriteDataManager.append(
				new DataKeyImpl(new Sequence(2015, 0, 0)),
				new DataValueImpl(new RowChangedEvent(2, 3, "4", 5))
		);

		singleWriteDataManager.flush();

		singleReadDataManager.open(new DataKeyImpl(new Sequence(2015, 0, 0)));
		assertTrue(singleReadDataManager.next().getBinlogEvent().equals(new RowChangedEvent(0, 1, "2", 3)));
		assertTrue(singleReadDataManager.next().getBinlogEvent().equals(new RowChangedEvent(1, 2, "3", 4)));
		assertTrue(singleReadDataManager.next().getBinlogEvent().equals(new RowChangedEvent(2, 3, "4", 5)));
	}

	@Test
	public void testHasRemainingForWrite() throws Exception {

	}

	@Override @After
	public void tearDown() throws IOException {
		if (singleWriteDataManager != null) {
			singleWriteDataManager.stop();
		}

		if (singleReadDataManager != null) {
			singleReadDataManager.stop();
		}

		super.tearDown();
	}
}