package com.dianping.puma.syncserver.executor.load;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class LoadFutureThreadPool {

	private static ExecutorService threadPool;

	static {
		threadPool = Executors.newCachedThreadPool(new ThreadFactory() {
			@Override
			public Thread newThread(Runnable runnable) {
				Thread thread = new Thread(runnable);
				thread.setName("load-future");
				thread.setDaemon(true);
				return thread;
			}
		});
	}

	public static void execute(Runnable runnable) {
		threadPool.execute(runnable);
	}
}
