//package com.dianping.puma.storage;
//
//import java.io.EOFException;
//import java.io.File;
//import java.io.IOException;
//import java.io.RandomAccessFile;
//
//import org.junit.After;
//import org.junit.Before;
//import org.junit.Test;
//import org.unidal.helper.Files.Dir;
//
//import com.dianping.puma.core.codec.RawEventCodec;
//import com.dianping.puma.core.event.RowChangedEvent;
//import com.dianping.puma.storage.data.DataBucket;
//import com.dianping.puma.storage.bucket.LocalFileDataBucketManager;
//import com.dianping.puma.storage.exception.StorageLifeCycleException;
//import com.dianping.puma.storage.index.IndexBucket;
//import com.dianping.puma.storage.index.IndexKeyImpl;
//import com.dianping.puma.storage.index.IndexManager;
//import com.dianping.puma.storage.index.IndexValueImpl;
//
//public class StorageIntegrityCheckerTest {
//
//	private static final File masterStorageBaseDir = new File(System.getProperty("java.io.tmpdir", "."),
//	      "puma/storage/master");
//
//	private static final File slaveStorageBaseDir = new File(System.getProperty("java.io.tmpdir", "."),
//	      "puma/storage/slave/");
//
//	private static final File binlogIndexBaseDir = new File(System.getProperty("java.io.tmpdir", "."),
//	      "puma/binlogIndex/");
//
//	private DefaultEventStorage eventStorage;
//
//	@Before
//	public void setup() throws StorageLifeCycleException {
//		eventStorage = new DefaultEventStorage();
//		eventStorage.setName("storage-test");
//		eventStorage.setTaskName("test");
//		eventStorage.setCodec(new RawEventCodec());
//
//		LocalFileDataBucketManager masterBucketIndex = new LocalFileDataBucketManager();
//		masterBucketIndex.setBaseDir(masterStorageBaseDir.getAbsolutePath());
//		masterBucketIndex.setBucketFilePrefix("Bucket-");
//		masterBucketIndex.setMaxBucketLengthMB(1000);
//		eventStorage.setMasterBucketIndex(masterBucketIndex);
//
//		LocalFileDataBucketManager slaveBucketIndex = new LocalFileDataBucketManager();
//		slaveBucketIndex.setBaseDir(slaveStorageBaseDir.getAbsolutePath());
//		slaveBucketIndex.setBucketFilePrefix("Bucket-");
//		slaveBucketIndex.setMaxBucketLengthMB(1000);
//		eventStorage.setSlaveBucketIndex(slaveBucketIndex);
//
//		DefaultArchiveStrategy archiveStrategy = new DefaultArchiveStrategy();
//		archiveStrategy.setServerName("test");
//		archiveStrategy.setMaxMasterFileCount(25);
//		eventStorage.setArchiveStrategy(archiveStrategy);
//
//		DefaultCleanupStrategy cleanupStrategy = new DefaultCleanupStrategy();
//		cleanupStrategy.setPreservedDay(2);
//		eventStorage.setCleanupStrategy(cleanupStrategy);
//
//		eventStorage.setBinlogIndexBaseDir(binlogIndexBaseDir.getAbsolutePath());
//
//		eventStorage.start();
//	}
//
//	@After
//	public void cleanUp() {
//		eventStorage.stop();
//
//		File file = new File(System.getProperty("java.io.tmpdir", "."), "puma");
//
//		Dir.INSTANCE.delete(file, true);
//	}
//
//	@Test(expected = EOFException.class)
//	public void testBrokenIndex() throws IOException {
//		// build broken index
//		IndexManager<IndexKeyImpl, IndexValueImpl> indexManager = this.eventStorage.getWriteIndexManager();
//
//		indexManager.addL1Index(new IndexKeyImpl(0L, 1L, "mysql-000001", 4L), "1");
//		indexManager.flush();
//
//		this.eventStorage.store(new RowChangedEvent(0L, 1L, "mysql-000001", 4L));
//		this.eventStorage.store(new RowChangedEvent(0L, 1L, "mysql-000001", 14L));
//		this.eventStorage.flush();
//
//		IndexBucket<IndexKeyImpl, IndexValueImpl> indexBucket = indexManager.getIndexBucket("1");
//		indexBucket.start();
//		indexBucket.append(new byte[] { 1, 3, 4, 6 });
//		indexBucket.stop();
//
//		// begin to check and repair
//		DefaultStorageIntegrityChecker checker = new DefaultStorageIntegrityChecker(this.eventStorage);
//		checker.checkAndRepair();
//
//		indexBucket = indexManager.getIndexBucket("1");
//		indexBucket.start();
//
//		try {
//			indexBucket.next();
//			indexBucket.next();
//		} catch (Exception e) {
//		}
//		// expected eof exception
//		try {
//			indexBucket.next();
//		} finally {
//			indexBucket.stop();
//		}
//	}
//
//	@Test(expected = EOFException.class)
//	public void testBrokenData() throws IOException {
//		// build broken data
//		IndexManager<IndexKeyImpl, IndexValueImpl> indexManager = this.eventStorage.getWriteIndexManager();
//
//		indexManager.addL1Index(new IndexKeyImpl(0L, 1L, "mysql-000001", 4L), "1");
//
//		this.eventStorage.store(new RowChangedEvent(0L, 1L, "mysql-000001", 4L));
//		this.eventStorage.store(new RowChangedEvent(0L, 1L, "mysql-000001", 14L));
//		this.eventStorage.store(new RowChangedEvent(0L, 1L, "mysql-000001", 24L));
//		this.eventStorage.flush();
//
//		IndexBucket<IndexKeyImpl, IndexValueImpl> indexBucket = indexManager.getIndexBucket("1");
//		indexBucket.start();
//		IndexValueImpl first = indexBucket.next();
//		indexBucket.stop();
//
//		DataBucket readBucket = eventStorage.getBucketManager().getReadBucket(first.getSequence().longValue(), false);
//
//		File file = new File(System.getProperty("java.io.tmpdir", "."), "puma/storage/master/"
//		      + readBucket.getBucketFileName());
//		RandomAccessFile accessFile = new RandomAccessFile(file, "rwd");
//		accessFile.setLength(accessFile.length() - 2); // truncated file to broken data file;
//		accessFile.close();
//		readBucket.stop();
//
//		// begin to check and repair
//		DefaultStorageIntegrityChecker checker = new DefaultStorageIntegrityChecker(this.eventStorage);
//		checker.checkAndRepair();
//
//		indexBucket = indexManager.getIndexBucket("1");
//		indexBucket.start();
//
//		try {
//			indexBucket.next();
//			indexBucket.next();
//		} catch (Exception e) {
//		}
//
//		// expected eof exception
//		try {
//			indexBucket.next();
//		} finally {
//			indexBucket.stop();
//		}
//	}
//}
