package com.dianping.puma.storage;

import java.io.File;
import java.io.IOException;

import com.dianping.puma.core.event.ChangedEvent;

public class DefaultEventStorage implements EventStorage {
	private BucketManager	bucketManager;
	private File			baseDir;
	private String			name;

	public void initialize() {
		bucketManager = new DefaultBucketManager(baseDir, name);
	}

	@Override
	public EventChannel getChannel(long seq) throws IOException {

		return new DefaultEventChannel(bucketManager, seq);
	}

	public void setBaseDir(String basedir) {
		this.baseDir = new File(basedir);
	}

	public void setName(String name) {
		this.name = name;
	}

}
