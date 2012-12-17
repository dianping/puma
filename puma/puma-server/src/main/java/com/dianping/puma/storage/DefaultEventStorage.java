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
import com.dianping.puma.storage.exception.InvalidSequenceException;
import com.dianping.puma.storage.exception.StorageClosedException;
import com.dianping.puma.storage.exception.StorageException;
import com.dianping.puma.storage.exception.StorageLifeCycleException;
import com.dianping.puma.storage.exception.StorageWriteException;

public class DefaultEventStorage implements EventStorage {
    private Bucket                            writingBucket;
    private BucketManager                     bucketManager;
    private EventCodec                        codec;
    private List<WeakReference<EventChannel>> openChannels = new ArrayList<WeakReference<EventChannel>>();
    private volatile boolean                  stopped      = true;
    private BucketIndex                       masterIndex;
    private BucketIndex                       slaveIndex;
    private BinlogIndexManager                binlogIndexManager;
    private ArchiveStrategy                   archiveStrategy;
    private CleanupStrategy                   cleanupStrategy;
    private String                            name;
    private static final String               datePattern  = "yyyy-MM-dd";
    private String                            lastDate;

    public void start() throws StorageLifeCycleException {
        SimpleDateFormat sdf = new SimpleDateFormat(datePattern);
        lastDate = sdf.format(new Date());
        stopped = false;
        bucketManager = new DefaultBucketManager(masterIndex, slaveIndex, binlogIndexManager, archiveStrategy,
                cleanupStrategy);
        try {
            binlogIndexManager.start();
            bucketManager.start();
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

    /**
     * @param masterIndex
     *            the masterIndex to set
     */
    public void setMasterIndex(BucketIndex masterIndex) {
        this.masterIndex = masterIndex;
    }

    /**
     * @param slaveIndex
     *            the slaveIndex to set
     */
    public void setSlaveIndex(BucketIndex slaveIndex) {
        this.slaveIndex = slaveIndex;
    }

    /**
     * @param archiveStrategy
     *            the archiveStrategy to set
     */
    public void setArchiveStrategy(ArchiveStrategy archiveStrategy) {
        this.archiveStrategy = archiveStrategy;
    }

    public void setBinlogIndexManager(BinlogIndexManager binlogIndexManager) {
        this.binlogIndexManager = binlogIndexManager;
    }

    @Override
    public EventChannel getChannel(long seq, long serverId, String binlogFile, long binlogPos) throws StorageException {
        EventChannel channel = null;
        if (seq != -3) {
            channel = new DefaultEventChannel(bucketManager, seq, codec);
        } else {
            // TODO BinlogInfo & BinlogInfoAndSeq merge
            BinlogInfoAndSeq startbinlog = new BinlogInfoAndSeq(serverId, binlogFile, Long.valueOf(binlogPos)
                    .longValue(), -1);
            channel = new DefaultEventChannel(bucketManager, translateBinlogInfoToSeq(startbinlog), codec);
        }
        openChannels.add(new WeakReference<EventChannel>(channel));
        return channel;
    }

    // TODO translateBinlogInfoToSeq
    public long translateBinlogInfoToSeq(BinlogInfoAndSeq binlogInfoAndSeq) throws StorageException {
        try {
            // TODO
            long result = this.binlogIndexManager.tranBinlogIndexToSeq(binlogInfoAndSeq);
            if (result != -1) {
                return result;
            } else {
                // TODO
                throw new IOException();
            }
        } catch (IOException e) {
            throw new InvalidSequenceException("Invalid binlogInfo(" + binlogInfoAndSeq + ")", e);
        }
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
            if (writingBucket == null) {
                writingBucket = bucketManager.getNextWriteBucket();
            } else if (!writingBucket.hasRemainingForWrite()) {
                writingBucket.stop();
                writingBucket = bucketManager.getNextWriteBucket();
            } else if (writingBucket.getCurrentWritingBinlogInfoAndSeq().getServerId() != event.getServerId()) {
                writingBucket.stop();
                writingBucket = bucketManager.getNextWriteBucket();
            } else {
                SimpleDateFormat sdf = new SimpleDateFormat(datePattern);
                String nowDate = sdf.format(new Date());
                if (!lastDate.equals(nowDate)) {
                    writingBucket.stop();
                    writingBucket = bucketManager.getNextWriteBucket();
                    ;
                    lastDate = nowDate;
                }
            }

            event.setSeq(writingBucket.getCurrentWritingSeq());
            byte[] data = codec.encode(event);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bos.write(ByteArrayUtils.intToByteArray(data.length));
            bos.write(data);
            writingBucket.append(bos.toByteArray());
            BinlogInfoAndSeq nowItem = BinlogInfoAndSeq.getBinlogInfoAndSeq(event);
            if (writingBucket.getStartingBinlogInfoAndSeq() == null) {
                writingBucket.setStartingBinlogInfoAndSeq(nowItem);
                writingBucket.setCurrentWritingBinlogInfoAndSeq(nowItem);
                this.binlogIndexManager.updateBinlogIndex(writingBucket, nowItem);
            } else {
                if (!writingBucket.getCurrentWritingBinlogInfoAndSeq().binlogInfoEqual(
                        BinlogInfoAndSeq.getBinlogInfoAndSeq(event))) {
                    writingBucket.getCurrentWritingBinlogInfoAndSeq().setBinlogInfo(event);
                    this.binlogIndexManager.updateBinlogIndex(writingBucket, nowItem);
                }
            }
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
            try {
                this.binlogIndexManager.stop();
            } catch (IOException e) {
                // ignore
            }
            // TODO stop binlog index
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
