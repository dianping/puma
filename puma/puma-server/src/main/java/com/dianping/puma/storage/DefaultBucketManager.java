package com.dianping.puma.storage;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.Comparator;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.dianping.puma.core.datatype.BinlogPos;
import com.dianping.puma.core.datatype.BinlogPosAndSeq;
import com.dianping.puma.core.util.PumaThreadUtils;
import com.dianping.puma.storage.exception.StorageClosedException;
import com.dianping.puma.storage.exception.StorageLifeCycleException;

public class DefaultBucketManager implements BucketManager {
	private static final Logger log = Logger
			.getLogger(DefaultBucketManager.class);
	private BucketIndex masterIndex;
	private BucketIndex slaveIndex;
	private BinlogIndexManager binlogIndexManager;
	private ArchiveStrategy archiveStrategy;
	private CleanupStrategy cleanupStrategy;

	private volatile boolean stopped = true;

	public TreeMap<BinlogPos, BinlogPosAndSeq> getBinlogIndex() {
		return binlogIndexManager.getBinlogIndex();
	}

	public void setBinlogIndex(TreeMap<BinlogPos, BinlogPosAndSeq> binlogIndex) {
		binlogIndexManager.setBinlogIndex(binlogIndex);
	}

	public DefaultBucketManager(BucketIndex masterIndex,
			BucketIndex slaveIndex, ArchiveStrategy archiveStrategy,
			CleanupStrategy cleanupStrategy) {
		this.archiveStrategy = archiveStrategy;
		this.cleanupStrategy = cleanupStrategy;
		this.masterIndex = masterIndex;
		this.slaveIndex = slaveIndex;
		binlogIndexManager = new BinlogIndexManager(masterIndex.getBucketFilePrefix());
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
	public Bucket getNextReadBucket(long seq) throws StorageClosedException,
			IOException {
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
				throw new FileNotFoundException("No next read bucket for seq("
						+ seq + ")");
			}
		}
	}

	@Override
	public Bucket getNextWriteBucket() throws IOException,
			StorageClosedException {
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
	public Bucket getReadBucket(long seq) throws IOException,
			StorageClosedException {
		checkClosed();

		Bucket bucket = slaveIndex.getReadBucket(seq);

		if (bucket != null) {
			return bucket;
		} else {
			bucket = masterIndex.getReadBucket(seq);
			if (bucket != null) {
				return bucket;
			} else {
				throw new FileNotFoundException(String.format(
						"No matching bucket for seq(%d)!", seq));
			}
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.puma.storage.BucketManager#hasNexReadBucket(long)
	 */
	@Override
	public boolean hasNexReadBucket(long seq) throws IOException,
			StorageClosedException {
		checkClosed();
		Sequence sequence = new Sequence(seq);
		sequence.clearOffset();
		return slaveIndex.hasNexReadBucket(sequence)
				|| masterIndex.hasNexReadBucket(sequence);

	}

	public synchronized void start() throws StorageLifeCycleException {
		stopped = false;
		try {
			this.binlogIndexManager.start();
		} catch (IOException e) {
			throw new StorageLifeCycleException("Storage init failed", e);
		}
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
						cleanupStrategy.cleanup(slaveIndex, binlogIndexManager);
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

	@Override
	public void updateFileBinlogIndex(Bucket bucket) {
		binlogIndexManager.updateFileBinlogIndex(bucket);
	}

	@Override
	public void openBinlogIndex(Sequence seq) throws IOException {
		binlogIndexManager.openBinlogIndex(seq);
	}

	@Override
	public void writeBinlogToIndex(byte[] data) throws IOException {
		binlogIndexManager.writeBinlogToIndex(data);
	}

	@Override
	public byte[] readBinlogFromIndex() throws IOException {
		return binlogIndexManager.readBinlogFromIndex();
	}

	@Override
	public void binlogIndexFileclose() throws IOException {
		binlogIndexManager.binlogIndexFileclose();
	}

	protected static class PathBinlogPosComparator implements
			Comparator<BinlogPos>, Serializable {

		private static final long serialVersionUID = -350477869152651536L;

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		@Override
		public int compare(BinlogPos o1, BinlogPos o2) {
			if (o1.getServerId() < o2.getServerId()) {
				return -1;
			} else if (o1.getServerId() == o2.getServerId()) {
				if (o1.getBinlogFile().compareTo(o2.getBinlogFile()) < 0) {
					return -1;
				} else if (o1.getBinlogFile().compareTo(o2.getBinlogFile()) == 0) {
					if (o1.getBinlogPosition() < o2.getBinlogPosition()) {
						return -1;
					} else if (o1.getBinlogPosition() == o2.getBinlogPosition()) {
						return 0;
					} else {
						return 1;
					}
				} else {
					return 1;
				}
			} else {
				return 1;
			}
		}

	}

	@Override
	public Boolean getReadBinlogIndex(BinlogPos binlogpos)
			throws StorageClosedException, IOException {
		checkClosed();
		return binlogIndexManager.getReadBinlogIndex(binlogpos);
	}

	@Override
	public void wirteMainBinlogIndex(byte[] data) throws IOException {

	}

}
