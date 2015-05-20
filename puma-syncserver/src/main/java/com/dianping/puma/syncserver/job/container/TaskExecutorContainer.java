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

import com.dianping.puma.syncserver.job.container.exception.TECException;
import com.dianping.puma.syncserver.job.executor.TaskExecutor;

import java.util.List;

public interface TaskExecutorContainer {

    public void submit(String name, TaskExecutor taskExecutor) throws TECException;

    public void withdraw(String name) throws TECException;

    public void start(String name) throws TECException;

    public void stop(String name) throws TECException;

    public TaskExecutor get(String name);

    public List<TaskExecutor> getAll();

    public int size();

    //public void submit(TaskExecutor taskExecutor) throws TaskExecutionException;

    //public TaskExecutor get(String taskName);

    //public void changeStatus(String taskName, ActionController controller);

    //public void deleteSyncTask(String taskName);

    //public void deleteShardSyncTask(String taskName);

    //public void deleteShardDumpTask(String taskName);

    //public List<TaskExecutor> toList();

    //public List<TaskExecutor> getAll();
}
