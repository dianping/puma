/**
 * Project: ${puma-common.aid}
 * 
 * File Created at 2012-6-23
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
package com.dianping.puma.core.util;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * TODO Comment of PumaThreadUtils
 * 
 * @author Leo Liang
 * 
 */
public class PumaThreadUtils {

	private static final String								PREFIX		= "Puma-thread-";

	private static List<WeakReference<Thread>>				threadList	= Collections
																				.synchronizedList(new ArrayList<WeakReference<Thread>>());
	private static ConcurrentHashMap<String, AtomicInteger>	tsakToSeq	= new ConcurrentHashMap<String, AtomicInteger>();					;

	public static Thread createThread(Runnable r, String taskName, boolean isDaemon) {
		tsakToSeq.putIfAbsent(taskName, new AtomicInteger(1));
		Thread t = new Thread(r, PREFIX + taskName + "-" + tsakToSeq.get(taskName).getAndIncrement());
		t.setDaemon(isDaemon);
		threadList.add(new WeakReference<Thread>(t));
		return t;
	}

}
