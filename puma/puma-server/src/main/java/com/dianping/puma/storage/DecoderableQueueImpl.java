package com.dianping.puma.storage;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.dianping.puma.core.codec.EventCodec;

public class DecoderableQueueImpl implements DecoderableQueue {

	private static final Logger logger = Logger
			.getLogger(DecoderableQueueImpl.class);

	private static final int capacity = 1024;

	private int threadCapacity = 5;

	private LinkedBlockingQueue<DecoderElement> queue = new LinkedBlockingQueue<DecoderElement>(
			capacity);

	private ExecutorService executorService;

	private EventCodec codec;

	public DecoderableQueueImpl(EventCodec codec) {
		executorService = Executors.newFixedThreadPool(threadCapacity);
		this.codec = codec;
	}

	/**
	 * <note> 该方法线程安全</note>
	 */
	@Override
	public void put(DecoderElement e) throws InterruptedException {
		queue.put(e);

		DecodeTask task = new DecodeTask(e);
		executorService.execute(task);

	}

	/**
	 * 如果queue为空，则等待timeout时间；
	 * 如果queue不为空，则判断element是否已经被decode，如果是，则返回element，否则等待timeout时间。
	 * 
	 * <note> 该方法线程不安全，只能单线程调用 </note>
	 */
	@Override
	public DecoderElement take(long timeout) throws IOException,
			InterruptedException {
		DecoderElement element = queue.peek();

		if (element != null) {
			// 如果拿到的element仍然未被decode，则在该对象上wait，直到后台线程完成decode后唤醒它或超时。
			if (!element.isDecoded()) {
				synchronized (element) {
					if (!element.isDecoded()) {
						element.wait(timeout);
					}
				}
			}

			if (element.isDecoded()) {// 拿到的element已经被decode，则可移除element
				queue.poll();
			} else {
				// 否则返回null
				element = null;
			}

		} else {
			TimeUnit.MILLISECONDS.sleep(timeout);

			element = queue.peek();

			if (element != null && element.isDecoded()) {// 拿到的element已经被decode，则可移除
				queue.poll();
			} else {
				// 否则返回null
				element = null;
			}
		}

		return element;
	}

	private class DecodeTask implements Runnable {

		private DecoderElement element;

		public DecodeTask(DecoderElement element) {
			this.element = element;
		}

		@Override
		public void run() {
			byte[] data = element.getData();

			try {

				// 对data进行decode，存放到element
				element.setChangedEvent(codec.decode(data));

			} catch (Exception e) {
				element.setDecodeErrorMsg(e.getMessage());
				logger.error("Error when decode bytes data.", e);
			} finally {
				// 无论decode失败与否，都认为完成。
				element.setDecoded(true);
				// decode完成，唤醒在take()方法上等待的线程
				synchronized (element) {
					element.notifyAll();
				}

			}
		}
	}

	@Override
	public void close() {
		executorService.shutdown();
		queue.clear();
	}
	
	@Override
	public int size() {
		return queue.size();
	}

}
