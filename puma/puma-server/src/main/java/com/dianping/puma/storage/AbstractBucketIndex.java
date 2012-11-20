/**
 * Project: puma-server
 * 
 * File Created at 2012-7-18
 * $Id$
 * 
 * Copyright 2010 dianping.com.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Dianping Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with dianping.com.
 */
package com.dianping.puma.storage;

import java.io.EOFException;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.NavigableMap;
import java.util.Properties;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicReference;

import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.storage.exception.StorageClosedException;

/**
 * 
 * @author Leo Liang
 * 
 */
public abstract class AbstractBucketIndex implements BucketIndex {
    protected static final String                        PATH_SEPARATOR     = "/";
    protected AtomicReference<TreeMap<Sequence, String>> index              = new AtomicReference<TreeMap<Sequence, String>>();
    protected String                                     baseDir;
    private String                                       bucketFilePrefix   = "b-";
    protected int                                        maxBucketLengthMB  = 2000;
    private volatile boolean                             stopped            = true;
    protected AtomicReference<Sequence>                  latestSequence     = new AtomicReference<Sequence>();
    protected String                                     zipIndexsuffix     = "-zipIndex";
    protected Compressor                                 compressor;
    protected static final int                           COMPRESS_HEAD      = 20;
    protected static final String                        ZIPFORMAT          = "ZIPFORMAT           ";
    protected static final String                        ZIPINDEX_SEPARATOR = "$";

    // TODO remove zipIndex, refactor to local

    public Compressor getCompress() {
        return compressor;
    }

    public void setCompressor(Compressor compressor) {
        this.compressor = compressor;
    }

    /**
     * @return the index
     */
    @Override
    public AtomicReference<TreeMap<Sequence, String>> getIndex() {
        return index;
    }

    /**
     * @return the bucketFilePrefix
     */
    public String getBucketFilePrefix() {
        return bucketFilePrefix;
    }

    /**
     * @return the maxBucketLengthMB
     */
    public int getMaxBucketLengthMB() {
        return maxBucketLengthMB;
    }

    /**
     * @return the stop
     */
    public boolean isStop() {
        return stopped;
    }

    /**
     * @return the latestSequence
     */
    public AtomicReference<Sequence> getLatestSequence() {
        return latestSequence;
    }

    public void setBucketFilePrefix(String bucketFilePrefix) {
        this.bucketFilePrefix = bucketFilePrefix;
    }

    public void setMaxBucketLengthMB(int maxBucketLengthMB) {
        this.maxBucketLengthMB = maxBucketLengthMB;
    }

    public void setBaseDir(String baseDir) {
        this.baseDir = baseDir;
    }

    @Override
    public void start() throws IOException {
        stopped = false;
    }

    @Override
    public void add(Bucket bucket) throws StorageClosedException {
        checkClosed();
        TreeMap<Sequence, String> newIndex = new TreeMap<Sequence, String>(index.get());
        newIndex.put(new Sequence(bucket.getStartingSequece()), convertToPath(bucket.getStartingSequece()));
        index.set(newIndex);
    }

    @Override
    public Bucket getNextReadBucket(Sequence sequence) throws IOException, StorageClosedException {
        checkClosed();
        NavigableMap<Sequence, String> tailMap = index.get().tailMap(sequence, false);
        if (!tailMap.isEmpty()) {
            Entry<Sequence, String> firstEntry = tailMap.firstEntry();
            return doGetReadBucket(baseDir, firstEntry.getValue(), firstEntry.getKey(), maxBucketLengthMB);
        }
        return null;
    }

    protected abstract Bucket doGetReadBucket(String baseDir, String path, Sequence startingSeq, int maxSizeMB)
            throws IOException;

    @Override
    public Bucket getNextWriteBucket() throws IOException, StorageClosedException {
        checkClosed();
        Entry<Sequence, String> lastEntry = index.get().lastEntry();
        Sequence nextSeq = null;
        if (lastEntry == null) {
            nextSeq = new Sequence(getNowCreationDate(), 0);
        } else {
            nextSeq = getNextWriteBucketSequence(new Sequence(lastEntry.getKey()));
        }
        String bucketPath = convertToPath(nextSeq);
        return doGetNextWriteBucket(baseDir, bucketPath, nextSeq);

    }

    protected abstract Bucket doGetNextWriteBucket(String baseDir, String bucketPath, Sequence startingSequence)
            throws IOException;

    protected int getNowCreationDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd");
        return Integer.valueOf(sdf.format(new Date()));
    }

    protected Sequence getNextWriteBucketSequence(Sequence seq) {
        if (getNowCreationDate() == seq.getCreationDate()) {
            return seq.getNext(false);
        } else {
            return seq.getNext(true);
        }
    }

    protected String convertToPath(Sequence seq) {
        return "20" + seq.getCreationDate() + PATH_SEPARATOR + bucketFilePrefix + seq.getNumber();
    }

    protected int getDateFromPath(String path) {
        return Integer.valueOf(path.split(PATH_SEPARATOR)[0]);
    }

    protected Sequence convertToSequence(String path) {
        String[] parts = path.split(PATH_SEPARATOR);
        return new Sequence(Integer.valueOf(parts[0].substring(2)), Integer.valueOf(parts[1].substring(bucketFilePrefix
                .length())));
    }

    public void stop() {
        if (stopped) {
            return;
        }
        stopped = true;
    }

    protected void checkClosed() throws StorageClosedException {
        if (stopped) {
            throw new StorageClosedException("Bucket index has been closed.");
        }
    }

    public boolean hasNexReadBucket(Sequence sequence) throws StorageClosedException {
        checkClosed();
        NavigableMap<Sequence, String> tailMap = index.get().tailMap(sequence, false);

        return !tailMap.isEmpty();
    }

    public int size() {
        return index.get().size();
    }

    public void add(List<String> paths) throws StorageClosedException {
        checkClosed();
        TreeMap<Sequence, String> newIndexes = new TreeMap<Sequence, String>(index.get());

        for (String path : paths) {
            newIndexes.put(convertToSequence(path), path);
        }

        index.set(newIndexes);
    }

    public List<String> bulkGetRemainN(int remainSize) throws StorageClosedException {
        checkClosed();
        List<String> results = new ArrayList<String>();
        TreeMap<Sequence, String> bakIndexes = index.get();

        int i = 0;
        for (Entry<Sequence, String> entry : bakIndexes.entrySet()) {
            if (i < index.get().size() - remainSize) {
                results.add(entry.getValue());
            } else {
                break;
            }
            i++;
        }
        return results;
    }

    @Override
    public List<String> bulkGetRemainNDay(int remainDay) throws StorageClosedException {
        checkClosed();
        List<String> results = new ArrayList<String>();
        TreeMap<Sequence, String> bakIndexes = index.get();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -1 * (remainDay - 1));
        int preservedFromDay = Integer.valueOf(sdf.format(cal.getTime()));

        for (Entry<Sequence, String> entry : bakIndexes.entrySet()) {
            String path = entry.getValue();
            int date = getDateFromPath(path);
            if (date < preservedFromDay) {
                results.add(path);
            }
        }
        return results;
    }

    @Override
    public void remove(List<String> paths) throws StorageClosedException {
        checkClosed();
        TreeMap<Sequence, String> newIndexes = new TreeMap<Sequence, String>(index.get());

        for (String path : paths) {
            newIndexes.remove(convertToSequence(path));
        }

        index.set(newIndexes);
    }

    public String getBaseDir() {
        return baseDir;
    }

    public void copyFromLocal(String srcBaseDir, String path) throws IOException, StorageClosedException {
        checkClosed();
    }

    @Override
    public boolean removeBucket(String path) throws StorageClosedException {
        checkClosed();
        return true;
    }

    @Override
    public void updateLatestSequence(Sequence sequence) {
        this.latestSequence.set(sequence);
    }

    @Override
    public Bucket getReadBucket(long seq, boolean start) throws StorageClosedException, IOException {
        checkClosed();
        Sequence sequence = null;
        String path = null;

        if (seq == -1L) {
            // 从最老开始消费
            if (!index.get().isEmpty()) {
                path = index.get().firstEntry().getValue();
                if (path == null) {
                    return null;
                }
                sequence = new Sequence(index.get().firstEntry().getKey());
            } else {
                return null;
            }
        } else if (seq == -2L) {
            // 从最新开始消费
            if (this.latestSequence.get() != null) {
                sequence = new Sequence(this.latestSequence.get());
                path = convertToPath(sequence);
            } else {
                return null;
            }
        } else {
            sequence = new Sequence(seq);
            path = index.get().get(sequence);
            if (path == null) {
                return null;
            }

        }

        int offset = sequence.getOffset();
        Bucket bucket = doGetReadBucket(baseDir, path, sequence.clearOffset(), maxBucketLengthMB);

        if (bucket != null) {
            byte[] headData = bucket.getNext();
            if (headData.length != COMPRESS_HEAD) {
                if (start) {
                    bucket.seek(0);
                    return bucket;
                } else {
                    if (seq != -1 && seq != -2) {
                        bucket.seek(offset);
                        bucket.getNext();
                        return bucket;
                    } else {
                        bucket.seek(offset);
                        return bucket;
                    }
                }
            } else {
                String head = new String(headData);
                if (head.equals(ZIPFORMAT)) {
                    bucket.setIsCompress(true);
                    if (seq != -1L && !start) {
                        ArrayList<ZipIndexItem> zipIndex = readZipIndex(this.getBaseDir(), path + this.zipIndexsuffix);
                        long off = findZipFileOffset(sequence, zipIndex);
                        bucket.seek(off);
                        while (true) {
                            try {
                                byte[] lookupdata = bucket.getNext();
                                ChangedEvent event = this.compressor.getEvent(lookupdata);
                                if (event.getSeq() == seq) {
                                    return bucket;
                                }
                            } catch (EOFException e) {
                                return null;
                            }
                        }
                    } else {
                        bucket.seek(COMPRESS_HEAD + 4);
                        return bucket;
                    }
                } else {
                    if (start) {
                        bucket.seek(0);
                        return bucket;
                    } else {
                        if (seq != -1 && seq != -2) {
                            bucket.seek(offset);
                            bucket.getNext();
                            return bucket;
                        } else {
                            bucket.seek(offset);
                            return bucket;
                        }
                    }
                }
            }
        }

        return bucket;
    }

    public long findZipFileOffset(Sequence seq, ArrayList<ZipIndexItem> zipIndex) {
        int size = zipIndex.size();
        for (int i = 0; i < size; i++) {
            if (zipIndex.get(i).getBeginseq() <= seq.longValue() && zipIndex.get(i).getEndseq() >= seq.longValue()) {
                return zipIndex.get(i).getOffset();
            }
        }
        return -1;
    }

    public void writeZipIndex(ArrayList<ZipIndexItem> zipIndex, OutputStream ios) throws IOException {
        Properties properties = new Properties();
        for (int i = 0; i < zipIndex.size(); i++) {
            properties.put(
                    String.valueOf(zipIndex.get(i).getBeginseq()) + ZIPINDEX_SEPARATOR
                            + String.valueOf(zipIndex.get(i).getEndseq()), String.valueOf(zipIndex.get(i).getOffset()));
        }
        properties.store(ios, "store zipIndex");
    }
}
