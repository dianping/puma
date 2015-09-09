package com.dianping.puma.comparison.manager.run;

import com.google.common.util.concurrent.Uninterruptibles;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class TaskRunFuture extends FutureTask<Void> {

	private Runnable runnable;

	public TaskRunFuture(Callable<Void> callable) {
		super(callable);
	}

	public void addListener(final TaskRunFutureListener listener) {
		if (runnable != null) {
			throw new RuntimeException("failed to add more than one listener on one task run future.");
		}

		runnable = new Runnable() {
			@Override public void run() {
				try {
					Uninterruptibles.getUninterruptibly(TaskRunFuture.this);
				} catch (ExecutionException e) {
					listener.onFailure(e.getCause());
					return;
				} catch (Throwable t) {
					listener.onFailure(t);
					return;
				}

				listener.onSuccess(null);
			}
		};

		TaskRunFutureThreadPool.execute(runnable);
	}
}
