package com.dianping.puma.storage.data;

import com.dianping.puma.common.AbstractLifeCycle;
import com.dianping.puma.core.codec.EventCodec;
import com.dianping.puma.core.codec.RawEventCodec;
import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.event.Event;
import com.dianping.puma.storage.Sequence;
import com.dianping.puma.storage.bucket.BucketFactory;
import com.dianping.puma.storage.bucket.ReadBucket;

import java.io.File;
import java.io.IOException;

public final class SingleReadDataManager extends AbstractLifeCycle
        implements ReadDataManager<Sequence, ChangedEvent> {

    private final File file;

    private final int bufSizeByte;

    private final int avgSizeByte;

    private ReadBucket readBucket;

    private Sequence sequence;

    private EventCodec eventCodec = new RawEventCodec();

    protected SingleReadDataManager(File file, int bufSizeByte, int avgSizeByte) {
        this.file = file;
        this.bufSizeByte = bufSizeByte;
        this.avgSizeByte = avgSizeByte;
    }

    @Override
    protected void doStart() {
        readBucket = BucketFactory.newLengthReadBucket(file, bufSizeByte, avgSizeByte);
        readBucket.start();
    }

    @Override
    protected void doStop() {
        if (readBucket != null) {
            readBucket.stop();
        }
    }

    @Override
    public Sequence position() {
        checkStop();

        return new Sequence(sequence.getCreationDate(), sequence.getNumber(), readBucket.position());
    }

    @Override
    public void open(Sequence sequence) throws IOException {
        checkStop();

        this.sequence = new Sequence(sequence.getCreationDate(), sequence.getNumber(), 0);

        long offset = sequence.getOffset();
        readBucket.skip(offset);
    }

    @Override
    public ChangedEvent next() throws IOException {
        checkStop();

        byte[] data = readBucket.next();
        if (data == null) {
            return null;
        }
        return decode(data);
    }

    @Override
    public String getStorageMode() {
        return "File";
    }

    protected ChangedEvent decode(byte[] data) throws IOException {
        Event event = eventCodec.decode(data);
        if (!(event instanceof ChangedEvent)) {
            throw new IOException("unknown binlog event format.");
        }
        return (ChangedEvent) event;
    }
}
