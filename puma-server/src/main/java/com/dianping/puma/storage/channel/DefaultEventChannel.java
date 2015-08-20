package com.dianping.puma.storage.channel;

import com.dianping.puma.core.codec.EventCodec;
import com.dianping.puma.core.constant.SubscribeConstant;
import com.dianping.puma.core.event.Event;
import com.dianping.puma.storage.EventChannel;
import com.dianping.puma.storage.EventStorage;
import com.dianping.puma.storage.Sequence;
import com.dianping.puma.storage.bucket.BucketManager;
import com.dianping.puma.storage.bucket.DataBucket;
import com.dianping.puma.storage.exception.InvalidSequenceException;
import com.dianping.puma.storage.exception.StorageClosedException;
import com.dianping.puma.storage.exception.StorageException;
import com.dianping.puma.storage.exception.StorageReadException;
import com.dianping.puma.storage.index.IndexBucket;
import com.dianping.puma.storage.index.IndexKeyImpl;
import com.dianping.puma.storage.index.IndexManager;
import com.dianping.puma.storage.index.IndexValueImpl;

import java.io.EOFException;
import java.io.IOException;

public class DefaultEventChannel extends AbstractEventChannel implements EventChannel {
    private EventStorage eventStorage;

    private BucketManager bucketManager;

    private IndexManager<IndexKeyImpl, IndexValueImpl> indexManager;

    private IndexBucket<IndexKeyImpl, IndexValueImpl> indexBucket;

    private IndexBucket<IndexKeyImpl, IndexValueImpl> lastIndexBucket;

    private EventCodec codec;

    private volatile boolean stopped = true;

    private DataBucket readDataBucket;

    private IndexKeyImpl lastIndexKey = null;

    private Sequence lastReadSequence = null;

    public DefaultEventChannel(EventStorage eventStorage) throws StorageException {
        this.eventStorage = eventStorage;
        this.bucketManager = eventStorage.getBucketManager();
        this.indexManager = eventStorage.getIndexManager();
        this.codec = eventStorage.getEventCodec();
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
                    readDataBucket = this.bucketManager.getReadBucket(sequence.longValue(), false);
                }

                try {
                    event = codec.decode(readData(sequence));
                } catch (EOFException eof) {
                    // 处理索引已经刷新到文件，但是数据还没有刷新到文件的情况，这里强制刷新一下存储，然后再读数据，如果还读不到，说明真有问题了。
                    this.eventStorage.flush();

                    event = codec.decode(readData(sequence));
                }

                lastIndexKey = nextL2Index.getIndexKey();
                lastReadSequence = sequence;
            } catch (IOException e) {
                throw new StorageReadException("Failed to read", e);
            }
        }

        return event;
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

    public void setBucketManager(BucketManager bucketManager) {
        this.bucketManager = bucketManager;
    }

    @Override
    public void open(long serverId, String binlogFile, long binlogPosition) throws InvalidSequenceException {
        if (!stopped) {
            return;
        }

        stopped = false;

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
    public void open(long startTimeStamp) throws InvalidSequenceException {
        if (!stopped) {
            return;
        }

        stopped = false;

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
