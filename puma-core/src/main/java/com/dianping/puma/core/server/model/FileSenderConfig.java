package com.dianping.puma.core.server.model;

public class FileSenderConfig {

	private String fileSenderName;

	private String storageName;

	private String binlogIndexBaseDir;

	private String storageAcceptedTablesConfigKey;

	private String masterBucketFilePrefix;

	private int maxMasterBucketLengthMB;

	private String storageMasterBaseDir;

	private String slaveBucketFilePrefix;

	private int maxSlaveBucketLengthMB;

	private String storageSlaveBaseDir;

	private int maxMasterFileCount;

	private int preservedDay;

	public void setFileSenderName(String fileSenderName) {
		this.fileSenderName = fileSenderName;
	}

	public String getFileSenderName() {
		return fileSenderName;
	}

	public void setStorageName(String storageName) {
		this.storageName = storageName;
	}

	public String getStorageName() {
		return storageName;
	}

	public void setBinlogIndexBaseDir(String binlogIndexBaseDir) {
		this.binlogIndexBaseDir = binlogIndexBaseDir;
	}

	public String getBinlogIndexBaseDir() {
		return binlogIndexBaseDir;
	}

	public void setStorageAcceptedTablesConfigKey(
			String storageAcceptedTablesConfigKey) {
		this.storageAcceptedTablesConfigKey = storageAcceptedTablesConfigKey;
	}

	public String getStorageAcceptedTablesConfigKey() {
		return storageAcceptedTablesConfigKey;
	}

	public void setMasterBucketFilePrefix(String masterBucketFilePrefix) {
		this.masterBucketFilePrefix = masterBucketFilePrefix;
	}

	public String getMasterBucketFilePrefix() {
		return masterBucketFilePrefix;
	}

	public void setMaxMasterBucketLengthMB(int maxMasterBucketLengthMB) {
		this.maxMasterBucketLengthMB = maxMasterBucketLengthMB;
	}

	public int getMaxMasterBucketLengthMB() {
		return maxMasterBucketLengthMB;
	}

	public void setStorageMasterBaseDir(String storageMasterBaseDir) {
		this.storageMasterBaseDir = storageMasterBaseDir;
	}

	public String getStorageMasterBaseDir() {
		return storageMasterBaseDir;
	}

	public void setSlaveBucketFilePrefix(String slaveBucketFilePrefix) {
		this.slaveBucketFilePrefix = slaveBucketFilePrefix;
	}

	public String getSlaveBucketFilePrefix() {
		return slaveBucketFilePrefix;
	}

	public void setMaxSlaveBucketLengthMB(int maxSlaveBucketLengthMB) {
		this.maxSlaveBucketLengthMB = maxSlaveBucketLengthMB;
	}

	public int getMaxSlaveBucketLengthMB() {
		return maxSlaveBucketLengthMB;
	}

	public void setStorageSlaveBaseDir(String storageSlaveBaseDir) {
		this.storageSlaveBaseDir = storageSlaveBaseDir;
	}

	public String getStorageSlaveBaseDir() {
		return storageSlaveBaseDir;
	}

	public void setMaxMasterFileCount(int maxMasterFileCount) {
		this.maxMasterFileCount = maxMasterFileCount;
	}

	public int getMaxMasterFileCount() {
		return maxMasterFileCount;
	}

	public void setPreservedDay(int preservedDay) {
		this.preservedDay = preservedDay;
	}

	public int getPreservedDay() {
		return preservedDay;
	}

}
