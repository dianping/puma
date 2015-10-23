package com.dianping.puma.storage.manage;

import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

@Service
public class LocalFileInstanceStorageManager implements InstanceStorageManager {

	private static final String BINLOG_ROOT_PATH = "/data/appdatas/puma/binlog";

	private static final String BINLOG_SUFFIX = ".binlog";

	@Override
	public boolean exist(String filename) {
		File file = new File(BINLOG_ROOT_PATH, filename + BINLOG_SUFFIX);
		return file.exists();
	}

	@Override
	public void delete(String instance) {
		try {
			File file = new File(BINLOG_ROOT_PATH, instance + BINLOG_SUFFIX);
			if (file.exists()) {
				FileUtils.forceDelete(file);
			}
		} catch (IOException e) {
			throw new RuntimeException("failed to delete instance files.", e);
		}
	}
}
