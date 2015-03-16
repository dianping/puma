package com.dianping.puma.core.backup;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

public class LocalFileBasedBackup implements Backup {

	private static final Logger LOG = LoggerFactory.getLogger(LocalFileBasedBackup.class);

	private String originDir;

	private String backupDir;

	private int backupDay;

	private static final String backupDailyDirFormat = "yyyy-MM-dd";

	private static final String backupFileSuffixFormat = "HH:mm:ss";

	public void init() throws Exception {
		File backupFolder = new File(backupDir);
		if (!(backupFolder.isDirectory() || backupFolder.mkdirs())) {
			throw new Exception("Create backup folder failure.");
		}
	}

	public void backup(String taskName) throws Exception {
		Date now = new Date();

		String backupDailyDir = (new SimpleDateFormat(backupDailyDirFormat)).format(now);
		File backupDailyFolder = new File(backupDir, backupDailyDir);
		File backupFolder = new File(backupDailyFolder.getAbsolutePath(), taskName);

		File originFolder = new File(originDir, taskName);
		String backupFileSuffix = (new SimpleDateFormat(backupFileSuffixFormat)).format(now);
		Collection<File> originFiles = FileUtils.listFiles(originFolder, null, false);
		for (File originFile: originFiles) {
			File backupFile = new File(originFile.getAbsolutePath() + "-" + backupFileSuffix);
			FileUtils.moveFile(originFile, backupFile);
			FileUtils.moveFileToDirectory(backupFile, backupFolder, true);
		}

		FileUtils.deleteDirectory(originFolder);
	}

	public void rename(String oldTaskName, String newTaskName) throws Exception {
		FileUtils.copyDirectory(new File(originDir, oldTaskName), new File(originDir, newTaskName));
		backup(oldTaskName);
	}

	public String getOriginDir() {
		return originDir;
	}

	public void setOriginDir(String originDir) {
		this.originDir = originDir;
	}

	public String getBackupDir() {
		return backupDir;
	}

	public void setBackupDir(String backupDir) {
		this.backupDir = backupDir;
	}

	public int getBackupDay() {
		return backupDay;
	}

	public void setBackupDay(int backupDay) {
		this.backupDay = backupDay;
	}
}
