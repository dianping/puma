package com.dianping.puma.syncserver.task;

import com.dianping.puma.core.LifeCycle;
import com.dianping.puma.syncserver.task.exception.TaskException;

public interface TaskExecutor extends LifeCycle<TaskException> {
}
