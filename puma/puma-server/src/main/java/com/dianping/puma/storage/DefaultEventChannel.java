package com.dianping.puma.storage;

import java.io.FileNotFoundException;
import java.io.IOException;

import com.dianping.puma.core.event.ChangedEvent;

public class DefaultEventChannel implements EventChannel {
	private BucketManager bucketManager;

	private int lastFileNo;

	private Bucket bucket;

	public DefaultEventChannel(BucketManager bucketManager, int startFileNo, int startOffset) throws IOException {
		this.bucketManager = bucketManager;
		this.lastFileNo = startFileNo;

		bucket = bucketManager.getBucket(startFileNo);
		bucket.seek(startOffset);
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

			lastFileNo++;

			while (event == null) { // it means end of this bucket
				try {
					bucket = bucketManager.getBucket(lastFileNo);
					event = bucket.getNext();
				} catch (FileNotFoundException e) {
					Thread.sleep(1); // sleep 1 ms
				}
			}
		}

		return event;
	}

	@Override
	public void close() throws IOException {
		bucket.close();
	}
}
