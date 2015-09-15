package com.dianping.puma.checkserver.manager.report;

import com.dianping.puma.biz.entity.CheckTaskEntity;
import com.dianping.puma.checkserver.model.TaskResult;

public interface TaskReporter {

	public void report(CheckTaskEntity checkTask, TaskResult result);

	public void report(CheckTaskEntity checkTask, Throwable t);
}
