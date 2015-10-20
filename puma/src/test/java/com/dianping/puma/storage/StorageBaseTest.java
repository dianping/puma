package com.dianping.puma.storage;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class StorageBaseTest {

	protected void createFolder(File folder) throws IOException {
		if (folder.mkdirs()) {
			throw new IOException("failed to create folder.");
		}
	}

	protected void deleteFolder(File folder) throws IOException {
		FileUtils.forceDelete(folder);
	}

	protected void createFile(File file) throws IOException {
		if (file.createNewFile()) {
			throw new IOException("failed to create file.");
		}
	}

	protected void deleteFile(File file) throws IOException {
		FileUtils.forceDelete(file);
	}
}
