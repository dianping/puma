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
package com.dianping.puma.storage.bucket;

import java.io.IOException;
import java.util.List;

import com.dianping.puma.core.LifeCycle;
import com.dianping.puma.storage.Sequence;
import com.dianping.puma.storage.exception.StorageClosedException;

/**
 * Bucket索引
 * 
 * @author Leo Liang
 * 
 */
public interface DataBucketManager extends LifeCycle<IOException> {

    public void add(DataBucket bucket) throws StorageClosedException;

    public void add(List<String> paths) throws StorageClosedException;

    public List<String> bulkGetRemainN(int remainSize) throws StorageClosedException;

    public List<String> bulkGetRemainNDay(int remainDay) throws StorageClosedException;

    public DataBucket getNextReadBucket(Sequence sequence) throws StorageClosedException, IOException;

    public DataBucket getNextWriteBucket() throws StorageClosedException, IOException;

    public boolean hasNexReadBucket(Sequence sequence) throws StorageClosedException;

    public DataBucket getReadBucket(long seq, boolean fromNext) throws StorageClosedException, IOException;

    public int size();

    public String getBaseDir();

    public void setMaster(boolean isMaster);

    public void copyFromLocal(String baseDir, String path) throws StorageClosedException, IOException;

    public boolean removeBucket(String path) throws StorageClosedException;

    public void remove(List<String> paths) throws StorageClosedException;

    public void updateLatestSequence(Sequence sequence);

}
