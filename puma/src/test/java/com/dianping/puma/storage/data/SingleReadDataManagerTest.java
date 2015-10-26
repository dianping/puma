package com.dianping.puma.storage.data;

import com.dianping.puma.core.event.RowChangedEvent;
import com.dianping.puma.storage.Sequence;
import com.dianping.puma.storage.StorageBaseTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.*;

import static org.junit.Assert.*;

public class SingleReadDataManagerTest extends StorageBaseTest {

	SingleReadDataManager singleReadDataManager;

	SingleWriteDataManager singleWriteDataManager;

	File bucket;

	@Override @Before
	public void setUp() throws IOException {
		super.setUp();

		bucket = new File(testDir, "bucket");
		createFile(bucket);

		singleReadDataManager = DataManagerFactory.newSingleReadDataManager(bucket);
		singleReadDataManager.start();

		singleWriteDataManager = DataManagerFactory.newSingleWriteDataManager(bucket, "20151010", 0);
		singleWriteDataManager.start();
	}

	@Test
	public void testPosition() throws Exception {

	}

	@Test
	public void testOpenAndNext() throws IOException {
		singleWriteDataManager.append(
				new RowChangedEvent(0, 1, "2", 3)
		);

		singleWriteDataManager.append(
				new RowChangedEvent(1, 2, "3", 4)
		);

		singleWriteDataManager.append(
				new RowChangedEvent(2, 3, "4", 5)
		);

		singleWriteDataManager.flush();

		singleReadDataManager.open(new Sequence(2015, 0, 0));
		assertTrue(singleReadDataManager.next().equals(new RowChangedEvent(0, 1, "2", 3)));
		assertTrue(singleReadDataManager.next().equals(new RowChangedEvent(1, 2, "3", 4)));
		assertTrue(singleReadDataManager.next().equals(new RowChangedEvent(2, 3, "4", 5)));
	}

	@Test(expected = EOFException.class)
	public void testOpenAndNextEOF() throws IOException {
		singleReadDataManager.open(new Sequence(new com.dianping.puma.storage.Sequence(2015, 0, 0)));
		singleReadDataManager.next();
	}

	@Override @After
	public void tearDown() throws IOException {
		if (singleReadDataManager != null) {
			singleReadDataManager.stop();
		}

		if (singleWriteDataManager != null) {
			singleWriteDataManager.stop();
		}

		super.tearDown();
	}
}