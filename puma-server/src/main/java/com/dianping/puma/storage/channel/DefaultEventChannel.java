package com.dianping.puma.storage.channel;

import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.dianping.puma.core.codec.EventCodec;
import com.dianping.puma.core.codec.RawEventCodec;
import com.dianping.puma.core.constant.SubscribeConstant;
import com.dianping.puma.core.event.Event;
import com.dianping.puma.storage.EventChannel;
import com.dianping.puma.storage.Sequence;
import com.dianping.puma.storage.bucket.DataBucket;
import com.dianping.puma.storage.bucket.DataBucketManager;
import com.dianping.puma.storage.bucket.LocalFileDataBucketManager;
import com.dianping.puma.storage.conf.GlobalStorageConfig;
import com.dianping.puma.storage.exception.InvalidSequenceException;
import com.dianping.puma.storage.exception.StorageClosedException;
import com.dianping.puma.storage.exception.StorageException;
import com.dianping.puma.storage.exception.StorageReadException;
import com.dianping.puma.storage.index.DefaultIndexManager;
import com.dianping.puma.storage.index.IndexBucket;
import com.dianping.puma.storage.index.IndexKeyConvertor;
import com.dianping.puma.storage.index.IndexKeyImpl;
import com.dianping.puma.storage.index.IndexManager;
import com.dianping.puma.storage.index.IndexValueConvertor;
import com.dianping.puma.storage.index.IndexValueImpl;

public class DefaultEventChannel extends AbstractEventChannel implements EventChannel {

	private IndexManager<IndexKeyImpl, IndexValueImpl> indexManager;

	private DataBucketManager slaveIndex;

	private DataBucketManager masterIndex;

	private IndexBucket<IndexKeyImpl, IndexValueImpl> indexBucket;

	private IndexBucket<IndexKeyImpl, IndexValueImpl> lastIndexBucket;

	private EventCodec codec = new RawEventCodec();

	private volatile boolean stopped = true;

	private DataBucket readDataBucket;

	private IndexKeyImpl lastIndexKey = null;

	private Sequence lastReadSequence = null;
	
	public DefaultEventChannel(String database){
		this.database = database;
	}

	@Override
	public Event next(boolean shouldSleep) throws StorageException {
		checkClosed();

		Event event = null;

		while (event == null) {
			try {
				checkClosed();
				IndexValueImpl nextL2Index = null;

				if (this.indexBucket == null) {
					this.indexBucket = this.indexManager.getNextIndexBucket(lastIndexBucket.getStartKeyIndex());

					if (indexBucket == null) {
						if (!shouldSleep) {
							return null;
						} else {
							try {
								Thread.sleep(5);

								continue;
							} catch (InterruptedException e1) {
								Thread.currentThread().interrupt();
							}
						}
					}
				}

				try {
					nextL2Index = this.indexBucket.next();
				} catch (EOFException e) {
					if (this.indexManager.hasNextIndexBucket(this.indexBucket.getStartKeyIndex())) {
						if (readDataBucket != null) {
							this.readDataBucket.stop();
							this.readDataBucket = null;
						}

						if (indexBucket != null) {
							this.lastIndexBucket = this.indexBucket;
							this.indexBucket.stop();
							this.indexBucket = null;
						}

						continue;
					}

					if (!shouldSleep) {
						return null;
					} else {
						try {
							Thread.sleep(5);

							continue;
						} catch (InterruptedException e1) {
							Thread.currentThread().interrupt();
						}
					}
				}

				Sequence sequence = nextL2Index.getSequence();

				if (this.tables != null && !this.tables.contains(nextL2Index.getTable()) && !nextL2Index.isTransaction()) {
					lastIndexKey = nextL2Index.getIndexKey();
					continue;
				}
				if (this.withDdl != nextL2Index.isDdl() && this.withDml != nextL2Index.isDml()) {
					lastIndexKey = nextL2Index.getIndexKey();
					continue;
				}
				if (!this.withTransaction && nextL2Index.isTransaction()) {
					lastIndexKey = nextL2Index.getIndexKey();
					continue;
				}

				if (readDataBucket == null) {
					lastReadSequence = sequence;
					readDataBucket = this.getReadBucket(sequence.longValue(), false);
				}

				try {
					event = codec.decode(readData(sequence));
				} catch (EOFException eof) {
					// 处理索引已经刷新到文件，但是数据还没有刷新到文件的情况，这里强制刷新一下存储，然后再读数据，如果还读不到，说明真有问题了。
					this.indexBucket.resetNext();
					
					if (!shouldSleep) {
						return null;
					} else {
						try {
							Thread.sleep(1);

							continue;
						} catch (InterruptedException e1) {
							Thread.currentThread().interrupt();
						}
					}
				}

				lastIndexKey = nextL2Index.getIndexKey();
				lastReadSequence = sequence;
			} catch (IOException e) {
				throw new StorageReadException("Failed to read", e);
			}
		}

		return event;
	}

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

	private DataBucketManager createDataBucketManager(String baseDir, String prefix, String database, int lengthMB) {
		LocalFileDataBucketManager bucketManager = new LocalFileDataBucketManager();

		bucketManager.setBaseDir(baseDir + "/" + database);
		bucketManager.setBucketFilePrefix(prefix);
		bucketManager.setMaxBucketLengthMB(lengthMB);

		return bucketManager;
	}

	private byte[] readData(Sequence sequence) throws StorageClosedException, IOException {
		if (sequence.getOffset() != lastReadSequence.getOffset()) {
			readDataBucket.skip(sequence.getOffset() - lastReadSequence.getOffset() - lastReadSequence.getLen());
		}

		return readDataBucket.getNext();
	}

	@Override
	public Event next() throws StorageException {
		return next(false);
	}

	private void checkClosed() throws StorageClosedException {
		if (stopped) {
			throw new StorageClosedException("Channel has been closed.");
		}
	}

	@Override
	public void close() {
		if (!stopped) {
			stopped = true;
			if (this.readDataBucket != null) {
				try {
					this.readDataBucket.stop();
					this.readDataBucket = null;
				} catch (IOException ignore) {
				}
			}

			if (this.indexBucket != null) {
				try {
					this.indexBucket.stop();
					this.indexBucket = null;
				} catch (IOException ignore) {
				}
			}
		}
	}

	@Override
	public void open(long serverId, String binlogFile, long binlogPosition) throws IOException {
		openInternal();

		if (serverId != 0 && binlogFile != null && binlogPosition > 0) {
			try {
				this.lastIndexKey = this.indexManager.findByBinlog(new IndexKeyImpl(serverId, binlogFile, binlogPosition),
				      true);
			} catch (IOException e) {
				throw new InvalidSequenceException("find binlog error", e);
			}

			if (this.lastIndexKey == null) {
				throw new InvalidSequenceException("cannot find binlog position");
			}

			initIndexBucket(false);
		} else {
			throw new InvalidSequenceException("Invalid binlog info");
		}

		try {
			this.indexBucket.start();
		} catch (IOException ignore) {
		}
	}

	@Override
	public void open(long startTimeStamp) throws IOException {
		openInternal();

		try {
			if (startTimeStamp == SubscribeConstant.SEQ_FROM_LATEST) {
				this.lastIndexKey = this.indexManager.findLatest();
			} else if (startTimeStamp == SubscribeConstant.SEQ_FROM_OLDEST) {
				this.lastIndexKey = this.indexManager.findFirst();
			} else {
				this.lastIndexKey = this.indexManager.findByTime(new IndexKeyImpl(startTimeStamp), true);
			}
		} catch (IOException e) {
			throw new InvalidSequenceException("find binlog error", e);
		}

		if (this.lastIndexKey == null) {
			throw new InvalidSequenceException("cannot find any latest binlog");
		}

		initIndexBucket(startTimeStamp == SubscribeConstant.SEQ_FROM_OLDEST);

		try {
			this.indexBucket.start();
		} catch (IOException ignore) {
		}
	}

	private void openInternal() throws IOException {
		if (!stopped) {
			return;
		}

		this.indexManager = new DefaultIndexManager<IndexKeyImpl, IndexValueImpl>(GlobalStorageConfig.binlogIndexBaseDir
		      + "/" + database, new IndexKeyConvertor(), new IndexValueConvertor());
		this.slaveIndex = createDataBucketManager(GlobalStorageConfig.slaveStorageBaseDir,
		      GlobalStorageConfig.slaveBucketFilePrefix, database, GlobalStorageConfig.maxMasterBucketLengthMB);
		this.slaveIndex.start();
		this.masterIndex = createDataBucketManager(GlobalStorageConfig.masterStorageBaseDir,
		      GlobalStorageConfig.masterBucketFilePrefix, database, GlobalStorageConfig.maxMasterBucketLengthMB);
		this.masterIndex.start();

		stopped = false;
	}

	private void initIndexBucket(boolean inclusive) throws InvalidSequenceException {
		try {
			this.indexBucket = indexManager.getIndexBucket(this.lastIndexKey, inclusive);
		} catch (IOException e) {
			throw new InvalidSequenceException("Invalid binlogInfo(", e);
		}

		if (this.indexBucket == null) {
			throw new InvalidSequenceException("Invalid binlogInfo(");
		}
	}
}
