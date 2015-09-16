package com.dianping.puma.storage.channel;

import com.dianping.puma.core.codec.EventCodec;
import com.dianping.puma.core.codec.RawEventCodec;
import com.dianping.puma.core.constant.SubscribeConstant;
import com.dianping.puma.core.event.DdlEvent;
import com.dianping.puma.core.event.Event;
import com.dianping.puma.core.event.RowChangedEvent;
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
import com.dianping.puma.storage.index.*;
import com.google.common.base.Strings;

import java.io.EOFException;
import java.io.IOException;

public class DefaultEventChannel extends AbstractEventChannel implements EventChannel {

    private IndexManager<IndexKeyImpl, IndexValueImpl> indexManager;

    private EventCodec codec = new RawEventCodec();

    private volatile boolean stopped = true;

    private DataBucket readDataBucket;

    private Sequence lastSequence;

    public DefaultEventChannel(String database) {
        this.database = database;
    }

    @Override
    public Event next(boolean shouldSleep) throws StorageException {
        checkClosed();

        while (true) {
            try {
                checkClosed();
                byte[] data = readDataBucket.getNext();
                Event event = codec.decode(data);
                lastSequence = new Sequence(event.getSeq(), data.length);

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
                return event;
            } catch (EOFException e) {
                try {
                    DataBucket newReadDataBucket = getNextReadBucket(lastSequence);
                    if (newReadDataBucket == null) {
                        return null;
                    }
                    this.readDataBucket = newReadDataBucket;
                } catch (IOException exception) {
                    throw new StorageReadException("Failed to read", e);
                }
            } catch (IOException e) {
                throw new StorageReadException("Failed to read", e);
            }
        }
    }

    protected DataBucket initReadBucket(Sequence seq, boolean fromNext) throws IOException {
        checkClosed();
        DataBucketManager slaveDataBucketManager = null;
        DataBucketManager masterDataBucketManager = null;

        try {
            slaveDataBucketManager = createSlaveDataBucketManager();
            DataBucket bucket = slaveDataBucketManager.getReadBucket(seq.longValue(), fromNext);
            if (bucket != null) {
                return bucket;
            } else {
                masterDataBucketManager = createMasterDataBucketManager();
                return masterDataBucketManager.getReadBucket(seq.longValue(), fromNext);
            }
        } finally {
            if (slaveDataBucketManager != null) {
                slaveDataBucketManager.stop();
            }
            if (masterDataBucketManager != null) {
                masterDataBucketManager.stop();
            }
        }
    }

    protected DataBucket getNextReadBucket(Sequence seq) throws IOException {
        checkClosed();
        DataBucketManager slaveDataBucketManager = null;
        DataBucketManager masterDataBucketManager = null;

        try {
            slaveDataBucketManager = createSlaveDataBucketManager();
            DataBucket bucket = slaveDataBucketManager.getNextReadBucket(seq);
            if (bucket != null) {
                return bucket;
            } else {
                masterDataBucketManager = createMasterDataBucketManager();
                return masterDataBucketManager.getNextReadBucket(seq);
            }
        } finally {
            if (slaveDataBucketManager != null) {
                slaveDataBucketManager.stop();
            }
            if (masterDataBucketManager != null) {
                masterDataBucketManager.stop();
            }
        }
    }

    private DataBucketManager createSlaveDataBucketManager() throws IOException {
        DataBucketManager slaveIndex = createDataBucketManager(GlobalStorageConfig.slaveStorageBaseDir,
                GlobalStorageConfig.slaveBucketFilePrefix, database, GlobalStorageConfig.maxMasterBucketLengthMB);
        slaveIndex.start();
        return slaveIndex;
    }

    private DataBucketManager createMasterDataBucketManager() throws IOException {
        DataBucketManager masterIndex = createDataBucketManager(GlobalStorageConfig.masterStorageBaseDir,
                GlobalStorageConfig.masterBucketFilePrefix, database, GlobalStorageConfig.maxMasterBucketLengthMB);
        masterIndex.start();
        return masterIndex;
    }

    private DataBucketManager createDataBucketManager(String baseDir, String prefix, String database, int lengthMB) {
        LocalFileDataBucketManager bucketManager = new LocalFileDataBucketManager();

        bucketManager.setBaseDir(baseDir + "/" + database);
        bucketManager.setBucketFilePrefix(prefix);
        bucketManager.setMaxBucketLengthMB(lengthMB);

        return bucketManager;
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
            if (this.indexManager != null) {
                try {
                    this.indexManager.stop();
                    this.indexManager = null;
                } catch (IOException ignore) {
                }
            }
        }
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
                value = this.indexManager.findByBinlog(new IndexKeyImpl(serverId, binlogFile, binlogPosition), true);
            } catch (IOException e) {
                throw new InvalidSequenceException("find binlog error", e);
            }

            if (value == null) {
                throw new InvalidSequenceException("cannot find binlog position");
            }

            this.readDataBucket = initReadBucket(value.getSequence(), false);

            if (this.readDataBucket == null) {
                throw new InvalidSequenceException("cannot find binlog position");
            }
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
                value = this.indexManager.findLatest();
            } else if (startTimeStamp == SubscribeConstant.SEQ_FROM_OLDEST) {
                value = this.indexManager.findFirst();
            } else {
                value = this.indexManager.findByTime(new IndexKeyImpl(startTimeStamp), true);
            }
        } catch (IOException e) {
            throw new InvalidSequenceException("find binlog error", e);
        }

        if (value == null) {
            throw new InvalidSequenceException("cannot find any latest binlog");
        }

        this.readDataBucket = initReadBucket(value.getSequence(), startTimeStamp == SubscribeConstant.SEQ_FROM_LATEST);

        if (this.readDataBucket == null) {
            throw new InvalidSequenceException("cannot find any latest binlog");
        }
    }

    private void openInternal() throws IOException {
        if (!stopped) {
            return;
        }

        this.indexManager = new DefaultIndexManager<IndexKeyImpl, IndexValueImpl>(GlobalStorageConfig.binlogIndexBaseDir
                + "/" + database, new IndexKeyConvertor(), new IndexValueConvertor());

        stopped = false;
    }
}
