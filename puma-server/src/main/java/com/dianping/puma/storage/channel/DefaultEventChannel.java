package com.dianping.puma.storage.channel;

import com.dianping.puma.core.codec.EventCodec;
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

import java.io.EOFException;
import java.io.IOException;

public class DefaultEventChannel extends AbstractEventChannel implements EventChannel {
    private BucketManager bucketManager;

    private DataIndex<BinlogIndexKey, L2Index> indexManager;

    private IndexBucket<BinlogIndexKey, L2Index> indexBucket;

    private EventCodec codec;

    private BinlogIndexKey binlogIndexKey;

    private volatile boolean stopped = true;

    private DataBucket readDataBucket;

    private BinlogIndexKey lastBinLogIndexKey = null;

    private Sequence lastReadSequence = null;

    public DefaultEventChannel(BucketManager bucketManager, DataIndex<BinlogIndexKey, L2Index> indexManager,
                               EventCodec codec, long seq, long serverId, String binlogFile, long binlogPos, long timestamp)
            throws StorageException {
        this.bucketManager = bucketManager;
        this.indexManager = indexManager;
        this.codec = codec;
        this.binlogIndexKey = new BinlogIndexKey(binlogFile, binlogPos, serverId);

        try {
            this.indexBucket = indexManager.getIndexBucket(seq, this.binlogIndexKey);
        } catch (IOException e) {
            throw new InvalidSequenceException("Invalid sequence(" + seq + ")", e);
        }

        stopped = false;
    }

    @Override
    public Event next(boolean emptyReturnNull) throws StorageException {
        checkClosed();

        Event event = null;

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

                if (readDataBucket == null) {
                    lastReadSequence = sequence;
                    readDataBucket = this.bucketManager.getReadBucket(sequence.longValue(), false);
                }

                if (sequence.getOffset() != lastReadSequence.getOffset()) {
                    readDataBucket.skip(sequence.getOffset() - lastReadSequence.getOffset() - lastReadSequence.getLen());
                }
                byte[] data = readDataBucket.getNext();
                event = codec.decode(data);

                lastBinLogIndexKey = nextL2Index.getBinlogIndexKey();
                lastReadSequence = sequence;
            } catch (EOFException e) {
                try {
                    if (this.bucketManager.hasNexReadBucket(lastReadSequence.longValue())) {
                        if (readDataBucket != null) {
                            this.readDataBucket.stop();
                            this.readDataBucket = null;
                        }

                        if (indexBucket != null) {
                            this.indexBucket.stop();
                        }

                        this.indexBucket = this.indexManager.getNextIndexBucket(lastBinLogIndexKey);
                        this.indexBucket.start();
                    } else {
                        if (emptyReturnNull) {
                            return null;
                        }
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
