package com.dianping.puma.biz.storage.backup;

import com.dianping.puma.core.exception.BackupException;

public interface Backup {

	void backup(String taskName) throws BackupException;
}
