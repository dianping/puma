/**
 * Project: puma-server
 * 
 * File Created at 2012-7-20
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
package com.dianping.puma.syncserver.holder;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.dianping.puma.syncserver.job.executor.CatchupTaskExecutor;
import com.dianping.puma.syncserver.job.executor.DumpTaskExecutor;
import com.dianping.puma.syncserver.job.executor.SyncTaskExecutor;

/**
 * @author wukezhu
 */
public enum SystemStatusContainer {
    instance;

    private ConcurrentMap<Long, SyncTaskExecutor> syncTaskExecutors = new ConcurrentHashMap<Long, SyncTaskExecutor>();
    private ConcurrentMap<Long, DumpTaskExecutor> dumpTaskExecutors = new ConcurrentHashMap<Long, DumpTaskExecutor>();
    private ConcurrentMap<Long, CatchupTaskExecutor> catchupTaskExecutors = new ConcurrentHashMap<Long, CatchupTaskExecutor>();

}
