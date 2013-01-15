package com.dianping.puma.storage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import com.dianping.puma.common.SystemStatusContainer;
import com.dianping.puma.core.codec.EventCodec;
import com.dianping.puma.core.constant.SubscribeConstant;
import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.util.ByteArrayUtils;
import com.dianping.puma.storage.exception.InvalidSequenceException;
import com.dianping.puma.storage.exception.StorageClosedException;
import com.dianping.puma.storage.exception.StorageException;
import com.dianping.puma.storage.exception.StorageLifeCycleException;
import com.dianping.puma.storage.exception.StorageWriteException;

public class DefaultEventStorage implements EventStorage {
    private BucketManager                      bucketManager;
    private Bucket                             writingBucket;
    private EventCodec                         codec;
    private List<WeakReference<EventChannel>>  openChannels          = new ArrayList<WeakReference<EventChannel>>();
    private volatile boolean                   stopped               = true;
    private BucketIndex                        masterBucketIndex;
    private BucketIndex                        slaveBucketIndex;
    private ArchiveStrategy                    archiveStrategy;
    private CleanupStrategy                    cleanupStrategy;
    private String                             name;
    private static final String                datePattern           = "yyyy-MM-dd";
    private AtomicReference<String>            lastDate              = new AtomicReference<String>();
    private String                             binlogIndexBaseDir;
    private String                             timeStampIndexBaseDir;
    private DataIndex<TimeStampIndexKey, Long> timeStampIndex;
    private AtomicReference<TimeStampIndexKey> lastTimeStampIndexKey = new AtomicReference<TimeStampIndexKey>(null);
    private DataIndex<BinlogIndexKey, Long>    binlogIndex;
    private AtomicReference<BinlogIndexKey>    lastBinlogIndexKey    = new AtomicReference<BinlogIndexKey>(null);
    private AtomicReference<Long>              processingServerId    = new AtomicReference<Long>(null);

    /**
     * @param binlogIndexBaseDir
     *            the binlogIndexBaseDir to set
     */
    public void setBinlogIndexBaseDir(String binlogIndexBaseDir) {
        this.binlogIndexBaseDir = binlogIndexBaseDir;
    }

    /**
     * @param timeStampIndexBaseDir
     *            the timeStampIndexBaseDir to set
     */
    public void setTimeStampIndexBaseDir(String timeStampIndexBaseDir) {
        this.timeStampIndexBaseDir = timeStampIndexBaseDir;
    }

    /**
     * @return the masterBucketIndex
     */
    public BucketIndex getMasterBucketIndex() {
        return masterBucketIndex;
    }

    /**
     * @return the slaveBucketIndex
     */
    public BucketIndex getSlaveBucketIndex() {
        return slaveBucketIndex;
    }

    public void start() throws StorageLifeCycleException {
        stopped = false;
        masterBucketIndex.setMaster(true);
        slaveBucketIndex.setMaster(false);
        bucketManager = new DefaultBucketManager(masterBucketIndex, slaveBucketIndex, archiveStrategy, cleanupStrategy);
        timeStampIndex = new DefaultDataIndexImpl<TimeStampIndexKey, Long>(timeStampIndexBaseDir,
                new LongIndexItemConvertor(), new TimeStampIndexKeyConvertor());
        binlogIndex = new DefaultDataIndexImpl<BinlogIndexKey, Long>(binlogIndexBaseDir, new LongIndexItemConvertor(),
                new BinlogIndexKeyConvertor());

        cleanupStrategy.addDataIndex(binlogIndex);
        cleanupStrategy.addDataIndex(timeStampIndex);

        try {
            bucketManager.start();

            timeStampIndex.start();
            binlogIndex.start();
        } catch (Exception e) {
            throw new StorageLifeCycleException("Storage init failed", e);
        }
    }

    /**
     * @param cleanupStrategy
     *            the cleanupStrategy to set
     */
    public void setCleanupStrategy(CleanupStrategy cleanupStrategy) {
        this.cleanupStrategy = cleanupStrategy;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    public void setMasterBucketIndex(BucketIndex masterBucketIndex) {
        this.masterBucketIndex = masterBucketIndex;
    }

    public void setSlaveBucketIndex(BucketIndex slaveBucketIndex) {
        this.slaveBucketIndex = slaveBucketIndex;
    }

    /**
     * @param archiveStrategy
     *            the archiveStrategy to set
     */
    public void setArchiveStrategy(ArchiveStrategy archiveStrategy) {
        this.archiveStrategy = archiveStrategy;
    }

    @Override
    public EventChannel getChannel(long seq, long serverId, String binlog, long binlogPos, long timestamp)
            throws StorageException {
        long newSeq = translateSeqIfNeeded(seq, serverId, binlog, binlogPos, timestamp);
        EventChannel channel = new DefaultEventChannel(bucketManager, newSeq, codec, newSeq == seq);
        openChannels.add(new WeakReference<EventChannel>(channel));
        return channel;
    }

    /**
     * @param codec
     *            the codec to set
     */
    public void setCodec(EventCodec codec) {
        this.codec = codec;
    }

    @Override
    public synchronized void store(ChangedEvent event) throws StorageException {
        if (stopped) {
            throw new StorageClosedException("Storage has been closed.");
        }

        SimpleDateFormat sdf = new SimpleDateFormat(datePattern);
        String nowDate = sdf.format(new Date());

        if (processingServerId.get() == null) {
            processingServerId.set(event.getServerId());
        }

        if (lastDate.get() == null) {
            lastDate.set(nowDate);
        }

        try {
            boolean newL1Index = false;
            if (writingBucket == null) {
                writingBucket = bucketManager.getNextWriteBucket();
                newL1Index = true;
            } else if (!writingBucket.hasRemainingForWrite()) {
                writingBucket.stop();
                writingBucket = bucketManager.getNextWriteBucket();
                newL1Index = true;
            } else if (!processingServerId.get().equals(event.getServerId())) {
                writingBucket.stop();
                writingBucket = bucketManager.getNextWriteBucket();
                processingServerId.set(event.getServerId());
                newL1Index = true;
            } else {
                if (!lastDate.get().equals(nowDate)) {
                    writingBucket.stop();
                    writingBucket = bucketManager.getNextWriteBucket();
                    lastDate.set(nowDate);
                    newL1Index = true;
                }
            }

            long newSeq = writingBucket.getCurrentWritingSeq();
            updateIndex(event, newL1Index, newSeq);

            event.setSeq(newSeq);
            byte[] data = codec.encode(event);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bos.write(ByteArrayUtils.intToByteArray(data.length));
            bos.write(data);
            writingBucket.append(bos.toByteArray());
            bucketManager.updateLatestSequence(new Sequence(event.getSeq()));
            SystemStatusContainer.instance.updateStorageStatus(name, event.getSeq());
        } catch (IOException e) {
            throw new StorageWriteException("Failed to write event.", e);
        }
    }

    private void updateIndex(ChangedEvent event, boolean newL1Index, long newSeq) throws IOException {
        TimeStampIndexKey timestampKey = new TimeStampIndexKey(event.getExecuteTime());
        BinlogIndexKey binlogKey = new BinlogIndexKey(event.getBinlog(), event.getBinlogPos(), event.getServerId());

        if (newL1Index) {
            timeStampIndex.addL1Index(timestampKey, writingBucket.getBucketFileName().replace('/', '-'));
            binlogIndex.addL1Index(binlogKey, writingBucket.getBucketFileName().replace('/', '-'));
        }

        if (lastTimeStampIndexKey.get() == null || !lastTimeStampIndexKey.get().equals(timestampKey)) {
            timeStampIndex.addL2Index(timestampKey, newSeq);
            lastTimeStampIndexKey.set(timestampKey);
        }

        if (lastBinlogIndexKey.get() == null || !lastBinlogIndexKey.get().equals(binlogKey)) {
            binlogIndex.addL2Index(binlogKey, newSeq);
            lastBinlogIndexKey.set(binlogKey);
        }
    }

    private long translateSeqIfNeeded(long seq, long serverId, String binlog, long binlogPos, long timestamp)
            throws InvalidSequenceException {
        if (seq == SubscribeConstant.SEQ_FROM_BINLOGINFO) {
            if (serverId != -1L && binlog != null && binlogPos != -1L) {
                Long indexedSeq = binlogIndex.find(new BinlogIndexKey(binlog, binlogPos, serverId));
                if (indexedSeq != null) {
                    seq = indexedSeq.longValue();
                } else {
                    throw new InvalidSequenceException(String.format(
                            "Invalid binlogInfo(serverId=%d, binlog=%s, binlogPos=%d)", serverId, binlog, binlogPos));
                }
            } else {
                throw new InvalidSequenceException(String.format("Invalid sequence(seq=%d but no binlogInfo set)", seq));
            }
        } else if (seq == SubscribeConstant.SEQ_FROM_TIMESTAMP) {
            if (timestamp != -1L) {
                Long indexedSeq = timeStampIndex.find(new TimeStampIndexKey(timestamp));
                if (indexedSeq != null) {
                    seq = indexedSeq.longValue();
                } else {
                    throw new InvalidSequenceException(String.format("Invalid timestamp(timestamp=%d)", timestamp));
                }
            } else {
                throw new InvalidSequenceException(String.format("Invalid sequence(seq=%d but no timestamp set)", seq));
            }
        }
        return seq;
    }

    @Override
    public synchronized void stop() {
        if (stopped) {
            return;
        }
        stopped = true;
        try {
            bucketManager.stop();
        } catch (StorageLifeCycleException e1) {
            // ignore
        }
        if (writingBucket != null) {
            try {
                writingBucket.stop();
            } catch (IOException e) {
                // ignore
            }
        }

        try {
            timeStampIndex.stop();
        } catch (IOException e1) {
            // ignore
        }

        try {
            binlogIndex.stop();
        } catch (IOException e1) {
            // ignore
        }

        for (WeakReference<EventChannel> channelRef : openChannels) {
            EventChannel channel = channelRef.get();
            if (channel != null) {
                try {
                    channel.close();
                } catch (Exception e) {
                    // ignore
                }
            }
        }

    }

}
