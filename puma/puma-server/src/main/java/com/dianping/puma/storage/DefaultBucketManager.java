package com.dianping.puma.storage;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.log4j.Logger;

import com.dianping.puma.core.util.PumaThreadUtils;

public class DefaultBucketManager implements BucketManager {
	private static final Logger	log		= Logger.getLogger(DefaultBucketManager.class);
	private BucketIndex			masterIndex;
	private BucketIndex			slaveIndex;

	private ArchiveStrategy		archiveStrategy;

	private volatile boolean	stopped	= false;
	private int					maxMasterFileCount;

	public DefaultBucketManager(int maxMasterFileCount, BucketIndex masterIndex, BucketIndex slaveIndex,
			ArchiveStrategy archiveStrategy) throws IOException {
		this.maxMasterFileCount = maxMasterFileCount;
		this.archiveStrategy = archiveStrategy;
		this.masterIndex = masterIndex;
		this.slaveIndex = slaveIndex;

	}

	private void checkClosed() throws IOException {
		if (stopped) {
			throw new IOException("Bucket manager has been closed.");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.puma.storage.BucketManager#close()
	 */
	@Override
	public void close() {
		stopped = true;
		masterIndex.close();
		slaveIndex.close();
	}

	@Override
	public Bucket getNextReadBucket(long seq) throws IOException {
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
	public Bucket getNextWriteBucket() throws IOException {
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
	public Bucket getReadBucket(long seq) throws IOException {
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
	public boolean hasNexReadBucket(long seq) throws IOException {
		checkClosed();
		Sequence sequence = new Sequence(seq);
		sequence.clearOffset();
		return slaveIndex.hasNexReadBucket(sequence) || masterIndex.hasNexReadBucket(sequence);

	}

	public synchronized void init() throws Exception {
		startArchiveJob();
	}

	private void startArchiveJob() {
		Thread archiveThread = PumaThreadUtils.createThread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					if (stopped) {
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

}
