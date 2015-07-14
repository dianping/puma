/**
 * Project: puma-server
 *
 * File Created at Nov 29, 2013
 *
 */
package com.dianping.puma.storage;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import com.dianping.cat.Cat;
import com.dianping.puma.core.event.Event;
import com.dianping.puma.core.event.ServerErrorEvent;
import com.dianping.puma.storage.exception.StorageException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BufferedEventChannel implements EventChannel {

	private static final Logger logger = LoggerFactory.getLogger(BufferedEventChannel.class);

	private volatile boolean inited = false;

	private String clientName;

	private EventChannel eventChannel;

	private BlockingQueue<Event> eventBuffer;

	private Thread extractThread;

	public BufferedEventChannel(String clientName, EventChannel eventChannel, int bufSize) {
		this.clientName = clientName;
		this.eventChannel = eventChannel;
		this.eventBuffer = new ArrayBlockingQueue<Event>(bufSize);
	}

	@Override
	public void open() {
		if (inited) {
			return;
		}

		// Opening storage channel.
		eventChannel.open();

		// Starting extract thread.
		extractThread = new Thread(new ExtractTask());
		extractThread.setName(String.format("extract-thread-%s", clientName));
		extractThread.setDaemon(true);
		extractThread.start();

		inited = true;
	}

	@Override
	public void close() {
		if (!inited) {
			return;
		}

		// Stopping extract thread.
		extractThread.interrupt();
		extractThread = null;

		// Closing storage channel.
		eventChannel.close();

		inited = false;
	}

	@Override
	public Event next() {
		try {
			return eventBuffer.take();
		} catch (InterruptedException e) {
			return null;
		}
	}

	private class ExtractTask implements Runnable {

		private volatile boolean stopped = false;

		public void stop() {
			stopped = true;
			Thread.currentThread().interrupt();
		}

		@Override
		public void run() {
			while (!checkStop()) {
				try {
					eventBuffer.put(eventChannel.next());
				} catch (StorageException e) {
					try {
						eventBuffer.put(new ServerErrorEvent("storage error event"));

						String msg = String.format("Puma server channel reading storage error.");
						Exception pe = new Exception(msg, e);
						logger.error(msg, pe);
						Cat.logError(msg, pe);

						// Stop the channel.
						stop();
					} catch (InterruptedException ie) {
						Thread.currentThread().interrupt();
					}
				} catch (InterruptedException ie) {
					Thread.currentThread().interrupt();
				}
			}
		}

		private boolean checkStop() {
			return stopped || Thread.currentThread().isInterrupted();
		}
	}
}
