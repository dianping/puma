package com.dianping.puma.syncserver.executor.load;

import com.dianping.puma.syncserver.exception.PumaException;
import com.google.common.util.concurrent.Uninterruptibles;

import java.util.concurrent.*;

public class LoadFuture extends FutureTask<Integer> {

	private Runnable runnable;

	public LoadFuture(Callable<Integer> callable) {
		super(callable);
	}

	public void addListener(final LoadFutureListener loadFutureListener) {
		if (runnable != null) {
			throw new PumaException("add load future listener failure, already added.");
		}

		runnable = new Runnable() {
			@Override
			public void run() {
				Integer result;

				try {
					result = Uninterruptibles.getUninterruptibly(LoadFuture.this);
				} catch (ExecutionException e) {
					loadFutureListener.onFailure(e.getCause());
					return;
				} catch (Exception e) {
					loadFutureListener.onFailure(e);
					return;
				}

				loadFutureListener.onSuccess(result);
			}
		};

		LoadFutureThreadPool.execute(runnable);
	}
}
