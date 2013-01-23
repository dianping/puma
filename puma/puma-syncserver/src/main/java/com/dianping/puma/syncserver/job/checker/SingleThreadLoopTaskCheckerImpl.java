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
package com.dianping.puma.syncserver.job.checker;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.puma.core.util.PumaThreadUtils;
import com.dianping.puma.syncserver.job.executor.TaskExecutionContainer;
import com.dianping.puma.syncserver.job.executor.TaskExecutionException;
import com.dianping.puma.syncserver.job.executor.TaskExecutor;

/**
 * @author Leo Liang
 */
public class SingleThreadLoopTaskCheckerImpl implements TaskChecker {
    private static final Logger log = LoggerFactory.getLogger(SingleThreadLoopTaskCheckerImpl.class);

    private int loopIntervalMilliSec = 100;
    private List<TaskCheckStrategy> taskCheckStrategyList;
    private TaskExecutionContainer taskExecutionContainer;
    private volatile boolean stopped = true;

    public void setLoopIntervalMilliSec(int loopIntervalMilliSec) {
        this.loopIntervalMilliSec = loopIntervalMilliSec;
    }

    /*
     * (non-Javadoc)
     * @see com.dianping.puma.core.LifeCycle#start()
     */
    @Override
    public void start() throws TaskExecutionException {
        stopped = false;
        PumaThreadUtils.createThread(new Runnable() {

            @Override
            public void run() {
                while (!stopped && Thread.currentThread().isInterrupted()) {
                    if (taskCheckStrategyList != null && !taskCheckStrategyList.isEmpty()) {
                        for (TaskCheckStrategy taskCheckStrategy : taskCheckStrategyList) {
                            try {
                                List<TaskExecutor> executors = taskCheckStrategy.check();
                                if (executors != null && !executors.isEmpty()) {
                                    for (TaskExecutor executor : executors) {
                                        try {
                                            taskExecutionContainer.submitTask(executor);
                                        } catch (Exception e) {
                                            log.error("Exception occurs while submitting task({})", executor.getTaskId(), e);
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                log.error("Exception occurs in taskCheckStrategy({}) ", taskCheckStrategy.getName(), e);
                            }
                        }
                    }

                    try {
                        TimeUnit.MILLISECONDS.sleep(loopIntervalMilliSec);
                    } catch (InterruptedException e) {
                        log.info("Thread interrupted({}).", Thread.currentThread().getName());
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }, "SingleThreadLoopTaskCheckerImpl", false).start();
        log.info("SingleThreadLoopTaskCheckerImpl started. ");
    }

    /*
     * (non-Javadoc)
     * @see com.dianping.puma.core.LifeCycle#stop()
     */
    @Override
    public void stop() throws TaskExecutionException {
        stopped = true;
        log.info("SingleThreadLoopTaskCheckerImpl stopped. ");
    }

    @Override
    public void setTaskExecutionContainer(TaskExecutionContainer taskExecutionContainer) {
        this.taskExecutionContainer = taskExecutionContainer;
    }

    @Override
    public void setTaskCheckStrategyList(List<TaskCheckStrategy> taskCheckStrategyList) {
        this.taskCheckStrategyList = taskCheckStrategyList;
    }

}
