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
package com.dianping.puma.syncserver.job.container;

import com.dianping.puma.core.constant.Controller;
import com.dianping.puma.core.constant.SyncType;
import com.dianping.puma.core.monitor.SyncTaskOperationEvent;
import com.dianping.puma.core.sync.model.task.SyncTaskStatusAction;
import com.dianping.puma.core.sync.model.task.Type;
import com.dianping.puma.syncserver.job.executor.TaskExecutionException;
import com.dianping.puma.syncserver.job.executor.TaskExecutor;

import java.util.List;

/**
 * @author Leo Liang
 */
@SuppressWarnings("rawtypes")
public interface TaskExecutionContainer {

    public void submit(TaskExecutor taskExecutor) throws TaskExecutionException;

    public TaskExecutor get(SyncType syncType, String taskName);

    public void changeStatus(String taskName, Controller controller);

    public void deleteSyncTask(String taskName);

    public List<TaskExecutor> toList();
}
