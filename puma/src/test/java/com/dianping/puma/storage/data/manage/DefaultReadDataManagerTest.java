package com.dianping.puma.storage.data.manage;

import com.dianping.puma.storage.Sequence;
import com.dianping.puma.storage.data.manage.DefaultReadDataManager;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.*;

import static org.junit.Assert.*;

public class DefaultReadDataManagerTest {

	private DefaultReadDataManager manager;

	private File masterFolder;

	private File slaveFolder;

	@Before
	public void before() throws IOException {
		masterFolder = new File(System.getProperty("java.io.tmpdir"), "test/master");
		FileUtils.deleteDirectory(masterFolder);
		createNewFolder(masterFolder);

		slaveFolder = new File(System.getProperty("java.io.tmpdir"), "test/slave");
		FileUtils.deleteDirectory(slaveFolder);
		createNewFolder(slaveFolder);

		createNewFolder(new File(masterFolder, "puma"));
		createNewFolder(new File(slaveFolder, "puma"));
		createNewFolder(new File(masterFolder, "puma/20151009"));
		createNewFolder(new File(masterFolder, "puma/20151010"));
		createNewFolder(new File(masterFolder, "puma/20151011"));
		createNewFolder(new File(slaveFolder, "puma/20151009"));
		createNewFolder(new File(slaveFolder, "puma/20151010"));
		createNewFolder(new File(slaveFolder, "puma/20151011"));

		manager = new DefaultReadDataManager(masterFolder.getAbsolutePath(), slaveFolder.getAbsolutePath(), "puma");
		manager.start();
	}

	@Test
	public void testNext() throws IOException {
		File file_4 = new File(slaveFolder, "puma/20151009/Bucket-0");
		createNewFile(file_4);
		DataOutputStream os_4 = new DataOutputStream(new FileOutputStream(file_4));
		os_4.writeInt(2);
		os_4.write(new byte[]{'m', 'n'});
		os_4.flush();

		File file_5 = new File(slaveFolder, "puma/20151009/Bucket-1");
		createNewFile(file_5);
		DataOutputStream os_5 = new DataOutputStream(new FileOutputStream(file_5));
		os_5.writeInt(2);
		os_5.write(new byte[]{'o', 'p'});
		os_5.flush();

		File file_0 = new File(masterFolder, "puma/20151009/Bucket-2");
		createNewFile(file_0);
		DataOutputStream os_0 = new DataOutputStream(new FileOutputStream(file_0));
		os_0.writeInt(2);
		os_0.write(new byte[] {'a', 'b'});
		os_0.flush();

		File file_1 = new File(masterFolder, "puma/20151009/Bucket-3");
		createNewFile(file_1);
		DataOutputStream os_1 = new DataOutputStream(new FileOutputStream(file_1));
		os_1.writeInt(2);
		os_1.write(new byte[] { 'c', 'd' });
		os_1.writeInt(1);
		os_1.write(new byte[]{'e'});
		os_1.flush();

		File file_2 = new File(masterFolder, "puma/20151010/Bucket-0");
		createNewFile(file_2);
		DataOutputStream os_2 = new DataOutputStream(new FileOutputStream(file_2));
		os_2.writeInt(3);
		os_2.write(new byte[]{'f', 'g', 'h'});
		os_2.flush();

		File file_3 = new File(masterFolder, "puma/20151010/Bucket-10");
		createNewFile(file_3);
		DataOutputStream os_3 = new DataOutputStream(new FileOutputStream(file_3));
		os_3.writeInt(4);
		os_3.write(new byte[]{'i', 'j', 'k', 'l'});
		os_3.flush();

		manager.open(new Sequence(20151009, 0, 0));
		assertArrayEquals(new byte[]{'m', 'n'}, manager.next());
		assertArrayEquals(new byte[]{'o', 'p'}, manager.next());
		assertArrayEquals(new byte[]{'a', 'b'}, manager.next());
		assertArrayEquals(new byte[]{'c', 'd'}, manager.next());
		assertArrayEquals(new byte[]{'e'}, manager.next());
		assertArrayEquals(new byte[]{'f', 'g', 'h'}, manager.next());
		assertArrayEquals(new byte[]{'i', 'j', 'k', 'l'}, manager.next());
	}

	@After
	public void after() throws IOException {
		manager.stop();

		FileUtils.deleteDirectory(masterFolder);
		FileUtils.deleteDirectory(slaveFolder);
	}

	protected void createNewFolder(File file) throws IOException {
		if (!file.mkdirs()) {
			throw new RuntimeException("failed to create new folder.");
		}
	}

	protected void createNewFile(File file) throws IOException {
		if (!file.createNewFile()) {
			throw new RuntimeException("failed to create new file.");
		}
	}

}