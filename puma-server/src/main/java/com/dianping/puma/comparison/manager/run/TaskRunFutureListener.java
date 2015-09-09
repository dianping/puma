package com.dianping.puma.comparison.manager.run;

import com.google.common.util.concurrent.FutureCallback;

public interface TaskRunFutureListener extends FutureCallback<Void> {

	@Override
	public void onSuccess(Void result);

	@Override
	public void onFailure(Throwable cause);

}
