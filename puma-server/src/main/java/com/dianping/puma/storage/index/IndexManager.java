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

import com.dianping.puma.core.LifeCycle;

import java.io.IOException;
import java.util.TreeMap;

/**
 * @author Leo Liang
 */
public interface IndexManager<K extends IndexKey<K>, V> extends LifeCycle<IOException> {

    void addL1Index(K key, String l2IndexName) throws IOException;

    TreeMap<K, String> getL1Index();

    void addL2Index(K key, V value) throws IOException;

    void removeByL2IndexName(String l2IndexName) throws IOException;

    void flush() throws IOException;

    IndexBucket<K, V> getIndexBucket(String fileName) throws IOException;

    IndexBucket<K, V> getIndexBucket(K key, boolean inclusive) throws IOException;

    boolean hasNextIndexBucket(K key) throws IOException;

    IndexBucket<K, V> getNextIndexBucket(K key) throws IOException;

    K findFirst() throws IOException;

    K findLatest() throws IOException;

    K findByTime(K searchKey, boolean startWithCompleteTransaction) throws IOException;

    K findByBinlog(K searchKey, boolean startWithCompleteTransaction) throws IOException;

}
