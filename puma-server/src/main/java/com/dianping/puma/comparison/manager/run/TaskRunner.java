package com.dianping.puma.comparison.manager.run;

import com.dianping.puma.biz.entity.CheckTaskEntity;

public interface TaskRunner {

	public TaskRunFuture run(CheckTaskEntity checkTask);
}
