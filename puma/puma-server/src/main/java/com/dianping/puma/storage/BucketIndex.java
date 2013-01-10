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

import java.io.IOException;
import java.util.List;

import com.dianping.puma.common.LifeCycle;
import com.dianping.puma.storage.exception.StorageClosedException;

/**
 * Bucket索引
 * 
 * @author Leo Liang
 * 
 */
public interface BucketIndex extends LifeCycle<IOException> {

    public static final long SEQ_FROM_OLDEST = -1L;
    public static final long SEQ_FROM_LATEST = -2L;

    public void add(Bucket bucket) throws StorageClosedException;

    public void add(List<String> paths) throws StorageClosedException;

    public List<String> bulkGetRemainN(int remainSize) throws StorageClosedException;

    public List<String> bulkGetRemainNDay(int remainDay) throws StorageClosedException;

    public Bucket getNextReadBucket(Sequence sequence) throws StorageClosedException, IOException;

    public Bucket getNextWriteBucket() throws StorageClosedException, IOException;

    public boolean hasNexReadBucket(Sequence sequence) throws StorageClosedException;

    public Bucket getReadBucket(long seq) throws StorageClosedException, IOException;

    public int size();

    public String getBaseDir();

    public void copyFromLocal(String baseDir, String path) throws StorageClosedException, IOException;

    public boolean removeBucket(String path) throws StorageClosedException;

    public void remove(List<String> paths) throws StorageClosedException;

    public void updateLatestSequence(Sequence sequence);

}
