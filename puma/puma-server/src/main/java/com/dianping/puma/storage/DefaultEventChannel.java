package com.dianping.puma.storage;

import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.dianping.puma.core.event.ChangedEvent;

public class DefaultEventChannel implements EventChannel {
	private BucketManager	bucketManager;
	private Bucket			bucket;
	private long			seq;

	public DefaultEventChannel(BucketManager bucketManager, long seq) throws IOException {
		this.bucketManager = bucketManager;

		bucket = bucketManager.getReadBucket(seq);
		this.seq = seq;

	}

	@Override
	public ChangedEvent next() throws IOException, InterruptedException {
		ChangedEvent event = null;
		try {
			event = bucket.getNext();
		} catch (EOFException e) {
			try {
				bucket.close();
			} catch (IOException ex) {
				// TODO
				e.printStackTrace();
			}

			while (event == null) { // it means end of this bucket
				try {
					bucket = bucketManager.getNextReadBucket(seq);
					seq = bucket.getStartingSequece().longValue();

					event = bucket.getNext();
				} catch (FileNotFoundException ex) {
					Thread.sleep(1); // sleep 1 ms
				}
			}
		}

		seq = event.getSeq();

		return event;
	}

	@Override
	public void close() throws IOException {
		bucket.close();
	}
}
