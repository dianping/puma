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
package com.dianping.puma.syncserver.job;

/**
 * @author Leo Liang
 * 
 */
public interface TaskExecutor {
    public String getTaskName();

    public TaskExecutionResult exec() throws TaskExecutionException;

    public TaskExecutionResult beforeExec() throws TaskExecutionException;

    public TaskExecutionResult afterExec() throws TaskExecutionException;

    public long getJobId();
}
