package com.dianping.puma.storage.channel;

import java.io.EOFException;
import java.io.IOException;

import com.dianping.puma.core.codec.EventCodec;
import com.dianping.puma.core.event.Event;
import com.dianping.puma.storage.EventChannel;
import com.dianping.puma.storage.bucket.BucketManager;
import com.dianping.puma.storage.bucket.DataBucket;
import com.dianping.puma.storage.exception.InvalidSequenceException;
import com.dianping.puma.storage.exception.StorageClosedException;
import com.dianping.puma.storage.exception.StorageException;
import com.dianping.puma.storage.exception.StorageReadException;

public class DefaultEventChannelBak extends AbstractEventChannel implements EventChannel {
	private BucketManager bucketManager;

	private EventCodec codec;

	private DataBucket bucket;

	private long seq;

	private volatile boolean stopped = true;

	public DefaultEventChannelBak(BucketManager bucketManager, long seq, EventCodec codec, boolean fromNext)
	      throws StorageException {
		this.bucketManager = bucketManager;
		try {
			bucket = bucketManager.getReadBucket(seq, fromNext);
		} catch (IOException e) {
			throw new InvalidSequenceException("Invalid sequence(" + seq + ")", e);
		}
		this.codec = codec;
		this.seq = bucket.getStartingSequece().longValue();
		stopped = false;
	}

	@Override
	public Event next() throws StorageException {
		checkClosed();

		Event event = null;

		while (event == null) {
			try {
				checkClosed();
				byte[] data = bucket.getNext();
				event = codec.decode(data);
			} catch (EOFException e) {
				try {
					if (bucketManager.hasNexReadBucket(seq)) {
						bucket.stop();
						bucket = bucketManager.getNextReadBucket(seq);
						seq = bucket.getStartingSequece().longValue();
					} else {
						try {
							Thread.sleep(5);
						} catch (InterruptedException e1) {
							Thread.currentThread().interrupt();
						}
					}
				} catch (IOException ex) {
					throw new StorageReadException("Failed to read", ex);
				}
			} catch (IOException e) {
				throw new StorageReadException("Failed to read", e);
			}
		}

		seq = event.getSeq();

		return event;
	}

	private void checkClosed() throws StorageClosedException {
		if (stopped) {
			throw new StorageClosedException("Channel has been closed.");
		}
	}

	@Override
	public void close() {
		if (!stopped) {
			stopped = true;
			if (bucket != null) {
				try {
					bucket.stop();
					bucket = null;
				} catch (IOException e) {
					// ignore
				}
			}
		}
	}

	public void open() {
		if (!stopped) {
			return;
		}

		stopped = false;
		try {
			bucket.start();
		} catch (IOException e) {
			// ignore
		}
	}

	public void setBucketManager(BucketManager bucketManager) {
		this.bucketManager = bucketManager;
	}
}