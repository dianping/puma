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

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.event.RowChangedEvent;
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

//	@Test
//	public void testResumeAtStopPoint() throws Exception {
//		test(new TestLogic() {
//
//			@Override
//			public void doLogic() throws Exception {
//				final AtomicInteger counter = new AtomicInteger(0);
//				final AtomicBoolean stop = new AtomicBoolean(false);
//				final CountDownLatch startLatch = new CountDownLatch(1);
//				final CountDownLatch endLatch = new CountDownLatch(1);
//				Thread producerThread = new Thread(new Runnable() {
//
//					@Override
//					public void run() {
//						try {
//							startLatch.await();
//						} catch (InterruptedException e1) {
//							e1.printStackTrace();
//						}
//						while (!stop.get()) {
//							try {
//								executeSql("INSERT INTO " + table + " values (" + counter.getAndAdd(1) + ")");
//							} catch (Exception e) {
//								e.printStackTrace();
//							}
//						}
//
//						endLatch.countDown();
//					}
//				});
//				producerThread.setName("ProducerThread");
//				producerThread.start();
//
//				Thread opThread = new Thread(new Runnable() {
//
//					@Override
//					public void run() {
//						try {
//							startLatch.await();
//							Thread.sleep(10 * 1000);
//						} catch (InterruptedException e1) {
//							e1.printStackTrace();
//						}
//
//						try {
//
//							server.stop();
//						} catch (Exception e) {
//							e.printStackTrace();
//						}
//
//						try {
//							Thread.sleep(10 * 1000);
//							sender.start();
//							storage.initialize();
//							masterIndex.init();
//							slaveIndex.init();
//							PumaThreadUtils.createThread(new Runnable() {
//
//								@Override
//								public void run() {
//									try {
//										server.start();
//									} catch (Exception e) {
//										// TODO Auto-generated catch block
//										e.printStackTrace();
//									}
//								}
//							}, "RestartServer", false).start();
//
//							Thread.sleep(10 * 1000);
//							stop.set(true);
//						} catch (Exception e) {
//							e.printStackTrace();
//						}
//					}
//				});
//				opThread.setName("OPThread");
//				opThread.start();
//
//				startLatch.countDown();
//
//				endLatch.await();
//
//				System.out.println("Rows: " + counter.get());
//				List<ChangedEvent> events = getEvents(counter.get(), false);
//				for (int i = 0; i < counter.get(); i++) {
//					Assert.assertEquals(RowChangedEvent.INSERT, ((RowChangedEvent) events.get(i)).getActionType());
//					Assert.assertEquals(table, ((RowChangedEvent) events.get(i)).getTable());
//					Assert.assertEquals(host + ":" + port, ((RowChangedEvent) events.get(i)).getMasterUrl());
//					Assert.assertEquals(db, ((RowChangedEvent) events.get(i)).getDatabase());
//					Assert.assertEquals(i, ((RowChangedEvent) events.get(i)).getColumns().get("id").getNewValue());
//				}
//			}
//		});
//	}

	public void doAfter() throws Exception {
		executeSql("DROP TABLE " + table);
	}
}
