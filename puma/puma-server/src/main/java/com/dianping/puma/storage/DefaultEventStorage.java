package com.dianping.puma.storage;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import com.dianping.puma.core.codec.EventCodec;
import com.dianping.puma.core.event.ChangedEvent;

public class DefaultEventStorage implements EventStorage {
	private BucketManager						bucketManager;
	private String								localBaseDir;
	private String								name;
	private Bucket								writingBucket;
	private EventCodec							codec;
	private int									fileMaxSizeMB		= 2000;
	private String								filePrefix			= "b";
	private String								hdfsBaseDir;
	private List<WeakReference<EventChannel>>	openChannels		= new ArrayList<WeakReference<EventChannel>>();
	private volatile boolean					stopped				= false;
	private int									maxLocalFileCount	= 20;

	public void initialize() throws IOException {
		bucketManager = new DefaultBucketManager(localBaseDir, hdfsBaseDir, name, filePrefix, fileMaxSizeMB, codec,
				maxLocalFileCount);
		try {
			bucketManager.init();
		} catch (Exception e) {
			throw new IOException(e);
		}
	}

	/**
	 * @param maxLocalFileCount
	 *            the maxLocalFileCount to set
	 */
	public void setMaxLocalFileCount(int maxLocalFileCount) {
		this.maxLocalFileCount = maxLocalFileCount;
	}

	public void setHdfsBaseDir(String hdfsBaseDir) {
		this.hdfsBaseDir = hdfsBaseDir;
	}

	@Override
	public EventChannel getChannel(long seq) throws IOException {

		EventChannel channel = new DefaultEventChannel(bucketManager, seq);
		openChannels.add(new WeakReference<EventChannel>(channel));
		return channel;
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
		if (stopped) {
			throw new IOException("Storage has been closed.");
		}
		if (writingBucket == null) {
			writingBucket = bucketManager.getNextWriteBucket();
		} else if (!writingBucket.hasRemainingForWrite()) {
			writingBucket.close();
			writingBucket = bucketManager.getNextWriteBucket();
		}

		event.setSeq(writingBucket.getCurrentWritingSeq());
		writingBucket.append(event);
	}

	@Override
	public synchronized void close() {
		stopped = true;
		bucketManager.close();
		if (writingBucket != null) {
			try {
				writingBucket.close();
			} catch (IOException e) {
				// ignore
			}
		}

		for (WeakReference<EventChannel> channelRef : openChannels) {
			EventChannel channel = channelRef.get();
			if (channel != null) {
				try {
					channel.close();
				} catch (Exception e) {
					// ignore
				}
			}
		}

	}
}
