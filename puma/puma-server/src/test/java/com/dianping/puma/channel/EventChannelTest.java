package com.dianping.puma.channel;

import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.springframework.util.Assert;

public class EventChannelTest {
	@Test
	public void test() {
		EventChannelManager manager = DefaultEventChannelManager.INSTANCE;
		EventChannel channel = manager.getChannel(0);
		int count = 10;

		try {
			while (count-- > 0) {
				RawEvent event = channel.next(5, TimeUnit.MILLISECONDS);

				Assert.notNull(event);
			}
		} catch (InterruptedException e) {
			// ignore it
		} finally {
			channel.close();
		}
	}
}
