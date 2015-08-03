/**
 * Project: puma-server
 * 
 * File Created at 2013-1-8
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
package com.dianping.puma.storage.index;

import java.io.IOException;
import java.util.TreeMap;

import com.dianping.puma.core.LifeCycle;

/**
 * @author Leo Liang
 * 
 */
public interface IndexManager<K extends IndexKey<K>, V> extends LifeCycle<IOException> {

	public void addL1Index(K key, String l2IndexName) throws IOException;
	
	public TreeMap<K,String> getL1Index();

	public void addL2Index(K key, V value) throws IOException;

	public void removeByL2IndexName(String l2IndexName) throws IOException;
	
	public void flush() throws IOException;
	
	public IndexBucket<K, V> getIndexBucket(String fileName) throws IOException;

	/**
	 * 
	 * @param startPos
	 *           {@code SubscribeConstant}
	 * @param key
	 * @return
	 * @throws IOException
	 */
	public IndexBucket<K, V> getIndexBucket(K key, boolean inclusive) throws IOException;

	public boolean hasNextIndexBucket(K key) throws IOException;

	public IndexBucket<K, V> getNextIndexBucket(K key) throws IOException;

	public K findFirst() throws IOException;

	public K findLatest() throws IOException;

	public K findByTime(K searchKey, boolean startWithCompleteTransaction) throws IOException;

	public K findByBinlog(K searchKey, boolean startWithCompleteTransaction) throws IOException;

}
