/**
 * Project: puma-syncserver
 * 
 * File Created at 2013-1-17
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

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.puma.core.util.PumaThreadUtils;

/**
 * 
 * @author Leo Liang
 * 
 */
public class DefaultTaskContainerImpl implements TaskExecutionContainer {
    private static final Logger log      = LoggerFactory.getLogger(DefaultTaskContainerImpl.class);
    private List<JobContext>    taskList = new ArrayList<JobContext>();
    private volatile boolean    stopped  = true;

    @Override
    public void start() throws TaskExecutionException {
        stopped = false;
    }

    @Override
    public void stop() throws TaskExecutionException {
        stopped = true;
    }

    @Override
    public void submitTask(final TaskExecutor job) throws TaskExecutionException {
        if (!stopped) {
//            Thread workerThread = PumaThreadUtils.createThread(new Runnable() {
//
//                @Override
//                public void run() {
//                    try {
//                        TaskExecutionResult result = job.exec();
//                    } catch (Exception e) {
//                        log.error("Job exec fail(jobId: {}).", job.getTaskId());
//                    }
//
//                }
//            }, job.getTaskName() + "-" + job.getJobId(), false);
        }
    }

    private static class JobContext {
        Thread workerThread;
    }

}
