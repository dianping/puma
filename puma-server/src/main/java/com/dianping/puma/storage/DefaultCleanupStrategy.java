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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.dianping.puma.storage.bucket.DataBucketManager;
import com.dianping.puma.storage.exception.StorageClosedException;
import com.dianping.puma.storage.index.IndexManager;

/**
 * TODO Comment of DefaultCleanupStrategy
 * 
 * @author Leo Liang
 * 
 */
public class DefaultCleanupStrategy implements CleanupStrategy {
    private int             preservedDay      = 14;
    private List<String>    toBeDeleteBuckets = new ArrayList<String>();
    @SuppressWarnings("rawtypes")
    private List<IndexManager> dataIndexes       = new ArrayList<IndexManager>();

    public void setPreservedDay(int preservedDay) {
        this.preservedDay = preservedDay;
    }

    @SuppressWarnings("rawtypes")
    public void addDataIndex(IndexManager index) {
        this.dataIndexes.add(index);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void cleanup(DataBucketManager index) {
        try {
            toBeDeleteBuckets.addAll(index.bulkGetRemainNDay(preservedDay));

            if (!toBeDeleteBuckets.isEmpty()) {
                index.remove(toBeDeleteBuckets);
                for (String path : toBeDeleteBuckets) {
                    if (dataIndexes != null && !dataIndexes.isEmpty()) {
                        for (IndexManager dataIndex : dataIndexes) {
                            try {
                                dataIndex.removeByL2IndexName(path.replace('/', '-'));
                            } catch (IOException e) {
                                // ignore
                            }
                        }
                    }
                }

                Iterator<String> iterator = toBeDeleteBuckets.iterator();
                while (iterator.hasNext()) {
                    String path = iterator.next();
                    if (StringUtils.isNotBlank(path)) {
                        if (index.removeBucket(path)) {
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
