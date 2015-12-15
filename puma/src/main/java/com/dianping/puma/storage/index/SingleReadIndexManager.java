package com.dianping.puma.storage.index;

import com.dianping.puma.common.AbstractLifeCycle;
import com.dianping.puma.storage.bucket.BucketFactory;
import com.dianping.puma.storage.bucket.ReadBucket;
import org.apache.commons.lang3.tuple.Pair;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;

public abstract class SingleReadIndexManager<K, V> extends AbstractLifeCycle
        implements ReadIndexManager<K, V> {

    private final File file;

    private final int bufSizeByte;

    private final int avgSizeByte;

    protected ReadBucket readBucket;

    public SingleReadIndexManager(File file, int bufSizeByte, int avgSizeByte) {
        this.file = file;
        this.bufSizeByte = bufSizeByte;
        this.avgSizeByte = avgSizeByte;
    }

    @Override
    protected void doStart() {
        readBucket = BucketFactory.newLineReadBucket(file, bufSizeByte, avgSizeByte);
        readBucket.start();
    }

    @Override
    protected void doStop() {
        if (readBucket != null) {
            readBucket.stop();
        }
    }

    @Override
    public V findOldest() throws IOException {
        checkStop();

        try {
            byte[] data = readBucket.next();
            return decode(data).getRight();
        } catch (EOFException eof) {
            return null;
        }
    }

    @Override
    public V findLatest() throws IOException {
        checkStop();

        byte[] data = null;
        try {
            while (!isStopped()) {
                data = readBucket.next();
            }
            return null;
        } catch (EOFException eof) {
            if (data == null) {
                return null;
            }
            return decode(data).getRight();
        }
    }

    @Override
    public V find(K indexKey) throws IOException {
        checkStop();

        byte[] data;
        Pair<K, V> oldPair = null;
        try {
            while (true) {
                data = readBucket.next();
                if (data != null) {
                    Pair<K, V> pair = decode(data);
                    if (!greater(indexKey, pair.getLeft())) {
                        if (oldPair == null) {
                            throw new IOException("failed to find.");
                        } else {
                            return oldPair.getRight();
                        }
                    } else {
                        oldPair = pair;
                    }
                }
            }
        } catch (EOFException eof) {
            if (oldPair == null) {
                throw new IOException("failed to find.");
            } else {
                return oldPair.getRight();
            }
        }
    }


    protected abstract boolean greater(K aIndexKey, K bIndexKey);

    protected abstract Pair<K, V> decode(byte[] data) throws IOException;

}
