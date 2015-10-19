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

import com.dianping.puma.storage.oldbucket.DataBucketManager;
import com.dianping.puma.storage.oldindex.WriteIndexManager;

/**
 * TODO Comment of CleanupStrategy
 * 
 * @author Leo Liang
 * 
 */
public interface CleanupStrategy {
    public void cleanup(DataBucketManager index);

    @SuppressWarnings("rawtypes")
    public void addDataIndex(WriteIndexManager index);
}
