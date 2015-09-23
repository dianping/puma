package com.dianping.puma.storage.bucket;

import java.io.FileNotFoundException;
import java.io.IOException;

import com.dianping.puma.storage.data.DataBucket;
import org.apache.log4j.Logger;

import com.dianping.puma.core.util.PumaThreadUtils;
import com.dianping.puma.storage.ArchiveStrategy;
import com.dianping.puma.storage.CleanupStrategy;
import com.dianping.puma.storage.Sequence;
import com.dianping.puma.storage.exception.StorageClosedException;

public class DefaultBucketManager implements BucketManager {
	private static final Logger log = Logger.getLogger(DefaultBucketManager.class);

	private DataBucketManager masterIndex;

	private DataBucketManager slaveIndex;

	private ArchiveStrategy archiveStrategy;

	private CleanupStrategy cleanupStrategy;

	private Thread archiveThread;

	private Thread cleanupThread;

	private volatile boolean stopped = true;

	public DefaultBucketManager(DataBucketManager masterIndex, DataBucketManager slaveIndex,
	      ArchiveStrategy archiveStrategy, CleanupStrategy cleanupStrategy) {
		this.masterIndex = masterIndex;
		this.slaveIndex = slaveIndex;
		this.archiveStrategy = archiveStrategy;
		this.cleanupStrategy = cleanupStrategy;
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

		if (this.cleanupThread != null) {
			this.cleanupThread.interrupt();
		}

		if (this.archiveThread != null) {
			this.archiveThread.interrupt();
		}
	}

	@Override
	public DataBucket getNextReadBucket(long seq) throws StorageClosedException, IOException {
		checkClosed();
		Sequence sequence = new Sequence(seq, 0);
		sequence = sequence.clearOffset();

		DataBucket bucket = slaveIndex.getNextReadBucket(sequence);

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
	public DataBucket getNextWriteBucket() throws IOException, StorageClosedException {
		checkClosed();
		DataBucket bucket = masterIndex.getNextWriteBucket();

		if (bucket != null) {
			masterIndex.add(bucket);
			return bucket;
		} else {
			throw new IOException("Can't get next writeBucket!");
		}
	}

	@Override
	public DataBucket getReadBucket(long seq, boolean fromNext) throws IOException, StorageClosedException {
		checkClosed();

		DataBucket bucket = slaveIndex.getReadBucket(seq, fromNext);

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
		Sequence sequence = new Sequence(seq, 0);
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

		archiveThread = PumaThreadUtils.createThread(new Runnable() {
			@Override
			public void run() {
				int failedCount = 0;
				while (!Thread.currentThread().isInterrupted()) {
					try {
						checkClosed();
					} catch (StorageClosedException e1) {
						break;
					}

					try {
						archiveStrategy.archive(masterIndex, slaveIndex);
						failedCount = 0;
					} catch (Exception e) {
						log.error("Archive Job failed.", e);
						failedCount = (++failedCount) % 10;
					}
					try {
						Thread.sleep((failedCount + 1) * 5000);
					} catch (InterruptedException e) {
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

		cleanupThread = PumaThreadUtils.createThread(new Runnable() {
			@Override
			public void run() {
				int failedCount = 0;
				while (!Thread.currentThread().isInterrupted()) {
					try {
						checkClosed();
					} catch (StorageClosedException e1) {
						break;
					}

					try {
						cleanupStrategy.cleanup(slaveIndex);
						failedCount = 0;
					} catch (Exception e) {
						log.error("Cleanup Job failed.", e);
						failedCount = (++failedCount) % 10;
					}
					try {
						Thread.sleep((failedCount + 1) * 5000);
					} catch (InterruptedException e) {
					}

				}
			}
		}, "CleanupTask", false);

		cleanupThread.start();
	}

	@Override
	public void updateLatestSequence(Sequence sequence) {
		this.masterIndex.updateLatestSequence(sequence);
	}
}
