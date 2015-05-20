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
package com.dianping.puma.syncserver.job.executor;

import com.dianping.puma.core.entity.BaseSyncTask;
import com.dianping.puma.core.model.state.BaseSyncTaskState;
import com.dianping.puma.core.model.state.TaskState;
import com.dianping.puma.core.sync.model.taskexecutor.TaskExecutorStatus;
import com.dianping.puma.syncserver.job.executor.status.Status;

public interface TaskExecutor<T extends BaseSyncTask> {

    void start();

    void stop();

    T getTask();
}
