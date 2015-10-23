package com.dianping.puma.storage.conf;

public final class GlobalStorageConfig {

	private GlobalStorageConfig() {
	}

	public static String MASTER_STORAGE_BASE_DIR = "/data/appdatas/puma/storage/master";

	public static String MASTER_BUCKET_FILE_PREFIX = "Bucket-";

	public static int MAX_MASTER_BUCKET_LENGTH_MB = 1000;

	public static int MAX_MASTER_FILE_COUNT = 25;

	public static String SLAVE_STORAGE_BASE_DIR = "/data/appdatas/puma/storage/slave";

	public static String SLAVE_BUCKET_FILE_PREFIX = "Bucket-";

	public static String BINLOG_INDEX_BASE_DIR = "/data/appdatas/puma/binlogIndex";
}
