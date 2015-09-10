package com.dianping.puma.comparison.manager.run;

import com.dianping.puma.comparison.model.TaskResult;
import com.google.common.util.concurrent.FutureCallback;

public interface TaskRunFutureListener extends FutureCallback<TaskResult> {

	@Override
	public void onSuccess(TaskResult result);

	@Override
	public void onFailure(Throwable cause);

}
