package com.dianping.puma.server.container;

import com.dianping.puma.taskexecutor.TaskExecutor;

public interface TaskMerger {

	TaskExecutor tryMerge(TaskExecutor t0, TaskExecutor t1);
}
