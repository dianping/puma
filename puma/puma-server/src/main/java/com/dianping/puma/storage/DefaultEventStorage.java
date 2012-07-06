package com.dianping.puma.storage;

import java.io.IOException;

import com.dianping.puma.core.codec.EventCodec;
import com.dianping.puma.core.event.ChangedEvent;

public class DefaultEventStorage implements EventStorage {
	private BucketManager	bucketManager;
	private String			localBaseDir;
	private String			name;
	private Bucket			writingBucket;
	private EventCodec		codec;
	private int				fileMaxSizeMB	= 2000;
	private String			filePrefix		= "b";

	public void initialize() {
		bucketManager = new DefaultBucketManager(localBaseDir, name, filePrefix, fileMaxSizeMB, codec);
	}

	@Override
	public EventChannel getChannel(long seq) throws IOException {

		return new DefaultEventChannel(bucketManager, seq);
	}

	/**
	 * @param filePrefix
	 *            the filePrefix to set
	 */
	public void setFilePrefix(String filePrefix) {
		this.filePrefix = filePrefix;
	}

	public void setLocalBaseDir(String localBaseDir) {
		this.localBaseDir = localBaseDir;
	}

	/**
	 * @param fileMaxSizeMB
	 *            the fileMaxSizeMB to set
	 */
	public void setFileMaxSizeMB(int fileMaxSizeMB) {
		this.fileMaxSizeMB = fileMaxSizeMB;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @param codec
	 *            the codec to set
	 */
	public void setCodec(EventCodec codec) {
		this.codec = codec;
	}

	@Override
	public synchronized void store(ChangedEvent event) throws IOException {
		if (writingBucket == null) {
			writingBucket = bucketManager.getNextWriteBucket();
		} else if (!writingBucket.hasRemaining()) {
			writingBucket.close();
			writingBucket = bucketManager.getNextWriteBucket();
		}

		event.setSeq(writingBucket.getCurrentSeq());
		writingBucket.append(event);
	}
}
