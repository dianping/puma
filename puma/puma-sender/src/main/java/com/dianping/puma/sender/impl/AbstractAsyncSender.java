/**
 * Project: ${puma-sender.aid}
 * 
 * File Created at 2012-6-30
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
package com.dianping.puma.sender.impl;

import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.dianping.filequeue.DefaultFileQueueConfig.FileQueueConfigHolder;
import com.dianping.filequeue.DefaultFileQueueImpl;
import com.dianping.filequeue.FileQueue;
import com.dianping.filequeue.FileQueueClosedException;
import com.dianping.puma.client.DataChangedEvent;
import com.dianping.puma.common.bo.PumaContext;
import com.dianping.puma.common.util.PumaThreadUtils;

/**
 * TODO Comment of AbstractAsyncSender
 * 
 * @author Leo Liang
 * 
 */
public abstract class AbstractAsyncSender extends AbstractSender {

	private static final Logger				log	= Logger.getLogger(AbstractAsyncSender.class);
	protected FileQueue<DataChangedEvent>	queue;
	protected FileQueueConfigHolder			config;

	public void setConfig(FileQueueConfigHolder config) {
		this.config = config;
	}

	@Override
	protected void doSend(DataChangedEvent event, PumaContext context) {
		try {
			queue.add(event);
		} catch (FileQueueClosedException e) {
			log.error("Send failed. Sender Name: " + this.name, e);
		}

	}

	@Override
	public void stop() throws Exception {
		stop = true;
		queue.close();
	}

	@Override
	public void start() throws Exception {

		config.setConfigName(this.name);

		queue = new DefaultFileQueueImpl<DataChangedEvent>(config, this.name, true);
		PumaThreadUtils.createThread(new AsyncSenderTask(this.name, this.canMissEvent), "AsyncSender_" + this.name,
				false).start();

	}

	protected abstract void doAsyncSend(DataChangedEvent dataChangedEvent) throws Exception;

	private class AsyncSenderTask implements Runnable {
		private String	senderName;
		private boolean	canMissEvent;

		public AsyncSenderTask(String senderName, boolean canMissEvent) {
			this.senderName = senderName;
			this.canMissEvent = canMissEvent;
		}

		@Override
		public void run() {
			DataChangedEvent dataChangedEvent = null;
			int tryTimes = 0;

			while (!stop) {
				try {
					if (tryTimes == 0) {
						dataChangedEvent = queue.get(1, TimeUnit.SECONDS);
					}
					if (dataChangedEvent != null) {
						doAsyncSend(dataChangedEvent);
						tryTimes = 0;
					}

				} catch (Throwable e) {
					tryTimes++;
					if (this.canMissEvent) {
						if (tryTimes == maxTryTimes) {
							log.error("Sender {" + senderName + "} send event failed for maxmim times: "
									+ dataChangedEvent, e);
							tryTimes = 0;
						}
					} else {
						if (tryTimes % maxTryTimes == 0) {
							log.error("Sender {" + senderName + "} send event failed for maxmim times: "
									+ dataChangedEvent, e);
						}
					}
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e1) {
					}

				}

			}
		}
	}

}
