package com.dianping.puma.storage.channel;

import com.dianping.puma.common.AbstractLifeCycle;
import com.dianping.puma.core.codec.EventCodec;
import com.dianping.puma.core.codec.RawEventCodec;
import com.dianping.puma.core.constant.SubscribeConstant;
import com.dianping.puma.core.event.Event;
import com.dianping.puma.storage.Sequence;
import com.dianping.puma.storage.data.factory.DataManagerFactory;
import com.dianping.puma.storage.data.ReadDataManager;
import com.dianping.puma.storage.exception.InvalidSequenceException;
import com.dianping.puma.storage.exception.StorageClosedException;
import com.dianping.puma.storage.exception.StorageException;
import com.dianping.puma.storage.exception.StorageReadException;
import com.dianping.puma.storage.index.*;
import com.google.common.base.Strings;

import java.io.IOException;

public class DefaultEventChannel extends AbstractLifeCycle implements EventChannel {

	private String database;

	private ReadIndexManager<IndexKeyImpl, IndexValueImpl> readIndexManager;

	private EventCodec codec = new RawEventCodec();

	private volatile boolean stopped = true;

	private ReadDataManager readDataManager;

	private Sequence lastSequence;

	public DefaultEventChannel(String database) {
		this.database = database;
	}

	@Override
	protected void doStart() {
		readDataManager = DataManagerFactory.newReadDataManager(database);
		readDataManager.start();
	}

	@Override
	protected void doStop() {
		readDataManager.stop();
	}

	@Override
	public void open(long serverId, String binlogFile, long binlogPosition) throws IOException {
		if (Strings.isNullOrEmpty(binlogFile) || binlogPosition < 0) {
			throw new InvalidSequenceException("Invalid binlog info");
		}

		openInternal();

		IndexValueImpl value;

		if (serverId != 0 && binlogPosition > 0) {
			try {
				value = this.readIndexManager
						.findByBinlog(new IndexKeyImpl(serverId, binlogFile, binlogPosition), true);
			} catch (IOException e) {
				throw new InvalidSequenceException("find binlog error", e);
			}

			if (value == null) {
				throw new InvalidSequenceException("cannot find binlog position");
			}

			try {
				readDataManager.open(value.getSequence());
				//readDataManager.open(value.getSequence(), false);
			} catch (IOException e) {
				throw new InvalidSequenceException("cannot find binlog position.");
			}

			/*
			this.readDataBucket = initReadBucket(value.getSequence(), false);

			if (this.readDataBucket == null) {
				throw new InvalidSequenceException("cannot find binlog position");
			}*/
		} else {
			throw new InvalidSequenceException("Invalid binlog info");
		}
	}

	@Override
	public void open(long startTimeStamp) throws IOException {
		openInternal();

		IndexValueImpl value;
		try {
			if (startTimeStamp == SubscribeConstant.SEQ_FROM_LATEST) {
				value = this.readIndexManager.findLatest();
			} else if (startTimeStamp == SubscribeConstant.SEQ_FROM_OLDEST) {
				value = this.readIndexManager.findFirst();
			} else {
				value = this.readIndexManager.findByTime(new IndexKeyImpl(startTimeStamp), true);
			}
		} catch (IOException e) {
			throw new InvalidSequenceException("find binlog error", e);
		}

		if (value == null) {
			throw new InvalidSequenceException("cannot find any latest binlog");
		}

		try {
			readDataManager.open(value.getSequence());
			//readDataManager.open(value.getSequence(), startTimeStamp == SubscribeConstant.SEQ_FROM_LATEST);
		} catch (IOException e) {
			throw new InvalidSequenceException("cannot find any latest binlog.");
		}

		/*
		this.readDataBucket = initReadBucket(value.getSequence(), startTimeStamp == SubscribeConstant.SEQ_FROM_LATEST);

		if (this.readDataBucket == null) {
			throw new InvalidSequenceException("cannot find any latest binlog");
		}*/
	}

	@Override
	public Event next(boolean shouldSleep) throws StorageException {
		checkClosed();

		while (true) {
			try {
				checkClosed();
				byte[] data = readDataManager.next();
				//byte[] data = readDataBucket.getNext();
				Event event = codec.decode(data);
				lastSequence = new Sequence(event.getSeq(), data.length);

				/*
				if (event instanceof DdlEvent && !this.withDdl) {
					continue;
				}
				if (event instanceof RowChangedEvent) {
					RowChangedEvent rowChangedEvent = (RowChangedEvent) event;
					if ((rowChangedEvent.isTransactionBegin() || rowChangedEvent.isTransactionCommit())
							) {
						if (!this.withTransaction) {
							continue;
						}
					} else {
						if (!this.withDml) {
							continue;
						}

						if (!this.tables.contains(rowChangedEvent.getTable())) {
							continue;
						}
					}
				}
				*/
				return event;
			} catch (IOException e) {
				throw new StorageReadException("Failed to read", e);
			}
		}
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
		if (stopped) {
			return;
		}

		stopped = true;

		if (readDataManager != null) {
			readDataManager.stop();
		}

		if (readIndexManager != null) {
			readIndexManager.stop();
		}
	}

	private void openInternal() throws IOException {
		if (!stopped) {
			return;
		}

		this.readIndexManager = new DefaultReadIndexManager<IndexKeyImpl, IndexValueImpl>(
				new IndexKeyConverter(), new IndexValueConverter());

		stopped = false;
	}
}
