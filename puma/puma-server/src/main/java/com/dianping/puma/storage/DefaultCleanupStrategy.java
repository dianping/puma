/**
 * Project: puma-server
 * 
 * File Created at 2012-8-6
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.dianping.puma.storage.exception.StorageClosedException;

/**
 * TODO Comment of DefaultCleanupStrategy
 * 
 * @author Leo Liang
 * 
 */
public class DefaultCleanupStrategy implements CleanupStrategy {
	private int				preservedDay		= 14;
	private List<String>	toBeDeleteBuckets	= new ArrayList<String>();

	public void setPreservedDay(int preservedDay) {
		this.preservedDay = preservedDay;
	}

	@Override
	public void cleanup(BucketIndex index, BinlogIndexManager binlogIndexManager) {
		try {
			toBeDeleteBuckets.addAll(index.bulkGetRemainNDay(preservedDay));

			if (!toBeDeleteBuckets.isEmpty()) {
				index.remove(toBeDeleteBuckets);

				Iterator<String> iterator = toBeDeleteBuckets.iterator();
				while (iterator.hasNext()) {
					String path = iterator.next();
					if (StringUtils.isNotBlank(path)) {
						if (index.removeBucket(path)) {
							binlogIndexManager.deleteBinlogIndex(path);
							iterator.remove();
						}
					}
				}
			}
		} catch (StorageClosedException e) {
			// ignore
		}

	}
}
