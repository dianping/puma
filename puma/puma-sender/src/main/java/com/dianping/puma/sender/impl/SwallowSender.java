package com.dianping.puma.sender.impl;

import com.dianping.filequeue.DefaultFileQueueImpl;
import com.dianping.filequeue.FileQueue;
import com.dianping.filequeue.FileQueueClosedException;
import com.dianping.filequeue.DefaultFileQueueConfig.FileQueueConfigHolder;
import com.dianping.puma.client.DataChangedEvent;
import com.dianping.puma.common.bo.PumaContext;
import com.dianping.puma.common.util.PumaThreadUtils;

public class SwallowSender extends AbstractSender {

	private volatile boolean			stop	= false;
	private FileQueue<DataChangedEvent>	queue;
	private FileQueueConfigHolder		config;

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
		PumaThreadUtils.createThread(new SwallowSenderTask(this.name), "Read_from_queue_for_swallow", false).start();

		// open filequeue

	}

	@Override
	public void stop() throws Exception {

		stop = true;
		queue.close();
	}

	private class SwallowSenderTask implements Runnable {
		private String	senderName;

		public SwallowSenderTask(String senderName) {
			this.senderName = senderName;
		}

		@Override
		public void run() {

			try {
				while (!stop) {
					System.out.println("Thread swallowsender name: " + senderName + " read from the queue: "
							+ queue.get());
				}

			} catch (Exception e) {
				// TODO
			}
		}
	}
}