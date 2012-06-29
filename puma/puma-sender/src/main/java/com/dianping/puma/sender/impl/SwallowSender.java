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

public class SwallowSender extends AbstractSender {

	private volatile boolean			stop			= false;
	private FileQueue<DataChangedEvent>	queue;
	private FileQueueConfigHolder		config;
	private int							maxTryTimes		= 3;
	private boolean						canMissEvent	= false;

	private static final Logger			log				= Logger.getLogger(SwallowSender.class);

	public void setCanMissEvent(boolean canMissEvent) {
		this.canMissEvent = canMissEvent;
	}

	public void setMaxTryTimes(int maxTryTimes) {
		this.maxTryTimes = maxTryTimes;
	}

	public void setConfig(FileQueueConfigHolder config) {
		this.config = config;
	}

	@Override
	protected void doSend(DataChangedEvent event, PumaContext context) {
		try {
			queue.add(event);
		} catch (FileQueueClosedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public void start() throws Exception {

		// TODO
		config.setConfigName(this.name);

		queue = new DefaultFileQueueImpl<DataChangedEvent>(config, this.name, true);
		// TODO Auto-generated method stub
		PumaThreadUtils.createThread(new SwallowSenderTask(this.name, this.canMissEvent),
				"Read_from_queue_for_swallow", false).start();

		// open filequeue

	}

	@Override
	public void stop() throws Exception {

		stop = true;
		queue.close();
	}

	private class SwallowSenderTask implements Runnable {
		private String	senderName;
		private boolean	canMissEvent;

		public SwallowSenderTask(String senderName, boolean canMissEvent) {
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
						// TODO send event to swallow
						// System.out.println("Thread swallowsender name " +
						// senderName + "  read from the queue: "
						// + dataChangedEvent);
						tryTimes = 0;
					}

				} catch (Exception e) {
					tryTimes++;
					if (this.canMissEvent) {
						if (tryTimes == maxTryTimes) {
							log.error("This event failed for maxmim times: " + dataChangedEvent);
							tryTimes = 0;
						}
					} else {
						if (tryTimes % maxTryTimes == 0) {
							log.error("This event failed for maxmim times: " + dataChangedEvent);
							// TODO send email and message
						}
					}
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}

				}

				// TODO

			}
		}
	}
}