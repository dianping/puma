package com.dianping.puma.api.impl;

import com.dianping.puma.api.PumaClient;
import com.dianping.puma.api.PumaClientConfig;
import com.dianping.puma.api.lock.PumaClientLock;
import com.dianping.puma.core.dto.BinlogMessage;
import com.dianping.puma.core.event.Event;
import com.google.common.collect.Lists;

import java.util.concurrent.TimeUnit;

public class DistributedClusterPumaClientMainTest {

	public static void main(String[] args) {

		PumaClient client = new PumaClientConfig()
				.setClientName("technician-client")
				.setDatabase("Profession")
				.setTables(Lists.newArrayList("Technician"))
				.setDdl(false)
				.setDml(true)
				.setTransaction(false)
				.buildClusterPumaClient();

		PumaClientLock lock = new PumaClientLock("technician-client");
		try {
			while (true) {
				// Block until lock is acquired.
				lock.lock();

				try {
					// Get binlog events.
					BinlogMessage message = client.get(1, 1, TimeUnit.SECONDS);

					// Do business logic.
					// ...
					for (Event event: message.getBinlogEvents()) {
						System.out.println(event);
					}

					// Acknowledge binlog info.
					client.ack(message.getLastBinlogInfo());
				} catch (Throwable t) {
					// Error handling.
					// ...
				}
			}
		} finally {
			// Unlock quietly.
			lock.unlockQuietly();
		}
	}
}
