package com.dianping.puma.comparison.manager.report;

import com.dianping.puma.biz.entity.CheckTaskEntity;
import com.dianping.puma.comparison.TaskResult;

public interface TaskReporter {

	public void report(CheckTaskEntity checkTask, TaskResult result);

	public void report(CheckTaskEntity checkTask, Throwable t);
}
