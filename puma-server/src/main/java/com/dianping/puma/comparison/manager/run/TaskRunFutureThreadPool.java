package com.dianping.puma.comparison.manager.run;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class TaskRunFutureThreadPool {

	private static ExecutorService threadPool;

	static {
		threadPool = Executors.newCachedThreadPool(new ThreadFactory() {
			@Override
			public Thread newThread(Runnable runnable) {
				Thread thread = new Thread(runnable);
				thread.setName("check-task-related-thread");
				thread.setDaemon(true);
				return thread;
			}
		});
	}

	public static void execute(Runnable runnable) {
		threadPool.execute(runnable);
	}
}
