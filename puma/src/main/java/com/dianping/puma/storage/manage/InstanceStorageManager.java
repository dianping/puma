package com.dianping.puma.storage.manage;

public interface InstanceStorageManager {

	public boolean exist(String filename);

	public void delete(String instance);
}
