package com.dianping.puma.storage.maintain.clean;

import com.dianping.puma.storage.filesystem.FileSystem;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

@Service
public final class ScheduledDeleteService implements DeleteService {

	@Autowired
	FileSystem fileSystem;

	@Autowired
	DeleteStrategy deleteStrategy;

	@Override
	public void delete() {
		File[] l2DateDirs = fileSystem.visitL2IndexDateDirs();
		for (File l2DateDir: l2DateDirs) {
			delete(l2DateDir);
		}

		File[] dataDateDirs = fileSystem.visitMasterDataDateDirs();
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

	@Scheduled(cron = "0 0 2 * * *")
	public void scheduledDelete() {
		delete();
	}
}
