package com.dianping.puma.storage.filesystem;

import com.dianping.puma.storage.StorageBaseTest;
import com.google.common.collect.Lists;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class FileSystemTest extends StorageBaseTest {

	@Override @Before
	public void setUp() throws Exception {
		super.setUp();
	}

	@Test
	public void testParseNumber() {
		assertEquals(0, FileSystem.parseNumber(new File(testDir, "/puma/20151010/bucket-0.data"), "bucket-", ".data"));
		assertEquals(1, FileSystem.parseNumber(new File(testDir, "/puma/20151010/bucket-1.data"), "bucket-", ".data"));
		assertEquals(12, FileSystem.parseNumber(new File(testDir, "/puma/20151010/bucket-12.data"), "bucket-", ".data"));
	}

	@Test
	public void testParseDate() {
		assertEquals("20151010", FileSystem.parseDate(new File(testDir, "/puma/20151010/bucket-0.data")));
		assertEquals("19980910", FileSystem.parseDate(new File(testDir, "/puma/19980910/bucket-1.data")));
	}

	@Test
	public void testParseDb() {
		assertEquals("puma", FileSystem.parseDb(new File(testDir, "/puma/20151010/bucket-0.data")));
		assertEquals("test", FileSystem.parseDb(new File(testDir, "/test/20151010/bucket-0.data")));
	}

	@Test
	public void testMaxFileNumber() throws IOException {
		File file0 = new File(testDir, "/puma/20151010/bucket-0.data");
		createFile(file0);

		File file1 = new File(testDir, "/puma/20151010/bucket-1.data");
		createFile(file1);

		File file2 = new File(testDir, "/puma/20151010/bucket-10.data");
		createFile(file2);

		File file3 = new File(testDir, "/puma/20151010/bucket-7.data");
		createFile(file3);

		assertEquals(10, FileSystem.maxFileNumber(testDir.getAbsolutePath(), "puma", "20151010", "bucket-", ".data"));
	}

	@Test
	public void testVisitFile() throws IOException {
		File file0 = new File(testDir, "/puma/20151010/bucket-0.data");
		createFile(file0);

		File file1 = new File(testDir, "/puma/20151010/bucket-1.data");
		createFile(file1);

		File file2 = new File(testDir, "/puma/20151010/bucket-10.data");
		createFile(file2);

		File file3 = new File(testDir, "/puma/20151010/bucket-7.data");
		createFile(file3);

		assertEquals(file3, FileSystem.visitFile(testDir.getAbsolutePath(), "puma", "20151010", 7, "bucket-", ".data"));
		assertEquals(file2, FileSystem.visitFile(testDir.getAbsolutePath(), "puma", "20151010", 10, "bucket-", ".data"));
		assertEquals(file1, FileSystem.visitFile(testDir.getAbsolutePath(), "puma", "20151010", 1, "bucket-", ".data"));
		assertEquals(file0, FileSystem.visitFile(testDir.getAbsolutePath(), "puma", "20151010", 0, "bucket-", ".data"));
	}

	@Test
	public void testVisitFiles() throws Exception {
		File file0 = new File(testDir, "/puma/20151010/bucket-0.data");
		createFile(file0);

		File file1 = new File(testDir, "/puma/20151010/bucket-1.data");
		createFile(file1);

		File file2 = new File(testDir, "/puma/20151010/bucket-10.data");
		createFile(file2);

		File file3 = new File(testDir, "/puma/20151010/bucket-7.data");
		createFile(file3);

		Set<File> expected = new HashSet<File>(Lists.newArrayList(new File[] {file0, file1, file2, file3}));
		Set<File> result = new HashSet<File>(Lists.newArrayList(FileSystem.visitFiles(testDir.getAbsolutePath(), "puma", "20151010", "bucket-", ".data")));
		assertEquals(expected, result);
				FileSystem.visitFiles(testDir.getAbsolutePath(), "puma", "20151010", "bucket-", ".data");
	}

	@Override @After
	public void tearDown() throws Exception {
		super.tearDown();
	}
}