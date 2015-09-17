package com.dianping.puma.checkserver.manager.run;

import com.dianping.puma.checkserver.model.TaskResult;
import com.google.common.util.concurrent.FutureCallback;

public interface TaskRunFutureListener extends FutureCallback<TaskResult> {

	@Override
	public void onSuccess(TaskResult result);

	@Override
	public void onFailure(Throwable cause);

}
