package com.dianping.puma.core.storage.backup;

import com.dianping.puma.core.exception.BackupException;
import com.dianping.puma.core.exception.StorageCleanException;
import com.dianping.puma.core.storage.backup.strategy.AlwaysDeleteStrategy;
import com.dianping.puma.core.storage.backup.strategy.DeleteStrategy;
import com.dianping.puma.core.storage.backup.strategy.ExpiredDeleteStrategy;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;

public class LocalFileBasedBackup implements Backup {

	private static final Logger LOG = LoggerFactory.getLogger(LocalFileBasedBackup.class);

	File workingBaseFolder;

	File backupBaseFolder;

	int backupDay;

	@PostConstruct
	public void init() {
		try {
			FileUtils.forceMkdir(backupBaseFolder);
		} catch (IOException e) {
			throw new RuntimeException(
					String.format("Create backup base directory error: %s.", e.getMessage()));
		}
	}

	public void backup(String folderName) throws BackupException {
		try {
			File backupFolder = new File(backupBaseFolder.getAbsolutePath(), folderName);
			File workingFolder = new File(workingBaseFolder.getAbsolutePath(), folderName);
			deleteFolder(backupFolder, new AlwaysDeleteStrategy());
			FileUtils.moveDirectoryToDirectory(workingFolder, backupFolder, true);
		} catch (Exception e) {
			throw new BackupException(String.format("Backup error: %s.", e.getMessage()));
		}
	}

	private void deleteFolder(File folderToDelete, DeleteStrategy deleteStrategy) throws StorageCleanException {
		if (deleteStrategy.canDelete(folderToDelete)) {
			try {
				FileUtils.deleteDirectory(folderToDelete);
			} catch (Exception e) {
				throw new StorageCleanException(
						String.format("Delete folder error: %s.", e.getMessage()));
			}
		}
	}

	@Scheduled(cron = "0/3600 * * * * ?")
	public void delete() {
		DeleteStrategy deleteStrategy = new ExpiredDeleteStrategy(backupDay);

		for (File backupFolder: FileUtils.listFiles(backupBaseFolder, null, false)) {
			if (deleteStrategy.canDelete(backupFolder)) {
				try {
					FileUtils.deleteDirectory(backupFolder);
				} catch (Exception e) {
					LOG.error("Scheduled clean backup files error.", new StorageCleanException(e));
				}
			}
		}
	}

	public void setWorkingBaseFolder(String workingBaseFolderName) {
		this.workingBaseFolder = new File(workingBaseFolderName);
	}

	public void setBackupBaseFolder(String backupBaseFolderName) {
		this.backupBaseFolder = new File(backupBaseFolderName);
	}

	public void setBackupDay(int backupDay) {
		this.backupDay = backupDay;
	}
}
