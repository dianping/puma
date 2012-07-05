package com.dianping.puma.storage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class DefaultBucketManager implements BucketManager {
	private File baseDir;

	private String name;

	public DefaultBucketManager(File baseDir, String name) {
		this.baseDir = baseDir;
		this.name = name;
	}

	@Override
	public Bucket getBucket(int fileNo) throws IOException {
		String path = getBucketPath(fileNo);
		File file = new File(baseDir, path);

		if (file.isFile()) {
			return new FileBucket(file);
		} else {
			// TODO check HDFS bucket
			throw new FileNotFoundException(String.format("Bucket(%s) not found!", file.getCanonicalFile()));
		}
	}

	public String getBucketPath(int fileNo) {
		// sample: MainDB/20120705/b001
		return String.format("%s/20%06d/b%03d", name, fileNo / 1000, fileNo % 1000);
	}
}
