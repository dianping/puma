package com.dianping.puma.storage.channel;

import java.io.EOFException;
import java.io.IOException;

import com.dianping.puma.core.codec.EventCodec;
import com.dianping.puma.core.constant.SubscribeConstant;
import com.dianping.puma.core.event.Event;
import com.dianping.puma.storage.EventChannel;
import com.dianping.puma.storage.Sequence;
import com.dianping.puma.storage.bucket.BucketManager;
import com.dianping.puma.storage.bucket.DataBucket;
import com.dianping.puma.storage.exception.InvalidSequenceException;
import com.dianping.puma.storage.exception.StorageClosedException;
import com.dianping.puma.storage.exception.StorageException;
import com.dianping.puma.storage.exception.StorageReadException;
import com.dianping.puma.storage.index.BinlogIndexKey;
import com.dianping.puma.storage.index.DataIndex;
import com.dianping.puma.storage.index.IndexBucket;
import com.dianping.puma.storage.index.L2Index;

public class DefaultEventChannel extends AbstractEventChannel implements EventChannel {
	private BucketManager bucketManager;

	private DataIndex<BinlogIndexKey, L2Index> indexManager;

	private IndexBucket<BinlogIndexKey, L2Index> indexBucket;

	private EventCodec codec;

	private BinlogIndexKey binlogIndexKey;

	private volatile boolean stopped = true;

	private boolean fromNext;

	private DataBucket readDataBucket;

	private Sequence lastReadSequence;

	public DefaultEventChannel(BucketManager bucketManager, DataIndex<BinlogIndexKey, L2Index> indexManager,
	      EventCodec codec, long seq, long serverId, String binlogFile, long binlogPos, long timestamp)
	      throws StorageException {
		this.bucketManager = bucketManager;
		this.indexManager = indexManager;
		this.codec = codec;

		if (seq == SubscribeConstant.SEQ_FROM_BINLOGINFO) {
			if (serverId != -1L && binlogFile != null && binlogPos != -1L) {
				this.binlogIndexKey = new BinlogIndexKey(binlogFile, binlogPos, serverId);

				try {
					this.indexBucket = indexManager.getIndexBucket(seq, this.binlogIndexKey);
				} catch (IOException e) {
					throw new InvalidSequenceException("Invalid sequence(" + seq + ")", e);
				}

			} else {
				throw new InvalidSequenceException(String.format("Invalid sequence(seq=%d but no binlogInfo set)", seq));
			}
		} else {
			try {
	         this.indexBucket = indexManager.getIndexBucket(seq, null);
	         
         } catch (IOException e) {
         	
         }
		}

		try {
			this.readDataBucket = bucketManager.getReadBucket(seq, fromNext);
		} catch (IOException e) {
			throw new InvalidSequenceException("Invalid sequence(" + seq + ")", e);
		}

		this.lastReadSequence = this.readDataBucket.getStartingSequece();

		stopped = false;
	}


	@Override
	public Event next() throws StorageException {
		checkClosed();

		Event event = null;
		BinlogIndexKey lastBinLogIndexKey = null;
		while (event == null) {
			try {
				checkClosed();
				L2Index nextL2Index = this.indexBucket.next();

				if (this.database != null && !nextL2Index.getDatabase().equals(this.database)) {
					continue;
				}
				if (this.tables != null && !this.tables.contains(nextL2Index.getTable())) {
					continue;
				}
				if (this.withDdl != nextL2Index.isDdl() || this.withDml != nextL2Index.isDml()) {
					continue;
				}

				Sequence sequence = nextL2Index.getSequence();
				lastBinLogIndexKey = nextL2Index.getBinlogIndexKey();

				readDataBucket.skip(sequence.getOffset() - this.lastReadSequence.getOffset());
				byte[] data = readDataBucket.getNext();
				event = codec.decode(data);

				this.lastReadSequence = sequence;
			} catch (EOFException e) {
				try {
					if (this.bucketManager.hasNexReadBucket(this.lastReadSequence.longValue())) {
						this.readDataBucket.stop();
						this.indexBucket.stop();

						this.indexBucket = this.indexManager.getNextIndexBucket(lastBinLogIndexKey);
						this.indexBucket.start();
						this.readDataBucket = this.bucketManager.getNextReadBucket(this.lastReadSequence.longValue());
						this.readDataBucket.start();

						this.lastReadSequence = this.readDataBucket.getStartingSequece();
					} else {
						try {
							Thread.sleep(5);
						} catch (InterruptedException e1) {
							Thread.currentThread().interrupt();
						}
					}
				} catch (IOException ex) {
					throw new StorageReadException("Failed to read", ex);
				}
			} catch (IOException e) {
				throw new StorageReadException("Failed to read", e);
			}
		}

		return event;
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

	public void open() {
		if (!stopped) {
			return;
		}

		stopped = false;
		try {
			this.indexBucket.start();
			this.indexBucket.locate(binlogIndexKey);

			this.readDataBucket.start();
		} catch (IOException ignore) {
		}
	}

	public void setBucketManager(BucketManager bucketManager) {
		this.bucketManager = bucketManager;
	}
}
