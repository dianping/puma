package com.dianping.puma.storage;

import org.junit.Test;
import org.springframework.util.Assert;

import com.dianping.puma.core.event.ChangedEvent;

public class EventChannelTest {
	@Test
	public void test() throws Exception {
		DefaultEventStorage storage = new DefaultEventStorage();

		storage.setLocalBaseDir("target/puma");
		storage.setName("test");
		storage.initialize();

		EventChannel channel = storage.getChannel(0);
		int count = 10;

		while (count-- > 0) {
			ChangedEvent event = channel.next();

			Assert.notNull(event);
		}

		channel.close();
	}
}
