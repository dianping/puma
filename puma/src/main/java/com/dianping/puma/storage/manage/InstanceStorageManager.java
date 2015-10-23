package com.dianping.puma.storage.manage;

public interface InstanceStorageManager {

	boolean exist(String filename);

	void delete(String instance);
}
