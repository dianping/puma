package com.dianping.puma.storage;

import java.io.FileNotFoundException;
import java.io.IOException;

import com.dianping.puma.core.datatype.Pair;
import com.dianping.puma.core.event.ChangedEvent;

public class DefaultEventChannel implements EventChannel {
	private BucketManager	bucketManager;
	private Bucket			bucket;
	private long			seq;

	public DefaultEventChannel(BucketManager bucketManager, long seq) throws IOException {
		this.bucketManager = bucketManager;

		bucket = bucketManager.getBucket(seq);
		this.seq = seq;

	}

	@Override
	public ChangedEvent next() throws IOException, InterruptedException {
		ChangedEvent event = bucket.getNext();

		if (event == null) {
			try {
				bucket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

			while (event == null) { // it means end of this bucket
				try {
					Pair<Bucket, Long> result = bucketManager.getNextBucket(seq);
					seq = result.getSecond();
					bucket = result.getFirst();

					event = bucket.getNext();
				} catch (FileNotFoundException e) {
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
