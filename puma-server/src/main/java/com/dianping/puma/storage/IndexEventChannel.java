package com.dianping.puma.storage;

import java.io.EOFException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import com.dianping.puma.core.codec.EventCodec;
import com.dianping.puma.core.event.Event;
import com.dianping.puma.storage.exception.InvalidSequenceException;
import com.dianping.puma.storage.exception.StorageClosedException;
import com.dianping.puma.storage.exception.StorageException;
import com.dianping.puma.storage.exception.StorageReadException;

public class IndexEventChannel implements EventChannel {
	private BucketManager bucketManager;

	private IndexBucket<BinlogIndexKey, L2Index> indexBucket;

	private DataIndex<BinlogIndexKey, L2Index> indexManager;

	private EventCodec codec;

	private BinlogIndexKey binlogIndexKey;

	private volatile boolean stopped = true;

	private String database;

	private Set<String> tables;

	private boolean fromNext;

	private DataBucket readDataBucket;

	private Sequence lastReadSequence;

	public IndexEventChannel(BucketManager bucketManager, DataIndex<BinlogIndexKey, L2Index> indexManager,
	      EventCodec codec, BinlogIndexKey binlogIndexKey, long seq, boolean fromNext, String database, String... tables)
	      throws StorageException {
		this.bucketManager = bucketManager;
		this.indexManager = indexManager;
		this.binlogIndexKey = binlogIndexKey;
		this.codec = codec;
		this.fromNext = fromNext;

		this.database = database;
		this.tables = new HashSet<String>();
		for (String table : tables) {
			this.tables.add(table);
		}

		try {
			this.indexBucket = indexManager.getIndexBucket(this.binlogIndexKey);
			
			while(true){
				L2Index nextL2Index = this.indexBucket.next();
				
				if(nextL2Index.getBinlogIndexKey().equals(binlogIndexKey)){
					break;
				}
			}
			
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
				if (!nextL2Index.getDatabase().equalsIgnoreCase(this.database)) {
					continue;
				}

				if (!this.tables.contains(nextL2Index.getTable())) {
					continue;
				}

				Sequence sequence = nextL2Index.getSequence();
				lastBinLogIndexKey = nextL2Index.getBinlogIndexKey();

				if (this.readDataBucket == null) {
					this.readDataBucket = this.bucketManager.getReadBucket(sequence.longValue(), fromNext);
				}

				readDataBucket.skip(sequence.getOffset() - this.lastReadSequence.getOffset());
				byte[] data = readDataBucket.getNext();
				event = codec.decode(data);

				this.lastReadSequence = sequence;
			} catch (EOFException e) {
				try {
					if (this.bucketManager.hasNexReadBucket(this.lastReadSequence.longValue())) {
						this.readDataBucket.stop();
						this.indexBucket.stop();

						this.indexBucket = this.indexManager.getIndexBucket(lastBinLogIndexKey);
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
			this.readDataBucket.start();
		} catch (IOException ignore) {
		}
	}

	public void setBucketManager(BucketManager bucketManager) {
		this.bucketManager = bucketManager;
	}
}
