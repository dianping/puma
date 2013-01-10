package com.dianping.puma.storage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.dianping.puma.common.SystemStatusContainer;
import com.dianping.puma.core.codec.EventCodec;
import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.util.ByteArrayUtils;
import com.dianping.puma.storage.exception.StorageClosedException;
import com.dianping.puma.storage.exception.StorageException;
import com.dianping.puma.storage.exception.StorageLifeCycleException;
import com.dianping.puma.storage.exception.StorageWriteException;

public class DefaultEventStorage implements EventStorage {
    private BucketManager                      bucketManager;
    private Bucket                             writingBucket;
    private EventCodec                         codec;
    private List<WeakReference<EventChannel>>  openChannels = new ArrayList<WeakReference<EventChannel>>();
    private volatile boolean                   stopped      = true;
    private BucketIndex                        masterBucketIndex;
    private BucketIndex                        slaveBucketIndex;
    private ArchiveStrategy                    archiveStrategy;
    private CleanupStrategy                    cleanupStrategy;
    private String                             name;
    private static final String                datePattern  = "yyyy-MM-dd";
    private String                             lastDate;
    private String                             binlogIndexBaseDir;
    private String                             timeStampIndexBaseDir;
    private DataIndex<TimeStampIndexKey, Long> timeStampIndex;
    private DataIndex<BinlogIndexKey, Long>    binlogIndex;

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
        SimpleDateFormat sdf = new SimpleDateFormat(datePattern);
        lastDate = sdf.format(new Date());
        stopped = false;
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
    public EventChannel getChannel(long seq) throws StorageException {
        EventChannel channel = new DefaultEventChannel(bucketManager, seq, codec);
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
        try {
            boolean newL1Index = false;
            if (writingBucket == null) {
                writingBucket = bucketManager.getNextWriteBucket();
                newL1Index = true;
            } else if (!writingBucket.hasRemainingForWrite()) {
                writingBucket.stop();
                writingBucket = bucketManager.getNextWriteBucket();
                newL1Index = true;
            } else {
                SimpleDateFormat sdf = new SimpleDateFormat(datePattern);
                String nowDate = sdf.format(new Date());
                if (!lastDate.equals(nowDate)) {
                    writingBucket.stop();
                    writingBucket = bucketManager.getNextWriteBucket();
                    lastDate = nowDate;
                    newL1Index = true;
                }
            }

            long newSeq = writingBucket.getCurrentWritingSeq();

            if (newL1Index) {
                timeStampIndex.addL1Index(new TimeStampIndexKey(event.getExecuteTime()),
                        writingBucket.getBucketFileName());
                binlogIndex.addL1Index(
                        new BinlogIndexKey(event.getBinlog(), event.getBinlogPos(), event.getServerId()),
                        writingBucket.getBucketFileName());
            }

            timeStampIndex.addL2Index(new TimeStampIndexKey(event.getExecuteTime()), newSeq);
            binlogIndex.addL2Index(new BinlogIndexKey(event.getBinlog(), event.getBinlogPos(), event.getServerId()),
                    newSeq);

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
