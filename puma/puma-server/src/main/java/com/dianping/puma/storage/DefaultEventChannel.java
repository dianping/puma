package com.dianping.puma.storage;

import java.io.EOFException;
import java.io.IOException;

import com.dianping.puma.core.event.ChangedEvent;

public class DefaultEventChannel implements EventChannel {
	private BucketManager		bucketManager;
	private Bucket				bucket;
	private long				seq;
	private volatile boolean	stopped	= false;

	public DefaultEventChannel(BucketManager bucketManager, long seq) throws IOException {
		this.bucketManager = bucketManager;
		bucket = bucketManager.getReadBucket(seq);
		this.seq = bucket.getStartingSequece().longValue();

	}

	@Override
	public ChangedEvent next() throws IOException, InterruptedException {
		if (stopped) {
			throw new IOException("Channel has been closed.");
		}

		ChangedEvent event = null;

		while (event == null) {
			try {
				event = bucket.getNext();
			} catch (EOFException e) {
				if (bucketManager.hasNexReadBucket(seq)) {
					bucket.close();
					bucket = bucketManager.getNextReadBucket(seq);
					seq = bucket.getStartingSequece().longValue();
				} else {
					Thread.sleep(5);
				}
			}
		}

		seq = event.getSeq();

		return event;
	}

	@Override
	public void close() throws IOException {
		stopped = true;
		bucket.close();
	}
}
