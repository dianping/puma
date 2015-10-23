package com.dianping.puma.storage.conf;

public class GlobalStorageConfig {

	private GlobalStorageConfig() {
	}

	public static final String masterStorageBaseDir = "/data/appdatas/puma/storage/master";

	public static final String masterBucketFilePrefix = "Bucket-";

	public static final int maxMasterBucketLengthMB = 1000;

	public static final int maxMasterFileCount = 25;

	public static final String slaveStorageBaseDir = "/data/appdatas/puma/storage/slave";

	public static final String slaveBucketFilePrefix = "Bucket-";

	public static final String binlogIndexBaseDir = "/data/appdatas/puma/binlogIndex";
}
