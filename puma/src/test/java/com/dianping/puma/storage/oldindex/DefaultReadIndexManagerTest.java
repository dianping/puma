package com.dianping.puma.storage.oldindex;

import com.dianping.puma.storage.Sequence;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

public class DefaultReadIndexManagerTest {

	private File l1IndexFolder = null;

	private File l2IndexFolder = null;

	private DefaultWriteIndexManager<IndexKeyImpl, IndexValueImpl> writeIndex;

	private DefaultReadIndexManager<IndexKeyImpl, IndexValueImpl> readIndex;

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

		readIndex = new DefaultReadIndexManager<IndexKeyImpl, IndexValueImpl>(
				l1IndexFolder.getAbsolutePath(), l2IndexFolder.getAbsolutePath(),
				new IndexKeyConverter(), new IndexValueConverter());
		readIndex.start();

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

	/*
	 * 根据binlog查找：查找条件的binlog位置直接可以在l1Index中查到，并且这个位置不属于transactionCommit
	 */
	@Test
	public void testfindByBinlog1() throws IOException {
		IndexKeyImpl indexKey = readIndex.findByBinlog(new IndexKeyImpl(1, 0, "bin-0001.bin", 5), false).getIndexKey();

		assertEquals(new IndexKeyImpl(1, 0, "bin-0001.bin", 5), indexKey);

		assertEquals(null, readIndex.findByBinlog(new IndexKeyImpl(1, 0, "bin-0001.bin", 5), true));
	}

	/*
	 * 根据binlog查找：查找条件的binlog位置不可以在l1Index中直接查到，并且这个位置不属于transactionCommit
	 */
	@Test
	public void testfindByBinlog2() throws IOException {
		IndexKeyImpl indexKey = readIndex.findByBinlog(new IndexKeyImpl(20, 0, "bin-0002.bin", 70), false).getIndexKey();

		assertEquals(new IndexKeyImpl(20, 0, "bin-0002.bin", 70), indexKey);

		indexKey = readIndex.findByBinlog(new IndexKeyImpl(20, 0, "bin-0002.bin", 70), true).getIndexKey();
		assertEquals(new IndexKeyImpl(16, 0, "bin-0002.bin", 50), indexKey);
	}

	/*
	 * 根据binlog查找：查找条件的binlog位置不可以在l1Index中直接查到，并且这个位置属于transactionCommit
	 */
	@Test
	public void testfindByBinlog3() throws IOException {
		IndexKeyImpl indexKey = readIndex.findByBinlog(new IndexKeyImpl(16, 0, "bin-0002.bin", 50), false).getIndexKey();

		assertEquals(new IndexKeyImpl(16, 0, "bin-0002.bin", 50), indexKey);

		indexKey = readIndex.findByBinlog(new IndexKeyImpl(16, 0, "bin-0002.bin", 50), true).getIndexKey();
		assertEquals(new IndexKeyImpl(16, 0, "bin-0002.bin", 50), indexKey);
	}

	/*
	 * 根据binlog查找：查找条件的binlog位置不可以在l1Index中直接查到，同样也不存在l2Index
	 */
	@Test
	public void testfindByBinlog4() throws IOException {
		IndexKeyImpl indexKey = readIndex.findByBinlog(new IndexKeyImpl(16, 0, "bin-0002.bin", 80), false).getIndexKey();

		assertEquals(new IndexKeyImpl(20, 0, "bin-0002.bin", 70), indexKey);

		indexKey = readIndex.findByBinlog(new IndexKeyImpl(16, 0, "bin-0002.bin", 80), true).getIndexKey();
		assertEquals(new IndexKeyImpl(16, 0, "bin-0002.bin", 50), indexKey);
	}

	/*
	 * 根据binlog查找：查找条件的binlog位置不可以在l1Index中直接查到，同样也不存在l2Index,另外需要递归的查找到transactionCommit事件
	 */
	@Test
	public void testfindByBinlog5() throws IOException {
		IndexKeyImpl indexKey = readIndex.findByBinlog(new IndexKeyImpl(105, -12, "bin-0002.bin", 100), true).getIndexKey();

		assertEquals(new IndexKeyImpl(101, -12, "bin-0001.bin", 300), indexKey);
	}

	/*
	 * 根据time查找：查找条件的time直接可以在l1Index中查到，并且这个位置不属于transactionCommit
	 */
	@Test
	public void testfindByTime1() throws IOException {
		IndexKeyImpl searchKey = new IndexKeyImpl(2);

		IndexKeyImpl key = readIndex.findByTime(searchKey, false).getIndexKey();

		assertEquals(new IndexKeyImpl(1, 0, "bin-0001.bin", 5), key);

		assertEquals(null, readIndex.findByTime(searchKey, true));
	}

	/*
	 * 根据time查找：查找条件的time不可以在l1Index中直接查到，并且这个位置不属于transactionCommit
	 */
	@Test
	public void testfindByTime2() throws IOException {
		IndexKeyImpl searchKey = new IndexKeyImpl(21);

		IndexKeyImpl key = readIndex.findByTime(searchKey, false).getIndexKey();
		assertEquals(new IndexKeyImpl(20, 0, "bin-0002.bin", 70), key);

		key = readIndex.findByTime(searchKey, true).getIndexKey();
		assertEquals(new IndexKeyImpl(16, 0, "bin-0002.bin", 50), key);
	}

	/*
	 * 根据time查找：查找条件的time不可以在l1Index中直接查到，并且这个位置属于transactionCommit
	 */
	@Test
	public void testfindByTime3() throws IOException {
		IndexKeyImpl searchKey = new IndexKeyImpl(17);

		IndexKeyImpl key = readIndex.findByTime(searchKey, false).getIndexKey();
		assertEquals(new IndexKeyImpl(16, 0, "bin-0002.bin", 50), key);

		key = readIndex.findByTime(searchKey, true).getIndexKey();
		assertEquals(new IndexKeyImpl(16, 0, "bin-0002.bin", 50), key);
	}

	/*
	 * 根据time查找：查找条件的time不可以在l1Index中直接查到，同样也不存在l2Index
	 */
	@Test
	public void testfindByTime4() throws IOException {
		IndexKeyImpl searchKey = new IndexKeyImpl(21);

		IndexKeyImpl key = readIndex.findByTime(searchKey, false).getIndexKey();
		assertEquals(new IndexKeyImpl(20, 0, "bin-0002.bin", 70), key);

		key = readIndex.findByTime(searchKey, true).getIndexKey();
		assertEquals(new IndexKeyImpl(16, 0, "bin-0002.bin", 50), key);
	}

	/*
	 * 根据time查找：查找条件的binlog位置不可以在l1Index中直接查到，同样也不存在l2Index,另外需要递归的查找到transactionCommit事件
	 */
	@Test
	public void testfindByTime5() throws IOException {
		IndexKeyImpl searchKey = new IndexKeyImpl(106);

		IndexKeyImpl key = readIndex.findByTime(searchKey, false).getIndexKey();
		assertEquals(new IndexKeyImpl(105, -12, "bin-0002.bin", 100), key);

		key = readIndex.findByTime(searchKey, true).getIndexKey();
		assertEquals(new IndexKeyImpl(101, -12, "bin-0001.bin", 300), key);
	}

	@Test
	public void testfindFirst() throws IOException {
		IndexKeyImpl firstKey = readIndex.findFirst().getIndexKey();

		assertEquals(new IndexKeyImpl(1, 0, "bin-0001.bin", 5), firstKey);
	}

	@Test
	public void testfindLatest() throws IOException {
		IndexKeyImpl latestKey = readIndex.findLatest().getIndexKey();
		assertEquals(new IndexKeyImpl(106, -12, "bin-0002.bin", 200), latestKey);
	}
}