package com.dianping.puma.storage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.dianping.puma.common.SystemStatusContainer;
import com.dianping.puma.core.codec.EventCodec;
import com.dianping.puma.core.datatype.BinlogInfo;
import com.dianping.puma.core.datatype.BinlogInfoAndSeq;
import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.util.ByteArrayUtils;
import com.dianping.puma.storage.exception.InvalidSequenceException;
import com.dianping.puma.storage.exception.StorageClosedException;
import com.dianping.puma.storage.exception.StorageException;
import com.dianping.puma.storage.exception.StorageLifeCycleException;
import com.dianping.puma.storage.exception.StorageWriteException;

public class DefaultEventStorage implements EventStorage {
	private BucketManager bucketManager;
	private Bucket writingBucket;
	private EventCodec codec;
	private List<WeakReference<EventChannel>> openChannels = new ArrayList<WeakReference<EventChannel>>();
	private volatile boolean stopped = true;
	private BucketIndex masterIndex;
	private BucketIndex slaveIndex;
	private BinlogIndexManager binlogIndexManager;
	private ArchiveStrategy archiveStrategy;
	private CleanupStrategy cleanupStrategy;
	private String name;
	private static final String datePattern = "yyyy-MM-dd";
	private String lastDate;

	public void start() throws StorageLifeCycleException {
		SimpleDateFormat sdf = new SimpleDateFormat(datePattern);
		lastDate = sdf.format(new Date());
		stopped = false;
		bucketManager = new DefaultBucketManager(masterIndex, slaveIndex, binlogIndexManager,
				archiveStrategy, cleanupStrategy);
		try {
			bucketManager.start();
		} catch (Exception e) {
			throw new StorageLifeCycleException("Storage init failed", e);
		}
	}

	/**
	 * @param cleanupStrategy
	 *            the cleanupStrategy to set
	 */
	public void setCleanupStrategy(CleanupStrategy cleanupStrategy) {
		this.cleanupStrategy = cleanupStrategy;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
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

	public void setBinlogIndexManager(BinlogIndexManager binlogIndexManager) {
		this.binlogIndexManager = binlogIndexManager;
	}

	@Override
	public EventChannel getChannel(long seq, long serverId, String binlogFile,
			String binlogInfo) throws StorageException {
		EventChannel channel = null;
		if (seq != -3) {
			channel = new DefaultEventChannel(bucketManager, seq, codec);
		} else {
			BinlogInfo startbinlog = new BinlogInfo(serverId, binlogFile, Long
					.valueOf(binlogInfo).longValue());
			channel = new DefaultEventChannel(bucketManager,
					TransLateBinlogInfoToSeq(startbinlog), codec);
		}
		openChannels.add(new WeakReference<EventChannel>(channel));
		return channel;
	}

	public long TransLateBinlogInfoToSeq(BinlogInfo binlogInfo)
			throws StorageException {
		try {
			long result = this.bucketManager.TranBinlogIndexToSeq(binlogInfo);
			if (result != -1) {
				return result;
			} else {
				throw new IOException();
			}
		} catch (IOException e) {
			throw new InvalidSequenceException("Invalid binlogInfo("
					+ binlogInfo + ")", e);
		}
	}

	/**
	 * @param codec
	 *            the codec to set
	 */
	public void setCodec(EventCodec codec) {
		this.codec = codec;
	}

	public void writeBinlogIndexToFile() throws IOException {
		/*
		 * byte[] binlogindexitem = codec.encode(bucketManager.getBinlogIndex()
		 * .ceilingEntry(writingBucket.getStartingBinlogInfo()));
		 */
		bucketManager.writeBinlogIndex(writingBucket.getStartingBinlogInfo());
	}

	@Override
	public synchronized void store(ChangedEvent event) throws StorageException {
		if (stopped) {
			throw new StorageClosedException("Storage has been closed.");
		}
		try {
			if (writingBucket == null) {
				writingBucket = bucketManager.getNextWriteBucket();
				bucketManager.openBinlogIndex(writingBucket
						.getStartingSequece());
			} else if (!writingBucket.hasRemainingForWrite()) {
				writeBinlogIndexToFile();
				bucketManager.binlogIndexFileclose();
				writingBucket.stop();
				writingBucket = bucketManager.getNextWriteBucket();
				bucketManager.openBinlogIndex(writingBucket
						.getStartingSequece());
			} else if (writingBucket.getCurrentWritingBinlogInfo()
					.getServerId() != event.getServerId()) {
				writeBinlogIndexToFile();
				bucketManager.binlogIndexFileclose();
				writingBucket.stop();
				writingBucket = bucketManager.getNextWriteBucket();
				bucketManager.openBinlogIndex(writingBucket
						.getStartingSequece());
			} else {
				SimpleDateFormat sdf = new SimpleDateFormat(datePattern);
				String nowDate = sdf.format(new Date());
				if (!lastDate.equals(nowDate)) {
					writeBinlogIndexToFile();
					bucketManager.binlogIndexFileclose();
					writingBucket.stop();
					writingBucket = bucketManager.getNextWriteBucket();
					bucketManager.openBinlogIndex(writingBucket
							.getStartingSequece());
					lastDate = nowDate;
				}
			}

			event.setSeq(writingBucket.getCurrentWritingSeq());
			byte[] data = codec.encode(event);
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			bos.write(ByteArrayUtils.intToByteArray(data.length));
			bos.write(data);
			writingBucket.append(bos.toByteArray());
			if (writingBucket.getStartingBinlogInfo() == null) {
				writingBucket
						.setStartingBinlogInfo(new BinlogInfo(event
								.getServerId(), event.getBinlog(), event
								.getBinlogPos()));
				writingBucket
						.setCurrentWritingBinlogInfo(new BinlogInfo(event
								.getServerId(), event.getBinlog(), event
								.getBinlogPos()));
				bucketManager.updateFileBinlogIndex(writingBucket);
				bucketManager
						.writeBinlogIndexIntoProperty(new BinlogInfoAndSeq(
								event.getServerId(), event.getBinlog(), event
										.getBinlogPos(), event.getSeq()));
			} else {
				if (!writingBucket.getCurrentWritingBinlogInfo().equals(
						new BinlogInfo(event.getServerId(), event.getBinlog(),
								event.getBinlogPos()))) {
					writingBucket.setCurrentWritingBinlogInfo(new BinlogInfo(
							event.getServerId(), event.getBinlog(), event
									.getBinlogPos()));
					bucketManager.updateFileBinlogIndex(writingBucket);
					bucketManager
							.writeBinlogIndexIntoProperty(new BinlogInfoAndSeq(
									event.getServerId(), event.getBinlog(),
									event.getBinlogPos(), event.getSeq()));
					writingBucket.setCurrentWritingBinlogInfo(new BinlogInfo(
							event.getServerId(), event.getBinlog(), event
									.getBinlogPos()));
				}
			}

			bucketManager.updateLatestSequence(new Sequence(event.getSeq()));
			SystemStatusContainer.instance.updateStorageStatus(name, event
					.getSeq());
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
