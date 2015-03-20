package com.dianping.puma.core.backup;

public interface Backup {

	void backup(String taskName) throws Exception;

	void rename(String oldTaskName, String newTaskName) throws Exception;
}
