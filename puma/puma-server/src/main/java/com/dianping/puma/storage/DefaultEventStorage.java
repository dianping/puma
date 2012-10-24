package com.dianping.puma.storage;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.dianping.puma.common.SystemStatusContainer;
import com.dianping.puma.core.codec.EventCodec;
import com.dianping.puma.core.datatype.BinlogPos;
import com.dianping.puma.core.datatype.BinlogPosAndSeq;
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
	private ArchiveStrategy archiveStrategy;
	private CleanupStrategy cleanupStrategy;
	private String name;
	private static final String datePattern = "yyyy-MM-dd";
	private String lastDate;


	public void start() throws StorageLifeCycleException {
		SimpleDateFormat sdf = new SimpleDateFormat(datePattern);
		lastDate = sdf.format(new Date());
		stopped = false;
		bucketManager = new DefaultBucketManager(masterIndex, slaveIndex,
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

	@Override
	public EventChannel getChannel(long seq, long serverId, String binlogFile,
			String binlogPos) throws StorageException {
		EventChannel channel = null;
		if (seq != -3) {
			channel = new DefaultEventChannel(bucketManager, seq, codec);
		} else {
			BinlogPos startbinlog = new BinlogPos(serverId, binlogFile, Long
					.valueOf(binlogPos).longValue());
			channel = new DefaultEventChannel(bucketManager,
					TransLateBinlogPosToSeq(startbinlog), codec);
		}
		openChannels.add(new WeakReference<EventChannel>(channel));
		return channel;
	}

	public long TransLateBinlogPosToSeq(BinlogPos binlogpos)
			throws StorageException {
		try {
			BinlogPosAndSeq binlogandseq = null;
			if (this.bucketManager.getReadBinlogIndex(binlogpos)) {
				while (true) {
					try {
						// TODO new space?
						byte[] data = bucketManager.readBinlogFromIndex();
						/*byte[] buf = new byte[1024];
						ByteArrayOutputStream datastream = new ByteArrayOutputStream();
						GZIPInputStream zip = new GZIPInputStream(
								new ByteArrayInputStream(zipdata));
						int readCount = -1;
						while ((readCount = zip.read(buf)) != -1) {
							datastream.write(buf, 0, readCount);
						}
						byte[] data = datastream.toByteArray();
						zip.close();
						datastream.close();
						event = codec.decode(data);
						*/
						binlogandseq = (BinlogPosAndSeq) codec.decode(data);
						if (binlogandseq.getBinlogpos().getServerId() == binlogpos.getServerId()
								&& binlogandseq.getBinlogpos().getBinlogFile().equals(
										binlogpos.getBinlogFile())
								&& binlogandseq.getBinlogpos().getBinlogPosition() == binlogpos.getBinlogPosition()) {
							bucketManager.binlogIndexFileclose();
							return binlogandseq.getSeq();
						}
					} catch (EOFException e) {
						bucketManager.binlogIndexFileclose();
						throw new IOException();
					}
				}
			}
		} catch (IOException e) {
			throw new InvalidSequenceException("Invalid binlogpos(" + binlogpos
					+ ")", e);
		}
		return 1;
	}

	/**
	 * @param codec
	 *            the codec to set
	 */
	public void setCodec(EventCodec codec) {
		this.codec = codec;
	}

	public void writeIndexToFile() throws IOException {
		byte[] binlogindexitem = codec.encode(bucketManager.getBinlogIndex()
				.get(writingBucket.getStartingBinlogPos()));
		bucketManager.writeBinlogToIndex(binlogindexitem);
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
				writeIndexToFile();
				bucketManager.binlogIndexFileclose();
				writingBucket.stop();
				writingBucket = bucketManager.getNextWriteBucket();
				bucketManager.openBinlogIndex(writingBucket
						.getStartingSequece());
			} else if (writingBucket.getCurrentWritingBinlogPos().getServerId() != event
					.getServerId()) {
				writeIndexToFile();
				bucketManager.binlogIndexFileclose();
				writingBucket.stop();
				writingBucket = bucketManager.getNextWriteBucket();
				bucketManager.openBinlogIndex(writingBucket
						.getStartingSequece());
			} else {
				SimpleDateFormat sdf = new SimpleDateFormat(datePattern);
				String nowDate = sdf.format(new Date());
				if (!lastDate.equals(nowDate)) {
					writeIndexToFile();
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
			if (writingBucket.getStartingBinlogPos() == null) {
				writingBucket
						.setStartingBinlogPos(new BinlogPos(
								event.getServerId(), event.getBinlog(), event
										.getBinlogPos()));
				writingBucket
						.setCurrentWritingBinlogPos(new BinlogPos(event
								.getServerId(), event.getBinlog(), event
								.getBinlogPos()));
				bucketManager.updateFileBinlogIndex(writingBucket);
			} else {
				if (!writingBucket.getCurrentWritingBinlogPos().equals(
						new BinlogPos(event.getServerId(), event.getBinlog(),
								event.getBinlogPos()))) {
					writingBucket.setCurrentWritingBinlogPos(new BinlogPos(
							event.getServerId(), event.getBinlog(), event
									.getBinlogPos()));
					bucketManager.updateFileBinlogIndex(writingBucket);
					byte[] index = codec.encode(new BinlogPosAndSeq(event
							.getServerId(), event.getBinlog(), event
							.getBinlogPos(), event.getSeq()));
					ByteArrayOutputStream indexbos = new ByteArrayOutputStream();
					indexbos.write(ByteArrayUtils.intToByteArray(index.length));
					indexbos.write(index);
					bucketManager.writeBinlogToIndex(indexbos.toByteArray());
					writingBucket.setCurrentWritingBinlogPos(new BinlogPos(
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
