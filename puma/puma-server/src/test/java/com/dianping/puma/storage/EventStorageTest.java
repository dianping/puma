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

import java.io.IOException;

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
		DefaultEventStorage storage = new DefaultEventStorage();
		storage.setCodec(new JsonEventCodec());
		storage.setFileMaxSizeMB(50);
		storage.setFilePrefix("dd");
		storage.setLocalBaseDir("/data/applogs/puma/");
		storage.setName("puma");
		storage.initialize();

		DdlEvent event = new DdlEvent();
		event.setDatabase("cat");
		event.setExecuteTime(11111);
		event.setSql("SELECT * FROM REPORT");
		event.setTable("");

		storage.store(event);

		EventChannel channel = storage.getChannel(-1);
		while (true) {
			System.out.println(channel.next());
		}

	}
}
