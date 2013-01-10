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
	private CleanupStrategy		cleanupStrategy;

	private volatile boolean	stopped	= true;

	public DefaultBucketManager(BucketIndex masterIndex, BucketIndex slaveIndex, ArchiveStrategy archiveStrategy,
			CleanupStrategy cleanupStrategy) {
		this.archiveStrategy = archiveStrategy;
		this.cleanupStrategy = cleanupStrategy;
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
	public void stop() {
		if (stopped) {
			return;
		}
		stopped = true;
		try {
			masterIndex.stop();
		} catch (IOException e) {
			// ignore
		}
		try {
			slaveIndex.stop();
		} catch (IOException e) {
			// ignore
		}
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
	public Bucket getReadBucket(long seq, boolean fromNext) throws IOException, StorageClosedException {
		checkClosed();

		Bucket bucket = slaveIndex.getReadBucket(seq, fromNext);

		if (bucket != null) {
			return bucket;
		} else {
			bucket = masterIndex.getReadBucket(seq, fromNext);
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

	public synchronized void start() {
		stopped = false;
		startArchiveJob();
		startCleanupJob();
	}

	private void startArchiveJob() {
		if (archiveStrategy == null) {
			return;
		}
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
						archiveStrategy.archive(masterIndex, slaveIndex);
						Thread.sleep(5 * 1000);
					} catch (Exception e) {
						log.error("Archive Job failed.", e);
					}

				}
			}
		}, "ArchiveTask", false);

		archiveThread.start();
	}

	private void startCleanupJob() {
		if (cleanupStrategy == null) {
			return;
		}
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
						cleanupStrategy.cleanup(slaveIndex);
						Thread.sleep(5 * 1000);
					} catch (Exception e) {
						log.error("Cleanup Job failed.", e);
					}

				}
			}
		}, "CleanupTask", false);

		archiveThread.start();
	}

	@Override
	public void updateLatestSequence(Sequence sequence) {
		this.masterIndex.updateLatestSequence(sequence);
	}

}
