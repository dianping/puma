package com.dianping.puma.storage.maintain.clean;

import com.dianping.puma.storage.filesystem.FileSystem;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

@Service
public final class ScheduledDeleteService implements DeleteService {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	DeleteStrategy deleteStrategy;

	@Override
	public void delete() {
		File[] l2DateDirs = FileSystem.visitL2IndexDateDirs();
		for (File l2DateDir: l2DateDirs) {
			delete(l2DateDir);
		}

		File[] dataDateDirs = FileSystem.visitMasterDataDateDirs();
		for (File dataDateDir: dataDateDirs) {
			delete(dataDateDir);
		}
	}

	protected void delete(File directory) {
		if (deleteStrategy.canClean(directory)) {
			try {
				deleteDirectory(directory);
			} catch (IOException ignore) {
			}
		}
	}

	protected void deleteDirectory(File directory) throws IOException {
		try {
			FileUtils.deleteDirectory(directory);
		} catch (FileNotFoundException ignore) {
		}
	}

	@Scheduled(cron = "* * 1 * * ?")
	public void scheduledDelete() {
		try {
			logger.info("Starting scheduled deleting...");
			delete();
		} catch (Throwable e) {
			logger.error("Scheduled deleting expired files is error.", e);
		}
	}
}
