package com.dianping.puma.core.util;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;

public class ScheduledExecutorUtils {

	private static final String PREFIX_NAME = "executor-";

	private static final String INFIX_MARK = "-";

	private static final String PREFIX_POOLSIZE = "-size-";

	private static List<WeakReference<ScheduledExecutorService>> scheduledExecutorServices = Collections
			.synchronizedList(new ArrayList<WeakReference<ScheduledExecutorService>>());

	public static ScheduledExecutorService createScheduledExecutorService(int poolSize, String factoryName) {
		ScheduledExecutorService executorService = Executors.newScheduledThreadPool(poolSize, new PumaThreadFactory(
				PREFIX_NAME + factoryName + PREFIX_POOLSIZE + Integer.toString(poolSize) + INFIX_MARK));
		scheduledExecutorServices.add(new WeakReference<ScheduledExecutorService>(executorService));
		return executorService;
	}

	public static ScheduledExecutorService createSingleScheduledExecutorService(String factoryName) {
		ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor(new PumaThreadFactory(
				PREFIX_NAME + factoryName + PREFIX_POOLSIZE + Integer.toString(1) + INFIX_MARK));
		scheduledExecutorServices.add(new WeakReference<ScheduledExecutorService>(executorService));
		return executorService;
	}

	private static class PumaThreadFactory implements ThreadFactory {

		private String factoryName;

		public PumaThreadFactory(String factoryName) {
			this.factoryName = factoryName;
		}

		@Override
		public Thread newThread(Runnable r) {
			return PumaThreadUtils.createThread(r, factoryName, false);
		}
	}

}
