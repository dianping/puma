package com.dianping.puma.storage;

import java.io.File;
import java.io.IOException;

public class DefaultEventStorage implements EventStorage {
	private BucketManager bucketManager;

	private File baseDir;

	private String name;

	public void initialize() {
		bucketManager = new DefaultBucketManager(baseDir, name);
	}

	@Override
	public EventChannel getChannel(long seq) throws IOException {
		int fileNo = (int) (seq >> 32 & 0xFFFF);
		int offset = (int) (seq & 0xFFFF);

		return new DefaultEventChannel(bucketManager, fileNo, offset);
	}

	public void setBaseDir(String basedir) {
		this.baseDir = new File(basedir);
	}

	public void setName(String name) {
		this.name = name;
	}
}
