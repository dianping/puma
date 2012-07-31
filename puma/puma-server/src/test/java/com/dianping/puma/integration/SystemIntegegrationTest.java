/**
 * Project: puma-server
 * 
 * File Created at 2012-7-30
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
package com.dianping.puma.integration;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import com.dianping.puma.core.util.PumaThreadUtils;

/**
 * TODO Comment of SystemIntegegrationTest
 * 
 * @author Leo Liang
 * 
 */
public class SystemIntegegrationTest extends PumaServerIntegrationBaseTest {
	private String	table	= "systemTest";

	@Before
	public void before() throws Exception {
		executeSql("DROP TABLE IF EXISTS " + table);
		executeSql("CREATE TABLE " + table + "(id INT)");
	}

	@Test
	public void testStop() throws Exception {

		// wait for last run's stop
		Thread.sleep(5 * 1000);
		startServer();
		Thread thrds[] = new Thread[PumaThreadUtils.getThreadGroup().activeCount()];
		PumaThreadUtils.getThreadGroup().enumerate(thrds);
		for (Thread t : thrds) {
			System.out.println(t.getName());
		}
		Assert.assertEquals(2, PumaThreadUtils.getThreadGroup().activeCount());
		stopServer();
		Thread.sleep(5 * 1000);
		Assert.assertEquals(0, PumaThreadUtils.getThreadGroup().activeCount());
	}

	@Test
	public void doAfter() throws Exception {
		executeSql("DROP TABLE " + table);
	}
}
