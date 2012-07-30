package com.dianping.puma.storage;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.log4j.Logger;

import com.dianping.puma.core.util.PumaThreadUtils;
import com.dianping.puma.storage.exception.StorageClosedException;

public class DefaultBucketManager implements BucketManager {
	private static final Logger	log		= Logger.getLogger(DefaultBucketManager.class);
	private BucketIndex			masterIndex;
	private BucketIndex			slaveIndex;

	private ArchiveStrategy		archiveStrategy;

	private volatile boolean	stopped	= true;
	private int					maxMasterFileCount;

	public DefaultBucketManager(int maxMasterFileCount, BucketIndex masterIndex, BucketIndex slaveIndex,
			ArchiveStrategy archiveStrategy) {
		this.maxMasterFileCount = maxMasterFileCount;
		this.archiveStrategy = archiveStrategy;
		this.masterIndex = masterIndex;
		this.slaveIndex = slaveIndex;
	}

	private void checkClosed() throws StorageClosedException {
		if (stopped) {
			throw new StorageClosedException("Bucket manager has been closed.");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.puma.storage.BucketManager#close()
	 */
	@Override
	public void close() {
		if (stopped) {
			return;
		}
		stopped = true;
		masterIndex.close();
		slaveIndex.close();
	}

	@Override
	public Bucket getNextReadBucket(long seq) throws StorageClosedException, IOException {
		checkClosed();
		Sequence sequence = new Sequence(seq);
		sequence = sequence.clearOffset();

		Bucket bucket = slaveIndex.getNextReadBucket(sequence);

		if (bucket != null) {
			return bucket;
		} else {
			bucket = masterIndex.getNextReadBucket(sequence);
			if (bucket != null) {
				return bucket;
			} else {
				throw new FileNotFoundException("No next read bucket for seq(" + seq + ")");
			}
		}
	}

	@Override
	public Bucket getNextWriteBucket() throws IOException, StorageClosedException {
		checkClosed();
		Bucket bucket = masterIndex.getNextWriteBucket();

		if (bucket != null) {
			masterIndex.add(bucket);
			return bucket;
		} else {
			throw new IOException("Can't get next writeBucket!");
		}
	}

	@Override
	public Bucket getReadBucket(long seq) throws IOException, StorageClosedException {
		checkClosed();

		Bucket bucket = slaveIndex.getReadBucket(seq);

		if (bucket != null) {
			return bucket;
		} else {
			bucket = masterIndex.getReadBucket(seq);
			if (bucket != null) {
				return bucket;
			} else {
				throw new FileNotFoundException(String.format("No matching bucket for seq(%d)!", seq));
			}
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.puma.storage.BucketManager#hasNexReadBucket(long)
	 */
	@Override
	public boolean hasNexReadBucket(long seq) throws IOException, StorageClosedException {
		checkClosed();
		Sequence sequence = new Sequence(seq);
		sequence.clearOffset();
		return slaveIndex.hasNexReadBucket(sequence) || masterIndex.hasNexReadBucket(sequence);

	}

	public synchronized void init() {
		stopped = false;
		startArchiveJob();
	}

	private void startArchiveJob() {
		Thread archiveThread = PumaThreadUtils.createThread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					try {
						checkClosed();
					} catch (StorageClosedException e1) {
						break;
					}

					try {
						archiveStrategy.archive(masterIndex, slaveIndex, maxMasterFileCount);
						Thread.sleep(5 * 1000);
					} catch (Exception e) {
						log.error("Archive Job failed.", e);
					}

				}
			}
		}, "ArchiveTask", false);

		archiveThread.start();
	}

	@Override
	public void updateLatestSequence(Sequence sequence) {
		this.masterIndex.updateLatestSequence(sequence);
	}

}
