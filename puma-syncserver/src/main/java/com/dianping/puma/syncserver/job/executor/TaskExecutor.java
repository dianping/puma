package com.dianping.puma.syncserver.job.executor;

import com.dianping.puma.core.entity.BaseSyncTask;
import com.dianping.puma.syncserver.job.LifeCycle;
import com.dianping.puma.syncserver.job.executor.exception.TEException;

public interface TaskExecutor<T extends BaseSyncTask> extends LifeCycle<TEException> {

    T getTask();
}
