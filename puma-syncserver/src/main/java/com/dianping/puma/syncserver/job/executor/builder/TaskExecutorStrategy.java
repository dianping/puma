/**
 * Project: puma-syncserver
 * 
 * File Created at 2013-1-16
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
package com.dianping.puma.syncserver.job.executor.builder;

import com.dianping.puma.core.constant.SyncType;
import com.dianping.puma.biz.entity.BaseSyncTask;
import com.dianping.puma.biz.sync.model.task.Type;
import com.dianping.puma.syncserver.job.executor.TaskExecutor;

/**
 * @author wukezhu
 */
@SuppressWarnings("rawtypes")
public interface TaskExecutorStrategy<T extends BaseSyncTask, R extends TaskExecutor> {
    public R build(T task);

    public Type getType();

    public SyncType getSyncType();

}
