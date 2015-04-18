package com.dianping.puma.core.util;

import java.util.concurrent.*;

public class PumaThreadPool {

	private static final int SCHEDULED_THREAD_POOL_SIZE = 10;

	private static ExecutorService daemonThreadPool = Executors.newCachedThreadPool();

	private static ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(SCHEDULED_THREAD_POOL_SIZE);

	public static void execute(Runnable runnable) {
		daemonThreadPool.execute(runnable);
	}

	public static ScheduledFuture schedule(Runnable runnable, int delay, int period, TimeUnit timeUnit) {
		return scheduledThreadPool.scheduleAtFixedRate(runnable, delay, period, timeUnit);
	}
}
