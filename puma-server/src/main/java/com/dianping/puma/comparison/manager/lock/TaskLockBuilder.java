package com.dianping.puma.comparison.manager.lock;

import com.dianping.puma.biz.entity.CheckTaskEntity;

public interface TaskLockBuilder {

	public TaskLock buildLocalLock(CheckTaskEntity checkTask);

	public TaskLock buildRemoteLock(CheckTaskEntity checkTask);
}
