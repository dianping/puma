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

/**
 * Bucket索引
 * 
 * @author Leo Liang
 * 
 */
public interface BucketIndex {
	public void init() throws Exception;

	public void add(Bucket bucket);

	public void add(List<String> paths);

	public List<String> bulkGetRemainN(int remainSize);

	public Bucket getNextReadBucket(Sequence sequence) throws IOException;

	public Bucket getNextWriteBucket() throws IOException;

	public boolean hasNexReadBucket(Sequence sequence) throws IOException;

	public Bucket getReadBucket(long seq) throws IOException;

	public int size();

	public void close();

	public String getBaseDir();

	public void copyFromLocal(String baseDir, String path) throws IOException;

	public void remove(List<String> paths);

}
