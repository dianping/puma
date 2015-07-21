package com.dianping.puma.status;

import java.util.Calendar;
import java.util.concurrent.atomic.AtomicInteger;

import com.dianping.puma.core.annotation.ThreadUnSafe;

@ThreadUnSafe
public class SecondBucketCounter {

	private AtomicInteger counter;

	private volatile int second;

	public SecondBucketCounter() {
		counter = new AtomicInteger(0);
		second = Calendar.getInstance().get(Calendar.SECOND);
	}

	public void increase() {
		int currentSecond = Calendar.getInstance().get(Calendar.SECOND);

		if (currentSecond == this.second) {
			counter.incrementAndGet();
		} else {
			counter.set(1);
			this.second = currentSecond;
		}
	}

	public int get() {
		int currentSecond = Calendar.getInstance().get(Calendar.SECOND);

		if (currentSecond == this.second) {
			return counter.get();
		} else {
			return 0;
		}
	}
}