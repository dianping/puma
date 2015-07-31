package com.dianping.puma.storage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import com.dianping.puma.core.codec.EventCodec;
import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.event.DdlEvent;
import com.dianping.puma.core.event.RowChangedEvent;
import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.core.util.ByteArrayUtils;
import com.dianping.puma.filter.EventFilterChain;
import com.dianping.puma.status.SystemStatusManager;
import com.dianping.puma.storage.bucket.BucketManager;
import com.dianping.puma.storage.bucket.DataBucket;
import com.dianping.puma.storage.bucket.DataBucketManager;
import com.dianping.puma.storage.bucket.DefaultBucketManager;
import com.dianping.puma.storage.exception.StorageClosedException;
import com.dianping.puma.storage.exception.StorageException;
import com.dianping.puma.storage.exception.StorageLifeCycleException;
import com.dianping.puma.storage.exception.StorageWriteException;
import com.dianping.puma.storage.index.DefaultIndexManager;
import com.dianping.puma.storage.index.IndexKeyConvertor;
import com.dianping.puma.storage.index.IndexKeyImpl;
import com.dianping.puma.storage.index.IndexManager;
import com.dianping.puma.storage.index.IndexValueConvertor;
import com.dianping.puma.storage.index.IndexValueImpl;

public class DefaultEventStorage implements EventStorage {

	private BucketManager bucketManager;

	private DataBucket writingBucket;

	private EventCodec codec;

	private volatile boolean stopped = true;

	private DataBucketManager masterBucketIndex;

	private DataBucketManager slaveBucketIndex;

	private ArchiveStrategy archiveStrategy;

	private CleanupStrategy cleanupStrategy;

	private Thread flushTask;

	private String name;

	private String taskName;

	private BinlogInfo binlogInfo;

	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

	private AtomicReference<String> lastDate = new AtomicReference<String>();

	private String binlogIndexBaseDir;

	private IndexManager<IndexKeyImpl, IndexValueImpl> indexKeyManager;

	private AtomicReference<IndexKeyImpl> lastIndexKey = new AtomicReference<IndexKeyImpl>(null);

	private AtomicReference<Long> processingServerId = new AtomicReference<Long>(null);

	private EventFilterChain storageEventFilterChain;

	/**
	 * @param binlogIndexBaseDir
	 *           the binlogIndexBaseDir to set
	 */
	public void setBinlogIndexBaseDir(String binlogIndexBaseDir) {
		this.binlogIndexBaseDir = binlogIndexBaseDir;
	}

	/**
	 * @return the masterBucketIndex
	 */
	public DataBucketManager getMasterBucketIndex() {
		return masterBucketIndex;
	}

	/**
	 * @return the slaveBucketIndex
	 */
	public DataBucketManager getSlaveBucketIndex() {
		return slaveBucketIndex;
	}

	public void start() throws StorageLifeCycleException {
		stopped = false;
		masterBucketIndex.setMaster(true);
		slaveBucketIndex.setMaster(false);
		bucketManager = new DefaultBucketManager(masterBucketIndex, slaveBucketIndex, archiveStrategy, cleanupStrategy);
		indexKeyManager = new DefaultIndexManager<IndexKeyImpl, IndexValueImpl>(binlogIndexBaseDir,
		      new IndexKeyConvertor(), new IndexValueConvertor());
		flushTask = new Thread(new Flush());
		flushTask.setName("Puma-Storage-Flush");
		flushTask.setDaemon(true);
		flushTask.start();

		cleanupStrategy.addDataIndex(indexKeyManager);

		try {
			masterBucketIndex.start();
			slaveBucketIndex.start();
			bucketManager.start();
			writingBucket = null;
			indexKeyManager.start();
		} catch (Exception e) {
			throw new StorageLifeCycleException("Storage init failed", e);
		}
	}

	public CleanupStrategy getCleanupStrategy() {
		return cleanupStrategy;
	}

	/**
	 * @param cleanupStrategy
	 *           the cleanupStrategy to set
	 */
	public void setCleanupStrategy(CleanupStrategy cleanupStrategy) {
		this.cleanupStrategy = cleanupStrategy;
	}

	/**
	 * @param name
	 *           the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public String getTaskName() {
		return taskName;
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	public BinlogInfo getBinlogInfo() {
		return binlogInfo;
	}

	public void setBinlogInfo(BinlogInfo binlogInfo) {
		this.binlogInfo = binlogInfo;
	}

	public void setMasterBucketIndex(DataBucketManager masterBucketIndex) {
		this.masterBucketIndex = masterBucketIndex;
	}

	public void setSlaveBucketIndex(DataBucketManager slaveBucketIndex) {
		this.slaveBucketIndex = slaveBucketIndex;
	}

	/**
	 * @param archiveStrategy
	 *           the archiveStrategy to set
	 */
	public void setArchiveStrategy(ArchiveStrategy archiveStrategy) {
		this.archiveStrategy = archiveStrategy;
	}

	/**
	 * @param codec
	 *           the codec to set
	 */
	public void setCodec(EventCodec codec) {
		this.codec = codec;
	}

	public void setStorageEventFilterChain(EventFilterChain storageEventFilterChain) {
		this.storageEventFilterChain = storageEventFilterChain;
	}

	@Override
	public synchronized void store(ChangedEvent event) throws StorageException {
		if (stopped) {
			throw new StorageClosedException("Storage has been closed.");
		}

		// Storage filter.
		storageEventFilterChain.reset();
		if (!storageEventFilterChain.doNext(event)) {
			return;
		}

		String nowDate = sdf.format(new Date());

		if (processingServerId.get() == null) {
			processingServerId.set(event.getBinlogInfo().getServerId());
		}

		if (lastDate.get() == null) {
			lastDate.set(nowDate);
		}

		try {
			boolean newL1Index = false;
			if (writingBucket == null) {
				writingBucket = bucketManager.getNextWriteBucket();
				newL1Index = true;
			} else if (!writingBucket.hasRemainingForWrite()) {
				writingBucket.stop();
				writingBucket = bucketManager.getNextWriteBucket();
				newL1Index = true;
			} else if (!processingServerId.get().equals(event.getBinlogInfo().getServerId())) {
				writingBucket.stop();
				writingBucket = bucketManager.getNextWriteBucket();
				processingServerId.set(event.getBinlogInfo().getServerId());
				newL1Index = true;
			} else {
				if (!lastDate.get().equals(nowDate)) {
					writingBucket.stop();
					writingBucket = bucketManager.getNextWriteBucket();
					lastDate.set(nowDate);
					newL1Index = true;
				}
			}

			Sequence newSeq = writingBucket.getCurrentWritingSeq();

			event.setSeq(newSeq.longValue());
			byte[] data = codec.encode(event);

			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			bos.write(ByteArrayUtils.intToByteArray(data.length));
			bos.write(data);
			byte[] byteArray = bos.toByteArray();
			writingBucket.append(byteArray);

			Sequence sequence = new Sequence(event.getSeq(), byteArray.length);
			updateIndex(event, newL1Index, sequence);

			bucketManager.updateLatestSequence(sequence);

			SystemStatusManager.incServerStoredBytes(getTaskName(), data.length);
			SystemStatusManager.updateServerBucket(getTaskName(), newSeq.getCreationDate(), newSeq.getNumber());
		} catch (IOException e) {
			throw new StorageWriteException("Failed to write event.", e);
		}
	}

	/**
	 * flush L2Index every second
	 * 
	 * @author damonzhu
	 *
	 */
	private class Flush implements Runnable {

		@Override
		public void run() {
			while (!Thread.currentThread().isInterrupted()) {
				if (writingBucket != null) {
					try {
						writingBucket.flush();
					} catch (IOException e) {
					}
				}

				if (indexKeyManager != null) {
					try {
						indexKeyManager.flush();
					} catch (IOException e) {
					}
				}

				try {
					TimeUnit.SECONDS.sleep(1);
				} catch (InterruptedException e) {
					break;
				}
			}
		}
	}

	private void updateIndex(ChangedEvent event, boolean newL1Index, Sequence sequence) throws IOException {
		IndexKeyImpl indexKey = new IndexKeyImpl(event.getExecuteTime(), event.getBinlogInfo().getServerId(), event
		      .getBinlogInfo().getBinlogFile(), event.getBinlogInfo().getBinlogPosition());
		if (newL1Index) {
			indexKeyManager.addL1Index(indexKey, writingBucket.getBucketFileName().replace('/', '-'));
		}

		if (lastIndexKey.get() == null || !lastIndexKey.get().equals(indexKey)) {
			IndexValueImpl l2Index = new IndexValueImpl();
			l2Index.setDatabase(event.getDatabase());
			l2Index.setTable(event.getTable());
			l2Index.setDdl(event instanceof DdlEvent);
			l2Index.setDml(event instanceof RowChangedEvent);
			l2Index.setSequence(new Sequence(sequence));
			l2Index.setIndexKey(indexKey);
			if (event instanceof RowChangedEvent) {
				RowChangedEvent rowEvent = (RowChangedEvent) event;

				l2Index.setTransactionBegin(rowEvent.isTransactionBegin());
				l2Index.setTransactionCommit(rowEvent.isTransactionCommit());
			}

			indexKeyManager.addL2Index(indexKey, l2Index);
			lastIndexKey.set(indexKey);
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

		try {
			indexKeyManager.stop();
		} catch (IOException e1) {
			// ignore
		}

		if (this.flushTask != null) {
			this.flushTask.interrupt();
		}
	}

	@Override
	public BucketManager getBucketManager() {
		return this.bucketManager;
	}

	@Override
	public IndexManager<IndexKeyImpl, IndexValueImpl> getDataIndex() {
		return this.indexKeyManager;
	}

	@Override
	public EventCodec getEventCodec() {
		return this.codec;
	}
}
