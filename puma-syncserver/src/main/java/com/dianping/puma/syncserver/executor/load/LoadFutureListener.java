package com.dianping.puma.syncserver.executor.load;

import com.google.common.util.concurrent.FutureCallback;

public interface LoadFutureListener extends FutureCallback<Integer> {

	@Override
	public void onSuccess(Integer result);

	@Override
	public void onFailure(Throwable cause);
}
