package com.dianping.puma.storage.conf;

public class GlobalStorageConfig {

	public static String masterStorageBaseDir = "/data/appdatas/puma/storage/master";

	public static String masterBucketFilePrefix = "Bucket-";

	public static int maxMasterBucketLengthMB = 1000;

	public static int maxMasterFileCount = 25;

	public static String slaveStorageBaseDir = "/data/appdatas/puma/storage/slave";

	public static String slaveBucketFilePrefix = "Bucket-";

	public static String binlogIndexBaseDir = "/data/appdatas/puma/binlogIndex";
}
