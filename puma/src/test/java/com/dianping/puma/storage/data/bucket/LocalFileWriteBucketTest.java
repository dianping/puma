//package com.dianping.puma.storage.data.bucket;
//
//import com.dianping.puma.storage.Sequence;
//import com.dianping.puma.storage.bucket.LocalFileWriteBucket;
//import org.apache.commons.io.FileUtils;
//import org.junit.After;
//import org.junit.Before;
//import org.junit.Test;
//
//import java.io.DataInputStream;
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.IOException;
//
//import static org.junit.Assert.*;
//
//public class LocalFileWriteBucketTest {
//
//	private File tempDir;
//
//	private File tempFile;
//
//	private LocalFileWriteBucket bucket;
//
//	@Before
//	public void before() throws IOException {
//		tempDir = new File(System.getProperty("java.io.tmpdir"), "puma");
//		deleteNewFolder(tempDir);
//		createNewFolder(tempDir);
//
//		tempFile = new File(tempDir, "test-file");
//		createNewFile(tempFile);
//
//		bucket = new LocalFileWriteBucket(new Sequence(20151012, 0, 0), tempFile, 50, 50);
//		bucket.start();
//	}
//
//	@Test
//	public void testAppend() throws IOException {
//		bucket.append(new byte[]{'a', 'b'});
//		bucket.append(new byte[]{'c', 'd', 'e'});
//		bucket.append(new byte[]{'f', 'g', 'h', 'i'});
//		bucket.append(new byte[]{'j', 'k', 'l', 'm', 'n'});
//		bucket.flush();
//
//		DataInputStream input = new DataInputStream(new FileInputStream(tempFile));
//		assertEquals(2, input.readInt());
//		assertEquals('a', input.readByte());
//		assertEquals('b', input.readByte());
//		assertEquals(3, input.readInt());
//		assertEquals('c', input.readByte());
//		assertEquals('d', input.readByte());
//		assertEquals('e', input.readByte());
//		assertEquals(4, input.readInt());
//		assertEquals('f', input.readByte());
//		assertEquals('g', input.readByte());
//		assertEquals('h', input.readByte());
//		assertEquals('i', input.readByte());
//		assertEquals(5, input.readInt());
//		assertEquals('j', input.readByte());
//		assertEquals('k', input.readByte());
//		assertEquals('l', input.readByte());
//		assertEquals('m', input.readByte());
//		assertEquals('n', input.readByte());
//
//		bucket.append(new byte[]{'o'});
//		bucket.append(new byte[]{'p', 'q'});
//		bucket.flush();
//
//		assertEquals(1, input.readInt());
//		assertEquals('o', input.readByte());
//		assertEquals(2, input.readInt());
//		assertEquals('p', input.readByte());
//		assertEquals('q', input.readByte());
//	}
//
//	@Test
//	public void testHasRemainingForWrite() throws IOException {
//		assertTrue(bucket.hasRemainingForWrite());
//
//		bucket.append(new byte[6]);
//		bucket.flush();
//		assertTrue(bucket.hasRemainingForWrite());
//
//		bucket.append(new byte[16]);
//		bucket.flush();
//		assertTrue(bucket.hasRemainingForWrite());
//
//		bucket.append(new byte[15]);
//		bucket.flush();
//		assertTrue(bucket.hasRemainingForWrite());
//
//		bucket.append(new byte[1]);
//		bucket.flush();
//		assertFalse(bucket.hasRemainingForWrite());
//
//		bucket.append(new byte[10]);
//		bucket.flush();
//		assertFalse(bucket.hasRemainingForWrite());
//	}
//
//	@After
//	public void after() throws IOException {
//		bucket.stop();
//		deleteNewFolder(tempDir);
//	}
//
//	protected void createNewFolder(File file) throws IOException {
//		if (!file.mkdirs()) {
//			throw new RuntimeException("failed to create new folder.");
//		}
//	}
//
//	protected void deleteNewFolder(File file) throws IOException {
//		FileUtils.deleteDirectory(file);
//	}
//
//	protected void createNewFile(File file) throws IOException {
//		if (!file.createNewFile()) {
//			throw new RuntimeException("failed to create new file.");
//		}
//	}
//}