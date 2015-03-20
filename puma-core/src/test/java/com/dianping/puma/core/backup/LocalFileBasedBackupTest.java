package com.dianping.puma.core.backup;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;

public class LocalFileBasedBackupTest {

	private final static String ORIGIN_DIR = "/data/appdatas/puma/test/binlog";

	private final static String BACKUP_DIR = "/data/appdatas/puma/test/bak";

	private final static int BACKUP_DAY = 2;

	private Backup backup;

	private final static String TASK_NAME = "taskNameTest";

	@BeforeClass
	public static void init() {
		File folder = new File(ORIGIN_DIR, TASK_NAME);
		if (!(folder.isDirectory() || folder.mkdirs())) {
			System.out.println("Create original folder failure");
		}

		File file = new File(folder.getAbsolutePath(), "binlogHolder");
		try {
			if (!file.createNewFile()) {
				System.out.println("Create original file failure.");
			}
		} catch (Exception e) {
			System.out.println("Create original file failure.");
		}
	}

	@Before
	public void before() {
		LocalFileBasedBackup localFileBasedBackup = new LocalFileBasedBackup();
		localFileBasedBackup.setOriginDir(ORIGIN_DIR);
		localFileBasedBackup.setBackupDir(BACKUP_DIR);
		localFileBasedBackup.setBackupDay(BACKUP_DAY);

		backup = localFileBasedBackup;
	}

	@Test
	public void backupTest() {
		try {
			backup.backup(TASK_NAME);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Backup failure");
		}
	}

	/*
	@Test
	public void renameTest() {
		try {
			backup.rename("taskNameTest", "newTaskNameTest");
		} catch (Exception e) {
			System.out.println("Rename failure.");
		}
	}*/
}
