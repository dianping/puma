/**
 * Project: puma-server
 * 
 * File Created at 2012-7-7
 * $Id$
 * 
 * Copyright 2010 dianping.com.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Dianping Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with dianping.com.
 */
package com.dianping.puma.storage;

import org.junit.Test;

import com.dianping.puma.core.codec.JsonEventCodec;
import com.dianping.puma.core.event.DdlEvent;

/**
 * TODO Comment of EventStorageTest
 * 
 * @author Leo Liang
 * 
 */
public class EventStorageTest {

	@Test
	public void test() throws Exception {
		final DefaultEventStorage storage = new DefaultEventStorage();
		storage.setCodec(new JsonEventCodec());
		storage.setFileMaxSizeMB(2000);
		storage.setFilePrefix("dd");
		storage.setLocalBaseDir("/data/applogs/puma/");
		storage.setName("puma");
		storage.initialize();

		// while (true) {
		// try {
		// DdlEvent event = new DdlEvent();
		// event.setDatabase("cat");
		// event.setExecuteTime((new Date()).getTime());
		// event.setSql("SELECT * FROM REPORT");
		// event.setTable("");
		//
		// storage.store(event);
		// Thread.sleep(50000);
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
		// }

		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {
				long i = 0;
				while (true) {
					try {
						DdlEvent event = new DdlEvent();
						event.setDatabase("cat");
						event.setExecuteTime(i++);
						event.setSql("SELECT * FROM REPORT");
						event.setTable("");

						storage.store(event);
						Thread.sleep(5);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		});
		t.start();

		Thread.sleep(100);

		EventChannel channel = storage.getChannel(-1);
		boolean hasException = false;
		while (true) {
			try {
				if (!hasException) {
					System.out.println(channel.next());
				}
			} catch (Throwable e) {
				hasException = true;
				e.printStackTrace();
			}
		}

	}
}
