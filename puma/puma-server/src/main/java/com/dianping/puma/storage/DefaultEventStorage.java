package com.dianping.puma.storage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import com.dianping.puma.core.codec.EventCodec;
import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.util.ByteArrayUtils;

public class DefaultEventStorage implements EventStorage {
	private BucketManager						bucketManager;
	private Bucket								writingBucket;
	private EventCodec							codec;
	private List<WeakReference<EventChannel>>	openChannels		= new ArrayList<WeakReference<EventChannel>>();
	private volatile boolean					stopped				= false;
	private int									maxMasterFileCount	= 20;
	private BucketIndex							masterIndex;
	private BucketIndex							slaveIndex;
	private ArchiveStrategy						archiveStrategy;

	public void initialize() throws IOException {
		bucketManager = new DefaultBucketManager(maxMasterFileCount, masterIndex, slaveIndex, archiveStrategy);
		try {
			bucketManager.init();
		} catch (Exception e) {
			throw new IOException(e);
		}
	}

	/**
	 * @param maxMasterFileCount
	 *            the maxMasterFileCount to set
	 */
	public void setMaxMasterFileCount(int maxMasterFileCount) {
		this.maxMasterFileCount = maxMasterFileCount;
	}

	/**
	 * @param masterIndex
	 *            the masterIndex to set
	 */
	public void setMasterIndex(BucketIndex masterIndex) {
		this.masterIndex = masterIndex;
	}

	/**
	 * @param slaveIndex
	 *            the slaveIndex to set
	 */
	public void setSlaveIndex(BucketIndex slaveIndex) {
		this.slaveIndex = slaveIndex;
	}

	/**
	 * @param archiveStrategy
	 *            the archiveStrategy to set
	 */
	public void setArchiveStrategy(ArchiveStrategy archiveStrategy) {
		this.archiveStrategy = archiveStrategy;
	}

	@Override
	public EventChannel getChannel(long seq) throws IOException {
		EventChannel channel = new DefaultEventChannel(bucketManager, seq, codec);
		openChannels.add(new WeakReference<EventChannel>(channel));
		return channel;
	}

	/**
	 * public void setName(String name) { this.name = name; }
	 * 
	 * /**
	 * 
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
		byte[] data = codec.encode(event);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		bos.write(ByteArrayUtils.intToByteArray(data.length));
		bos.write(data);
		writingBucket.append(bos.toByteArray());
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
