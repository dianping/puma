package com.dianping.puma.storage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import com.dianping.puma.common.SystemStatusContainer;
import com.dianping.puma.core.codec.EventCodec;
import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.util.ByteArrayUtils;
import com.dianping.puma.storage.exception.StorageClosedException;
import com.dianping.puma.storage.exception.StorageException;
import com.dianping.puma.storage.exception.StorageLifeCycleException;
import com.dianping.puma.storage.exception.StorageWriteException;

public class DefaultEventStorage implements EventStorage {
	private BucketManager						bucketManager;
	private Bucket								writingBucket;
	private EventCodec							codec;
	private List<WeakReference<EventChannel>>	openChannels		= new ArrayList<WeakReference<EventChannel>>();
	private volatile boolean					stopped				= true;
	private int									maxMasterFileCount	= 20;
	private BucketIndex							masterIndex;
	private BucketIndex							slaveIndex;
	private ArchiveStrategy						archiveStrategy;
	private String								name;

	public void start() throws StorageLifeCycleException {
		stopped = false;
		bucketManager = new DefaultBucketManager(maxMasterFileCount, masterIndex, slaveIndex, archiveStrategy);
		try {
			bucketManager.start();
		} catch (Exception e) {
			throw new StorageLifeCycleException("Storage init failed", e);
		}
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
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
	public EventChannel getChannel(long seq) throws StorageException {
		EventChannel channel = new DefaultEventChannel(bucketManager, seq, codec);
		openChannels.add(new WeakReference<EventChannel>(channel));
		return channel;
	}

	/**
	 * @param codec
	 *            the codec to set
	 */
	public void setCodec(EventCodec codec) {
		this.codec = codec;
	}

	@Override
	public synchronized void store(ChangedEvent event) throws StorageException {
		if (stopped) {
			throw new StorageClosedException("Storage has been closed.");
		}
		try {
			if (writingBucket == null) {
				writingBucket = bucketManager.getNextWriteBucket();
			} else if (!writingBucket.hasRemainingForWrite()) {
				writingBucket.stop();
				writingBucket = bucketManager.getNextWriteBucket();
			}

			event.setSeq(writingBucket.getCurrentWritingSeq());
			byte[] data = codec.encode(event);
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			bos.write(ByteArrayUtils.intToByteArray(data.length));
			bos.write(data);
			writingBucket.append(bos.toByteArray());
			bucketManager.updateLatestSequence(new Sequence(event.getSeq()));
			SystemStatusContainer.instance.updateStorageStatus(name, event.getSeq());
		} catch (IOException e) {
			throw new StorageWriteException("Failed to write event.", e);
		}
	}

	@Override
	public synchronized void stop() {
		if (stopped) {
			return;
		}
		stopped = true;
		try {
			bucketManager.stop();
		} catch (StorageLifeCycleException e1) {
			// ignore
		}
		if (writingBucket != null) {
			try {
				writingBucket.stop();
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
