package com.dianping.puma.storage.index.impl;

import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.storage.Sequence;
import com.dianping.puma.storage.StorageBaseTest;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

public class SingleReadIndexManagerTest extends StorageBaseTest {

	private SingleReadIndexManager<L1IndexKey, L1IndexValue> singleReadIndexManager;

	private SingleWriteIndexManager<L1IndexKey, L1IndexValue> singleWriteIndexManager;

	private File baseDir;

	@Before
	public void before() throws IOException {
		baseDir = new File(System.getProperty("java.io.tmpdir"), "test");
		createDirectory(baseDir);

		File file = new File(baseDir, "puma");

		singleReadIndexManager = new L1SingleReadIndexManager(file.getName());
		singleWriteIndexManager.start();

		singleWriteIndexManager = new L1SingleWriteIndexManager(file.getName());
		singleWriteIndexManager.start();
	}

	@Test
	public void testFindOldest() throws Exception {
		L1IndexKey l1IndexKey0 = new L1IndexKey(new BinlogInfo(0, "a", 0L, 0, 0));
		L1IndexValue l1IndexValue0 = new L1IndexValue(new Sequence(20151010, 0));
		singleWriteIndexManager.append(l1IndexKey0, l1IndexValue0);
		assertTrue(EqualsBuilder.reflectionEquals(l1IndexValue0, singleReadIndexManager.findOldest()));
	}

	@Test
	public void testFindLatest() throws Exception {

	}

	@Test
	public void testFind() throws Exception {

	}

	@After
	public void after() throws IOException {
		singleReadIndexManager.stop();
		singleWriteIndexManager.stop();
		deleteDirectory(baseDir);
	}
}