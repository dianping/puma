package com.dianping.puma.storage.oldindex;

import com.dianping.puma.storage.Sequence;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import static org.junit.Assert.*;

public class DefaultWriteIndexManagerTest {

	private File l1IndexFolder = null;

	private File l2IndexFolder = null;

	private DefaultWriteIndexManager<IndexKeyImpl, IndexValueImpl> writeIndex;

	private void addIndex(
			IndexKeyImpl indexKey,
			String bucketName,
			String database,
			String table,
			boolean isDdl,
			boolean isDml,
			boolean isTransactionBegin,
			boolean isTransactionCommit,
			Sequence seq,
			boolean shouldAddL1Index,
			boolean shouldAddL2Index) throws IOException {
		if (shouldAddL1Index) {
			writeIndex.addL1Index(indexKey, bucketName);
		}

		if (shouldAddL2Index) {
			IndexValueImpl l2IndexValue = new IndexValueImpl();

			l2IndexValue.setIndexKey(indexKey);
			l2IndexValue.setDdl(isDdl);
			l2IndexValue.setDml(isDml);
			l2IndexValue.setTransactionBegin(isTransactionBegin);
			l2IndexValue.setTransactionCommit(isTransactionCommit);
			l2IndexValue.setSequence(seq);

			writeIndex.addL2Index(indexKey, l2IndexValue);
		}
	}

	@After
	public void after() {
		FileUtils.deleteQuietly(l1IndexFolder);
		FileUtils.deleteQuietly(l2IndexFolder);
	}

	@Before
	public void before() throws IOException {
		l1IndexFolder = new File(System.getProperty("java.io.tmpdir", "."), "Index1Test");
		l1IndexFolder.mkdirs();

		l2IndexFolder = new File(System.getProperty("java.io.tempdir", "."), "Index2Test");
		l2IndexFolder.mkdirs();

		writeIndex = new DefaultWriteIndexManager<IndexKeyImpl, IndexValueImpl>(
				l1IndexFolder.getAbsolutePath(), l2IndexFolder.getAbsolutePath(),
				new IndexKeyConverter(), new IndexValueConverter());
		writeIndex.start();

		addIndex(new IndexKeyImpl(1, 0, "bin-0001.bin", 5), "1", "dianping", "receipt", false, true, false, false,
				new Sequence(123L, 0), true, true);
		addIndex(new IndexKeyImpl(5, 0, "bin-0002.bin", 10), null, "dianping", "receipt", false, true, true, false,
				new Sequence(123555L, 0), false, true);
		addIndex(new IndexKeyImpl(10, 0, "bin-0002.bin", 30), null, "dianping", "receipt", false, true, false, false,
				new Sequence(123556L, 0), false, true);
		addIndex(new IndexKeyImpl(16, 0, "bin-0002.bin", 50), null, "dianping", "receipt", false, true, false, true,
				new Sequence(123557L, 0), false, true);
		addIndex(new IndexKeyImpl(20, 0, "bin-0002.bin", 70), null, "dianping", "receipt", false, true, false, false,
				new Sequence(123557L, 0), false, true);
		addIndex(new IndexKeyImpl(22, 0, "bin-0002.bin", 90), null, "dianping", "receipt", false, true, false, false,
				new Sequence(123557L, 0), false, true);
		addIndex(new IndexKeyImpl(34, 0, "bin-0003.bin", 200), "2", "dianping", "receipt", false, true, false, false,
				new Sequence(123559L, 0), true, true);
		addIndex(new IndexKeyImpl(100, -12, "bin-0001.bin", 200), "3", "dianping", "receipt", false, true, false, false,
				new Sequence(123560L, 0), true, true);
		addIndex(new IndexKeyImpl(101, -12, "bin-0001.bin", 300), "3", "dianping", "receipt", false, true, false, true,
				new Sequence(123560L, 0), false, true);
		addIndex(new IndexKeyImpl(102, -12, "bin-0001.bin", 400), "3", "dianping", "receipt", false, true, true, false,
				new Sequence(123560L, 0), false, true);
		addIndex(new IndexKeyImpl(103, -12, "bin-0001.bin", 500), "3", "dianping", "receipt", false, true, false, false,
				new Sequence(123560L, 0), false, true);
		addIndex(new IndexKeyImpl(104, -12, "bin-0002.bin", 0), "4", "dianping", "receipt", false, true, false, false,
				new Sequence(123560L, 0), true, true);
		addIndex(new IndexKeyImpl(105, -12, "bin-0002.bin", 100), "4", "dianping", "receipt", false, true, false, false,
				new Sequence(123560L, 0), false, true);
		addIndex(new IndexKeyImpl(106, -12, "bin-0002.bin", 200), "4", "dianping", "receipt", false, true, false, false,
				new Sequence(123560L, 0), false, true);

		writeIndex.flush();
	}

	@Test
	public void testAddL1Index() throws IOException {
		File l1IndexFile = new File(l1IndexFolder, "l1Index.l1idx");

		assertTrue(l1IndexFile.exists());

		Properties prop = new Properties();
		prop.load(new FileInputStream(l1IndexFile));

		assertEquals(4, prop.size());
		assertEquals("1", prop.getProperty("1!0!bin-0001.bin!5"));
		assertEquals("2", prop.getProperty("34!0!bin-0003.bin!200"));
		assertEquals("3", prop.getProperty("100!-12!bin-0001.bin!200"));
	}

	@Test
	public void testAddL2Index() throws IOException {
		File l2IndexFile1 = new File(l2IndexFolder, "1.l2idx");
		assertTrue(l2IndexFile1.exists());

		File l2IndexFile2 = new File(l2IndexFolder, "2.l2idx");
		assertTrue(l2IndexFile2.exists());

		File l2IndexFile3 = new File(l2IndexFolder, "3.l2idx");
		assertTrue(l2IndexFile3.exists());

		LocalFileIndexBucket<IndexKeyImpl, IndexValueImpl> bucket = new LocalFileIndexBucket<IndexKeyImpl, IndexValueImpl>(
				"1.l2idx", l2IndexFile1, new IndexValueConverter());

		assertEquals(new IndexKeyImpl(1, 0, "bin-0001.bin", 5), bucket.next().getIndexKey());
		assertEquals(new IndexKeyImpl(5, 0, "bin-0002.bin", 10), bucket.next().getIndexKey());
	}

	@Test
	public void testHasNextIndexBucket() throws IOException {
		assertFalse(writeIndex.hasNextIndexBucket("xxx"));
		assertTrue(writeIndex.hasNextIndexBucket("1"));
		assertTrue(writeIndex.hasNextIndexBucket("2"));
		assertTrue(writeIndex.hasNextIndexBucket("3"));
		assertFalse(writeIndex.hasNextIndexBucket("4"));
	}

	@Test
	public void testGetNextIndexBucket() throws IOException {
		IndexBucket<IndexKeyImpl, IndexValueImpl> indexBucket = writeIndex.getNextIndexBucket("1");

		IndexValueImpl convertFromObj = indexBucket.next();

		assertEquals("bin-0003.bin", convertFromObj.getIndexKey().getBinlogFile());
		assertEquals(200L, convertFromObj.getIndexKey().getBinlogPosition());
		assertEquals(0L, convertFromObj.getIndexKey().getServerId());
		assertEquals(false, convertFromObj.isDdl());
		assertEquals(true, convertFromObj.isDml());
		assertEquals(123559L, convertFromObj.getSequence().longValue());

		indexBucket.stop();
	}

	@Test
	public void testRemoveByL2IndexName() throws IOException {
		writeIndex.removeByL2IndexName("1");

		File l1IndexFile = new File(l1IndexFolder, "l1Index.l1idx");
		assertTrue(l1IndexFile.exists());
		Properties prop = new Properties();
		prop.load(new FileInputStream(l1IndexFile));
		assertEquals(3, prop.size());

		File l2IndexFile = new File(l2IndexFolder, "1.l2idx");
		assertFalse(l2IndexFile.exists());
	}

	@Test
	public void testCanNotAddSameL1Index() throws IOException {
		int size = writeIndex.loadLinkedL1Index().size();
		addIndex(new IndexKeyImpl(200, -12, "bin-0003.bin", 0), "4", "dianping", "receipt", false, true, false, false,
				new Sequence(123561L, 0), true, true);
		assertEquals(size + 1, writeIndex.loadLinkedL1Index().size());
		addIndex(new IndexKeyImpl(200, -12, "bin-0003.bin", 0), "4", "dianping", "receipt", false, true, false, false,
				new Sequence(123561L, 0), true, true);
		assertEquals(size + 1, writeIndex.loadLinkedL1Index().size());
	}

}