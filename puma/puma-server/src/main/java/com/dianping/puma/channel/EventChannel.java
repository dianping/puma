package com.dianping.puma.channel;

import java.util.concurrent.TimeUnit;

public interface EventChannel {
	/**
	 * return next event from the channel.
	 * 
	 * @param timeout
	 * @param unit
	 * @return null if timeout
	 * @throws InterruptedException
	 */
	public RawEvent next(int timeout, TimeUnit unit) throws InterruptedException;

	/**
	 * close the channel.
	 */
	public void close();
}
