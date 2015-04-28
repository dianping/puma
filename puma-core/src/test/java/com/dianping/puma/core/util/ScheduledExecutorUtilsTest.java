package com.dianping.puma.core.util;

import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ScheduledExecutorUtilsTest {

	public static void main(String[] args) {

		ScheduledExecutorService executorService = ScheduledExecutorUtils.createSingleScheduledExecutorService("test");
		ScheduledExecutorTask scheduledExecutorTask = new ScheduledExecutorTask();

		Future future = executorService.scheduleWithFixedDelay(scheduledExecutorTask, 0, 3000, TimeUnit.MILLISECONDS);

		try {
			Thread.sleep(60000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (future != null && !future.isCancelled() && !future.isDone()) {
			future.cancel(true);
		}
		System.out.println("future != null" + (future != null));
		System.out.println("!future.isCancelled()" + (!future.isCancelled()));
		System.out.println("!future.isDone()" + (!future.isDone()));
		if (executorService != null && !executorService.isShutdown() && !executorService.isTerminated()) {
			executorService.shutdownNow();
		}
		
		System.out.println("executorService != null" + (future != null));
		System.out.println("!executorService.isShutdown()" + (!executorService.isShutdown()));
		System.out.println("!executorService.isTerminated()" + (!executorService.isTerminated()));

	}

	private static class ScheduledExecutorTask implements Runnable {
		static int i = 0;

		@Override
		public void run() {
			System.out.println("scheduled executor  execute." + (++i));
			Thread.currentThread().getName();
		}

	}

}
