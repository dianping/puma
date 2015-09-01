/**
 * Project: puma-server
 * <p/>
 * File Created at 2013-1-8
 * $Id$
 * <p/>
 * Copyright 2010 dianping.com.
 * All rights reserved.
 * <p/>
 * This software is the confidential and proprietary information of
 * Dianping Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with dianping.com.
 */
package com.dianping.puma.storage.index;

import com.dianping.puma.utils.LinkedProperties;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import org.apache.commons.io.FileUtils;
import org.codehaus.plexus.util.StringUtils;

import java.io.*;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

/**
 * @author damon.zhu
 */
public class DefaultIndexManager<K extends IndexKey, V extends IndexValue<K>> implements IndexManager<K, V> {

    private static final int BUF_SIZE = 1024 * 10;

    public static final String L1INDEX_FILENAME = "l1Index.l1idx";

    public static final String L2INDEX_FOLDER = "l2Index";

    public static final String L2INDEX_FILESUFFIX = ".l2idx";

    private String baseDir;

    private BufferedWriter l1IndexWriter;

    private String writingl2IndexName = null;

    private DataOutputStream l2IndexWriter;

    private IndexItemConvertor<K> indexKeyConvertor;

    private IndexItemConvertor<V> indexValueConvertor;

    private ReentrantReadWriteLock l1Lock = new ReentrantReadWriteLock();

    private ReadLock l1ReadLock = l1Lock.readLock();

    private WriteLock l1WriteLock = l1Lock.writeLock();

    private ReentrantReadWriteLock l2Lock = new ReentrantReadWriteLock();

    private WriteLock l2WriteLock = l2Lock.writeLock();

    private AtomicReference<K> latestL2Index = new AtomicReference<K>();

    public DefaultIndexManager(String baseDir, IndexItemConvertor<K> indexKeyConvertor,
                               IndexItemConvertor<V> indexValueConvertor) {
        this.baseDir = baseDir;
        this.indexKeyConvertor = indexKeyConvertor;
        this.indexValueConvertor = indexValueConvertor;
    }

    private File getL2IndexFile(String l2IndexName) {
        return new File(new File(baseDir, L2INDEX_FOLDER), l2IndexName + L2INDEX_FILESUFFIX);
    }

    private File getL1IndexFile() {
        return new File(baseDir, L1INDEX_FILENAME);
    }

    /*
     * for test purpose only
     */
    protected void setLatestL2IndexNull() {
        latestL2Index.getAndSet(null);
    }

    @Override
    public void start() throws IOException {
        createDirIfNeeded();

        File l1IndexFile = getL1IndexFile();
        l1IndexFile.createNewFile();
        l1IndexWriter = new BufferedWriter(new FileWriter(l1IndexFile, true));

        l2WriteLock.lock();
        try {
            writingl2IndexName = null;
            closeQuietly(l2IndexWriter);
            l2IndexWriter = null;
        } finally {
            l2WriteLock.unlock();
        }
    }

    protected LinkedHashMap<K, String> loadLinkedL1Index() throws IOException {
        LinkedHashMap<K, String> l1Index = new LinkedHashMap<K, String>();
        File l1IndexFile = getL1IndexFile();
        l1IndexFile.createNewFile();
        InputStream is = null;
        try {
            LinkedProperties prop = new LinkedProperties();
            is = new FileInputStream(l1IndexFile);
            prop.load(is);
            for (Map.Entry<Object, Object> entry : prop.entrySet()) {
                K key = indexKeyConvertor.convertFromObj(entry.getKey());
                String value = String.valueOf(entry.getValue());
                if (key != null && value != null) {
                    l1Index.put(key, value);
                }
            }
        } finally {
            closeQuietly(is);
        }

        return l1Index;
    }

    private void createDirIfNeeded() throws IOException {
        File folder = new File(baseDir);
        if (!folder.exists()) {
            if (!folder.mkdirs()) {
                throw new IOException(String.format("Create dir(%s) failed.", baseDir));
            }
        }

        File l2IndexFolder = new File(baseDir, L2INDEX_FOLDER);
        if (!l2IndexFolder.exists()) {
            if (!l2IndexFolder.mkdirs()) {
                throw new IOException(String.format("Create dir(%s) failed.", l2IndexFolder.getAbsolutePath()));
            }
        }
    }

    @Override
    public void flush() throws IOException {
        if (this.l2IndexWriter != null) {
            this.l2IndexWriter.flush();
        }
    }

    @Override
    public void stop() throws IOException {
        closeQuietly(l1IndexWriter);
        l1IndexWriter = null;
        closeQuietly(l2IndexWriter);
        writingl2IndexName = null;
        l2IndexWriter = null;
    }

    private void closeQuietly(Writer out) {
        if (out != null) {
            try {
                out.close();
            } catch (Exception e) {
                // ignore
            }
        }
    }

    private void closeQuietly(OutputStream out) {
        if (out != null) {
            try {
                out.close();
            } catch (Exception e) {
                // ignore
            }
        }
    }

    private void closeQuietly(InputStream in) {
        if (in != null) {
            try {
                in.close();
            } catch (Exception e) {
                // ignore
            }
        }
    }

    @Override
    public void addL1Index(K key, String l2IndexName) throws IOException {
        boolean added = false;
        LinkedHashMap<K, String> l1Index = loadLinkedL1Index();
        l1WriteLock.lock();
        try {
            if (!l1Index.containsKey(key)) {
                appendL1IndexToFile(key, l2IndexName);
                added = true;
            }
        } finally {
            l1WriteLock.unlock();
        }

        l2WriteLock.lock();
        try {
            if (added && !StringUtils.equals(l2IndexName, writingl2IndexName)) {
                writingl2IndexName = l2IndexName;
                closeQuietly(l2IndexWriter);
                File l2IndexFile = getL2IndexFile(writingl2IndexName);
                l2IndexFile.createNewFile();
                l2IndexWriter = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(l2IndexFile), BUF_SIZE));
            }
        } finally {
            l2WriteLock.unlock();
        }
    }

    private void appendL1IndexToFile(K key, String l2IndexName) throws IOException {
        l1IndexWriter.write(indexKeyConvertor.convertToObj(key) + "=" + l2IndexName);
        l1IndexWriter.newLine();
        l1IndexWriter.flush();
    }

    @Override
    public void addL2Index(K key, V value) throws IOException {
        l2WriteLock.lock();
        try {
            if (l2IndexWriter != null) {
                Object object = indexValueConvertor.convertToObj(value);

                if (object instanceof byte[]) {
                    byte[] bytes = (byte[]) object;

                    l2IndexWriter.writeInt(bytes.length);
                    l2IndexWriter.write(bytes);
                }

                latestL2Index.set(key);
            }
        } finally {
            l2WriteLock.unlock();
        }
    }

    @Override
    public void removeByL2IndexName(String l2IndexName) throws IOException {
        boolean removed = false;
        LinkedHashMap<K, String> l1Index = loadLinkedL1Index();

        l1WriteLock.lock();
        try {
            if (l1Index.containsValue(l2IndexName)) {
                K tobeRemoveKey = null;
                for (K key : l1Index.keySet()) {
                    if (l2IndexName.equals(l1Index.get(key))) {
                        tobeRemoveKey = key;
                        break;
                    }
                }

                if (tobeRemoveKey != null) {
                    l1Index.remove(tobeRemoveKey);
                    removed = true;
                    closeQuietly(l1IndexWriter);
                    l1IndexWriter = null;
                    FileUtils.deleteQuietly(getL1IndexFile());
                    l1IndexWriter = new BufferedWriter(new FileWriter(getL1IndexFile()));
                    for (Map.Entry<K, String> entry : l1Index.entrySet()) {
                        appendL1IndexToFile(entry.getKey(), entry.getValue());
                    }
                }
            }
        } finally {
            l1WriteLock.unlock();
        }

        if (removed) {
            l2WriteLock.lock();
            try {
                if (StringUtils.equals(l2IndexName, writingl2IndexName)) {
                    writingl2IndexName = null;
                    closeQuietly(l2IndexWriter);
                    l2IndexWriter = null;
                }
                File l2IndexFile = getL2IndexFile(l2IndexName);
                FileUtils.deleteQuietly(l2IndexFile);
            } finally {
                l2WriteLock.unlock();
            }
        }
    }

    public boolean hasNextIndexBucket(String fileName) throws IOException {
        LinkedList<String> files = Lists.newLinkedList(loadLinkedL1Index().values());

        if (files.isEmpty()) {
            return false;
        }

        int index = files.indexOf(fileName);
        if (index < 0) {
            return false;
        }

        return index < files.size() - 1;
    }

    public IndexBucket<K, V> getNextIndexBucket(String fileName) throws IOException {
        LinkedList<String> files = Lists.newLinkedList(loadLinkedL1Index().values());

        if (files.isEmpty()) {
            return null;
        }

        int index = files.indexOf(fileName);
        if (index < 0 || index == files.size() + 1) {
            return null;
        }
        String l2Index = files.get(index + 1);

        if (l2Index == null) {
            return null;
        }

        File l2IndexFile = getL2IndexFile(l2Index);
        LocalFileIndexBucket<K, V> localFileIndexBucket = new LocalFileIndexBucket<K, V>(l2Index, l2IndexFile,
                this.indexValueConvertor);

        return localFileIndexBucket;
    }

    @Override
    public V findFirst() throws IOException {
        LinkedHashMap<K, String> l1Index = loadLinkedL1Index();
        if (l1Index.isEmpty()) {
            return null;
        } else {
            Entry<K, String> firstEntry = FluentIterable.from(l1Index.entrySet()).first().get();
            LocalFileIndexBucket<K, V> bucket = new LocalFileIndexBucket<K, V>(
                    firstEntry.getValue(), getL2IndexFile(firstEntry.getValue()), this.indexValueConvertor);
            bucket.start();
            return bucket.next();
        }
    }

    @Override
    public V findLatest() throws IOException {
        LinkedHashMap<K, String> l1Index = loadLinkedL1Index();
        if (l1Index.isEmpty()) {
            return null;
        } else {
            Entry<K, String> lastEntry = FluentIterable.from(l1Index.entrySet()).last().get();

            LocalFileIndexBucket<K, V> bucket = new LocalFileIndexBucket<K, V>(
                    lastEntry.getValue(), getL2IndexFile(lastEntry.getValue()), this.indexValueConvertor);
            bucket.start();

            V next = null;
            try {
                while (true) {
                    next = bucket.next();
                }
            } catch (EOFException ignore) {
            } finally {
                bucket.stop();
            }

            if (next != null) {
                return next;
            } else {
                return null;
            }
        }
    }

    @Override
    public V findByTime(K startKey, boolean startWithCompleteTransaction) throws IOException {
        LinkedHashMap<K, String> l1Index = loadLinkedL1Index();

        if (l1Index.isEmpty()) {
            return null;
        }

        LinkedList<K> keys = Lists.newLinkedList(l1Index.keySet());
        K matches = null;
        for (int k = keys.size() - 1; k >= 0; k--) {
            if (keys.get(k).getTimestamp() < startKey.getTimestamp()) {
                matches = keys.get(k);
                break;
            }
        }

        if (matches == null) {
            return null;
        }

        LocalFileIndexBucket<K, V> bucket = new LocalFileIndexBucket<K, V>(l1Index.get(matches), getL2IndexFile(l1Index.get(matches)),
                this.indexValueConvertor);
        bucket.start();

        V next = null;
        LinkedList<V> entries = new LinkedList<V>();

        try {
            while (true) {
                next = bucket.next();

                if (next.getIndexKey().getTimestamp() >= startKey.getTimestamp()) {
                    break;
                } else {
                    entries.add(next);
                }
            }
        } catch (EOFException ignore) {
        } finally {
            bucket.stop();
        }

        if (startWithCompleteTransaction) {
            // 从队列中查找上一个transaction commit位置，从这个点之后的一定是完整的
            V last = entries.pollLast();

            while (last != null) {
                if (last.isTransactionCommit()) {
                    return last;
                } else {
                    last = entries.pollLast();
                }
            }
            // 如果仍然没有找到
            return findFromPreviosIndexBucketByTime(matches);
        } else {
            return entries.pollLast();
        }
    }

    private V findFromPreviosIndexBucketByTime(K startKey) throws IOException {
        LinkedHashMap<K, String> l1Index = loadLinkedL1Index();
        if (l1Index.isEmpty()) {
            return null;
        }

        LinkedList<K> keys = Lists.newLinkedList(l1Index.keySet());
        int index = keys.indexOf(startKey);

        if (index == 0) {
            return null;
        }

        K lastKey = keys.get(index - 1);

        LocalFileIndexBucket<K, V> bucket = new LocalFileIndexBucket<K, V>(l1Index.get(lastKey), getL2IndexFile(l1Index.get(lastKey)),
                this.indexValueConvertor);
        bucket.start();

        V commitValue = null;

        try {
            while (true) {
                V next = bucket.next();

                if (next.isTransactionCommit()) {
                    commitValue = next;
                }
            }
        } catch (EOFException ignore) {
        } finally {
            bucket.stop();
        }

        if (commitValue != null) {
            return commitValue;
        } else {
            return findFromPreviosIndexBucketByTime(lastKey);
        }
    }

    @Override
    public V findByBinlog(K startKey, boolean startWithCompleteTransaction) throws IOException {
        if (this.l2IndexWriter != null) {
            this.l2IndexWriter.flush();
        }

        LinkedHashMap<K, String> l1Index = loadLinkedL1Index();

        if (l1Index != null) {
            l1ReadLock.lock();

            try {
                if (l1Index.isEmpty()) {
                    return null;
                } else {
                    Entry<K, String> target = null;

                    for (Entry<K, String> entry : l1Index.entrySet()) {
                        K key = entry.getKey();

                        if (key.getServerId() == startKey.getServerId()) {
                            if (compareTo(startKey, key) >= 0) {

                                if (target == null) {
                                    target = entry;
                                } else {
                                    if (compareTo(key, target.getKey()) >= 0) {
                                        target = entry;
                                    }
                                }
                            }
                        }
                    }

                    if (target == null) {
                        return null;
                    }

                    LocalFileIndexBucket<K, V> bucket = new LocalFileIndexBucket<K, V>(target.getValue(), getL2IndexFile(target.getValue()),
                            this.indexValueConvertor);
                    bucket.start();

                    V next = null;
                    LinkedList<V> entries = new LinkedList<V>();

                    try {
                        while (true) {
                            next = bucket.next();

                            if (compareTo(next.getIndexKey(), startKey) > 0) {
                                break;
                            } else {
                                entries.add(next);
                            }
                        }
                    } catch (EOFException ignore) {
                    } finally {
                        bucket.stop();
                    }

                    if (startWithCompleteTransaction) {
                        // 从队列中查找上一个transaction commit位置，从这个点之后的一定是完整的
                        V last = entries.pollLast();

                        while (last != null) {
                            if (last.isTransactionCommit()) {
                                return last;
                            } else {
                                last = entries.pollLast();
                            }
                        }

                        // 如果仍然没有找到
                        return findFromPreviosIndexBucketByBinlog(target.getKey());
                    } else {
                        return entries.pollLast();
                    }
                }
            } finally {
                l1ReadLock.unlock();
            }
        } else {
            return null;
        }
    }

    private V findFromPreviosIndexBucketByBinlog(K startKey) throws IOException {
        Entry<K, String> target = null;

        LinkedHashMap<K, String> l1Index = loadLinkedL1Index();

        for (Entry<K, String> entry : l1Index.entrySet()) {
            K key = entry.getKey();

            if (key.getServerId() == startKey.getServerId()) {
                if (compareTo(startKey, key) > 0) {

                    if (target == null) {
                        target = entry;
                    } else {
                        if (compareTo(key, target.getKey()) > 0) {
                            target = entry;
                        }
                    }
                }
            }
        }

        if (target == null) {
            return null;
        }

        LocalFileIndexBucket<K, V> bucket = new LocalFileIndexBucket<K, V>(target.getValue(), getL2IndexFile(target.getValue()),
                this.indexValueConvertor);
        bucket.start();

        V commitValue = null;

        try {
            while (true) {
                V next = bucket.next();

                if (next.isTransactionCommit()) {
                    commitValue = next;
                }
            }
        } catch (EOFException ignore) {
        } finally {
            bucket.stop();
        }

        if (commitValue != null) {
            return commitValue;
        } else {
            return findFromPreviosIndexBucketByBinlog(target.getKey());
        }
    }

    private int compareTo(K key1, K key2) {
        if (key1.getServerId() == key2.getServerId()) {
            if (key1.getBinlogFile().equals(key2.getBinlogFile())) {
                if (key1.getBinlogPosition() == key2.getBinlogPosition()) {
                    return 0;
                } else {
                    return key1.getBinlogPosition() > key2.getBinlogPosition() ? 1 : -1;
                }
            } else {
                return key1.getBinlogFile().compareTo(key2.getBinlogFile());
            }
        } else {
            return key1.getServerId() > key2.getServerId() ? 1 : -1;
        }
    }

    @Override
    public IndexBucket<K, V> getIndexBucket(String fileName) throws IOException {
        File l2IndexFile = getL2IndexFile(fileName);
        LocalFileIndexBucket<K, V> localFileIndexBucket = new LocalFileIndexBucket<K, V>(fileName, l2IndexFile,
                this.indexValueConvertor);

        return localFileIndexBucket;
    }
}
