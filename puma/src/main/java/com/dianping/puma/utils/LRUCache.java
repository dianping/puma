/**
 * Project: ${puma-common.aid}
 * 
 * File Created at 2012-6-25
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
package com.dianping.puma.utils;

import com.dianping.puma.annotation.ThreadUnSafe;

import java.util.LinkedHashMap;
import java.util.Map.Entry;

/**
 * TODO Comment of LRUCache
 * 
 * @author Leo Liang
 * 
 */
@ThreadUnSafe
public class LRUCache<K, V> extends LinkedHashMap<K, V> {
	private static final long	serialVersionUID	= 6763345531545815287L;
	private int					maxElements;

	public LRUCache(int maxSize) {
		super(maxSize, 0.75f, true);
		this.maxElements = maxSize;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.LinkedHashMap#removeEldestEntry(java.util.Map.Entry)
	 */
	protected boolean removeEldestEntry(Entry<K, V> eldest) {
		return (size() > this.maxElements);
	}

}
