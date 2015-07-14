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

import com.dianping.puma.core.LifeCycle;

/**
 * @author Leo Liang
 * 
 */
public interface DataIndex<K extends DataIndexKey<K>, V> extends LifeCycle<IOException> {

	public void addL1Index(K key, String l2IndexName) throws IOException;

	public void addL2Index(K key, V value) throws IOException;

	public void removeByL2IndexName(String l2IndexName) throws IOException;

	/**
	 * 
	 * @param startPos {@code SubscribeConstant}
	 * @param key
	 * @return
	 * @throws IOException
	 */
	public IndexBucket<K, V> getIndexBucket(long startPos, K key) throws IOException;
	
	public IndexBucket<K, V> getNextIndexBucket(K key) throws IOException;
}
