package com.dianping.puma.storage.data;

import com.dianping.puma.common.AbstractLifeCycle;
import com.dianping.puma.core.codec.EventCodec;
import com.dianping.puma.core.codec.RawEventCodec;
import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.storage.Sequence;
import com.dianping.puma.storage.bucket.BucketFactory;
import com.dianping.puma.storage.bucket.WriteBucket;

import java.io.File;
import java.io.IOException;

public final class SingleWriteDataManager extends AbstractLifeCycle
        implements WriteDataManager<Sequence, ChangedEvent> {

    private final File file;

    private final int bufSizeByte;

    private final int maxSizeByte;

    private final String date;

    private final int number;

    private WriteBucket writeBucket;

    private EventCodec eventCodec = new RawEventCodec();

    protected SingleWriteDataManager(File file, String date, int number, int bufSizeByte, int maxSizeByte) {
        this.file = file;
        this.date = date;
        this.number = number;
        this.bufSizeByte = bufSizeByte;
        this.maxSizeByte = maxSizeByte;
    }

    @Override
    protected void doStart() {
        writeBucket = BucketFactory.newLengthWriteBucket(file, bufSizeByte, maxSizeByte);
        writeBucket.start();
    }

    @Override
    protected void doStop() {
        if (writeBucket != null) {
            writeBucket.stop();
        }
    }

    @Override
    public Sequence append(ChangedEvent binlogEvent) throws IOException {
        checkStop();

        Sequence sequence = position();

        byte[] data = encode(binlogEvent);
        writeBucket.append(data);

        return sequence;
    }

    @Override
    public void flush() throws IOException {
        checkStop();

        writeBucket.flush();
    }

    protected boolean hasRemainingForWrite() {
        checkStop();

        return writeBucket.hasRemainingForWrite();
    }

    @Override
    public Sequence position() {
        checkStop();

        return new Sequence(date, number, writeBucket.position());
    }

    protected byte[] encode(ChangedEvent binlogEvent) throws IOException {
        return eventCodec.encode(binlogEvent);
    }
}
