package com.dianping.puma.checkserver.manager.utils;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class ThreadPool {

	private static ExecutorService threadPool;

	static {
		threadPool = Executors.newCachedThreadPool(new ThreadFactory() {
			@Override
			public Thread newThread(Runnable runnable) {
				Thread thread = new Thread(runnable);
				thread.setName("check-task-run-future");
				thread.setDaemon(true);
				return thread;
			}
		});
	}

	public static void execute(Runnable runnable) {
		threadPool.execute(runnable);
	}

	public static void submit(Callable<?> callable) {
		threadPool.submit(callable);
	}
}
