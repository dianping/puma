package com.dianping.puma.storage.maintain.clean;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

@Service
public class ScheduledDeleteService implements DeleteService {

	@Autowired
	DeleteStrategy deleteStrategy;

	@Override
	public void delete() {

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
	protected void scheduledDelete() {
		delete();
	}
}
