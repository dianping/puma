package com.dianping.puma.storage.manage;

import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

@Service
public class LocalFileDatabaseStorageManager implements DatabaseStorageManager {

	private final String binlogIndexRootPath = "/data/appdatas/puma/binlogIndex/";

	private final String binlogMasterStorageRootPath = "/data/appdatas/puma/storage/master/";

	private final String binlogSlaveStorageRootPath = "/data/appdatas/puma/storage/slave";

	@Override
	public void delete(String database) {
		try {
			File binlogIndexFile = new File(binlogIndexRootPath, database);
			if (binlogIndexFile.exists()) {
				FileUtils.forceDelete(binlogIndexFile);
			}

			File binlogMasterStorageFile = new File(binlogMasterStorageRootPath, database);
			if (binlogMasterStorageFile.exists()) {
				FileUtils.forceDelete(binlogMasterStorageFile);
			}

			File binlogSlaveStorageFile = new File(binlogSlaveStorageRootPath, database);
			if (binlogSlaveStorageFile.exists()) {
				FileUtils.forceDelete(binlogSlaveStorageFile);
			}

		} catch (IOException e) {
			throw new RuntimeException("failed to delete database files.", e);
		}
	}
}
