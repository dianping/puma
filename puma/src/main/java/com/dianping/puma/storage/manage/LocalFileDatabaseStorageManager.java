package com.dianping.puma.storage.manage;

import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

@Service
public class LocalFileDatabaseStorageManager implements DatabaseStorageManager {

	private static final String BINLOG_INDEX_ROOT_PATH = "/data/appdatas/puma/binlogIndex/";

	private static final String BINLOG_MASTER_STORAGE_ROOT_PATH = "/data/appdatas/puma/storage/master/";

	private static final String BINLOG_SLAVE_STORAGE_ROOT_PATH = "/data/appdatas/puma/storage/slave";

	@Override
	public void delete(String database) {
		try {
			File binlogIndexFile = new File(BINLOG_INDEX_ROOT_PATH, database);
			if (binlogIndexFile.exists()) {
				FileUtils.forceDelete(binlogIndexFile);
			}

			File binlogMasterStorageFile = new File(BINLOG_MASTER_STORAGE_ROOT_PATH, database);
			if (binlogMasterStorageFile.exists()) {
				FileUtils.forceDelete(binlogMasterStorageFile);
			}

			File binlogSlaveStorageFile = new File(BINLOG_SLAVE_STORAGE_ROOT_PATH, database);
			if (binlogSlaveStorageFile.exists()) {
				FileUtils.forceDelete(binlogSlaveStorageFile);
			}

		} catch (IOException e) {
			throw new RuntimeException("failed to delete database files.", e);
		}
	}
}
