package com.dianping.puma.storage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.log4j.Logger;

import com.dianping.puma.core.datatype.BinlogPos;
import com.dianping.puma.core.datatype.BinlogPosAndSeq;
import com.dianping.puma.core.util.PumaThreadUtils;
import com.dianping.puma.storage.exception.StorageClosedException;

public class DefaultBucketManager implements BucketManager {
	private static final Logger log = Logger
			.getLogger(DefaultBucketManager.class);
	protected static final String PATH_SEPARATOR = "/";
	private BucketIndex masterIndex;
	private BucketIndex slaveIndex;
	private AtomicReference<TreeMap<BinlogPos, BinlogPosAndSeq>> binlogIndex = new AtomicReference<TreeMap<BinlogPos, BinlogPosAndSeq>>();

	private ArchiveStrategy archiveStrategy;
	private CleanupStrategy cleanupStrategy;
	private String binlogIndexBaseDir = "/data/applogs/puma/binlogindex";
	private String binlogIndexPrefix = "index-";
	private RandomAccessFile binlogindexfile;

	private volatile boolean stopped = true;

	public TreeMap<BinlogPos, BinlogPosAndSeq> getBinlogIndex() {
		return binlogIndex.get();
	}

	public void setBinlogIndex(TreeMap<BinlogPos, BinlogPosAndSeq> binlogIndex) {
		this.binlogIndex.set(binlogIndex);
	}

	public DefaultBucketManager(BucketIndex masterIndex,
			BucketIndex slaveIndex, ArchiveStrategy archiveStrategy,
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

	public synchronized void start() {
		stopped = false;
		this.binlogIndex.set(new TreeMap<BinlogPos, BinlogPosAndSeq>(
				new PathBinlogPosComparator()));
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

	private void updateStartBinlogPosIndex(Bucket bucket) {
		TreeMap<BinlogPos, BinlogPosAndSeq> newBinlogPosIndexes = new TreeMap<BinlogPos, BinlogPosAndSeq>(
				binlogIndex.get());
		newBinlogPosIndexes.put(bucket.getStartingBinlogPos(),
				new BinlogPosAndSeq(bucket.getCurrentWritingBinlogPos(), bucket
						.getStartingSequece().longValue()));
		binlogIndex.set(newBinlogPosIndexes);
	}

	@Override
	public void updateFileBinlogIndex(Bucket bucket) {
		if (binlogIndex.get().get(bucket.getStartingBinlogPos()) == null) {
			updateStartBinlogPosIndex(bucket);
		} else {
			binlogIndex.get().get(bucket.getStartingBinlogPos()).setBinlogpos(
					bucket.getCurrentWritingBinlogPos());
		}
	}

	protected String convertToPath(Sequence seq) {
		return "20" + seq.getCreationDate() + PATH_SEPARATOR
				+ binlogIndexPrefix + seq.getNumber();
	}

	@Override
	public void openBinlogIndex(Sequence seq) throws IOException {
		File bindex = new File(binlogIndexBaseDir, convertToPath(seq));
		if (!bindex.getParentFile().exists()) {
			if (!bindex.getParentFile().mkdirs()) {
				throw new IOException(String.format(
						"Can't create writeBucket's parent(%s)!", bindex
								.getParent()));
			}
		}

		this.binlogindexfile = new RandomAccessFile(bindex, "rw");
	}

	@Override
	public void writeBinlogToIndex(byte[] data) throws IOException {
		this.binlogindexfile.write(data);
	}

	@Override
	public byte[] readBinlogFromIndex() throws IOException {
		int length = this.binlogindexfile.readInt();
		byte[] data = new byte[length];
		int n = 0;
		while (n < length) {
			checkClosed();
			int count = this.binlogindexfile.read(data, 0 + n, length - n);
			n += count;
		}
		return data;
	}

	@Override
	public void binlogIndexFileclose() throws IOException {
		this.binlogindexfile.close();
		this.binlogindexfile = null;
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

	private Boolean binlogContain(BinlogPos value, BinlogPos start,
			BinlogPos end) {
		if (value.getServerId() != start.getServerId())
			return false;
		if (value.getBinlogFile().compareTo(start.getBinlogFile()) < 0
				|| value.getBinlogFile().compareTo(end.getBinlogFile()) > 0)
			return false;
		if (value.getBinlogFile().compareTo(end.getBinlogFile()) == 0
				&& value.getBinlogPosition() > end.getBinlogPosition())
			return false;
		return true;
	}

	@Override
	public Boolean getReadBinlogIndex(BinlogPos binlogpos)
			throws StorageClosedException, IOException {
		checkClosed();
		Map map = this.binlogIndex.get();
		Iterator iter = map.entrySet().iterator();
		Bucket bucket = null;
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			BinlogPos begin = (BinlogPos) entry.getKey();
			BinlogPosAndSeq value = (BinlogPosAndSeq) entry.getValue();
			BinlogPos end = value.getBinlogpos();
			if (binlogContain(binlogpos, begin, end)) {
				Sequence seq = new Sequence(value.getSeq());
				openBinlogIndex(seq);
				return true;
			}
		}
		return false;
	}

}
