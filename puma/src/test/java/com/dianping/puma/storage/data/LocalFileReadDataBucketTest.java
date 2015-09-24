package com.dianping.puma.storage.data;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.*;

import static org.junit.Assert.*;

public class LocalFileReadDataBucketTest {

	private File tempDir;

	@Before
	public void before() {
		tempDir = new File(System.getProperty("java.io.tempdir"), "puma");
		if (!tempDir.mkdirs()) {
			throw new RuntimeException("failed to create temp directory.");
		}
	}

	@Test
	public void testNext_0() throws IOException {
		// Functionality.

		// Case 0.
		File file_0 = new File(tempDir, "file_0");
		createNewFile(file_0);
		LocalFileReadDataBucket bucket_0 = new LocalFileReadDataBucket(file_0);
		bucket_0.start();

		DataOutputStream os_0 = new DataOutputStream(new FileOutputStream(file_0));
		byte[] bytes;

		os_0.writeInt(1);
		bytes = new byte[] {'a'};
		os_0.write(bytes);
		os_0.flush();
		assertArrayEquals(bytes, bucket_0.next());

		os_0.writeInt(2);
		bytes = new byte[] {'a', 'b'};
		os_0.write(bytes);
		os_0.flush();
		assertArrayEquals(bytes, bucket_0.next());

		os_0.writeInt(3);
		bytes = new byte[] {'a', 'b', 'c'};
		os_0.write(bytes);
		os_0.flush();
		assertArrayEquals(bytes, bucket_0.next());

		bucket_0.stop();
	}

	@Test(expected = IOException.class)
	public void testNext_1() throws IOException {
		// Exception.

		// Case 0.
		File file_0 = new File(tempDir, "file_0");
		createNewFile(file_0);
		LocalFileReadDataBucket bucket_0 = new LocalFileReadDataBucket(file_0);
		bucket_0.start();

		DataOutputStream os_0 = new DataOutputStream(new FileOutputStream(file_0));
		byte[] bytes;

		os_0.writeInt(2);
		bytes = new byte[]{'a'};
		os_0.write(bytes);
		os_0.flush();

		try {
			bucket_0.next();
		} catch (IOException io) {
			throw io;
		} finally {
			bucket_0.stop();
		}
	}

	@Test
	public void testSkip_0() throws IOException {
		// Functionality.

		// Case 0.
		File file_0 = new File(tempDir, "file_0");
		createNewFile(file_0);
		LocalFileReadDataBucket bucket_0 = new LocalFileReadDataBucket(file_0);
		bucket_0.start();

		DataOutputStream os_0 = new DataOutputStream(new FileOutputStream(file_0));
		byte[] bytes;

		os_0.writeInt(1);
		bytes = new byte[] {'a'};
		os_0.write(bytes);
		os_0.flush();
		bucket_0.skip(5);

		os_0.writeInt(2);
		bytes = new byte[] {'a', 'b'};
		os_0.write(bytes);
		os_0.flush();
		bucket_0.skip(6);

		os_0.writeInt(3);
		bytes = new byte[] {'a', 'b', 'c'};
		os_0.write(bytes);
		os_0.flush();
		assertArrayEquals(bytes, bucket_0.next());

		bucket_0.stop();
	}

	@Test(expected = IOException.class)
	public void testSkip_1() throws IOException {
		// Exception.

		// Case 0.
		File file_0 = new File(tempDir, "file_0");
		createNewFile(file_0);
		LocalFileReadDataBucket bucket_0 = new LocalFileReadDataBucket(file_0);
		bucket_0.start();

		try {
			bucket_0.skip(-1);
		} catch (IOException io) {
			throw io;
		} finally {
			bucket_0.stop();
		}
	}

	@After
	public void after() {
		try {
			FileUtils.deleteDirectory(tempDir);
		} catch (IOException io) {
			throw new RuntimeException("failed to delete temp directory.");
		}
	}

	protected void createNewFile(File file) throws IOException {
		if (!file.createNewFile()) {
			throw new RuntimeException("failed to create new file.");
		}
	}
}