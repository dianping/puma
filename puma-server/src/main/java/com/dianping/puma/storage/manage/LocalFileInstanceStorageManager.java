package com.dianping.puma.storage.manage;

import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

@Service
public class LocalFileInstanceStorageManager implements InstanceStorageManager {

	private final String binlogRootPath = "/data/appdatas/puma/binlog";

	private final String binlogSuffix = ".binlog";

	@Override
	public void delete(String instance) {
		try {
			File file = new File(binlogRootPath, instance + binlogSuffix);
			FileUtils.forceDelete(file);
		} catch (IOException e) {
			throw new RuntimeException("failed to delete instance files.", e);
		}
	}
}
