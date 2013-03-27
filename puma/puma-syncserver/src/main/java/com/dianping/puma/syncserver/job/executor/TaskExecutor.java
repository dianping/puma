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

import com.dianping.puma.core.sync.model.task.Task;
import com.dianping.puma.core.sync.model.taskexecutor.TaskExecutorStatus;

public interface TaskExecutor<T extends Task> {

    /** 开始任务，状态设置为运行中 */
    void start();

    /** 暂停任务，状态设置为暂停 */
    void pause(String detail);

    /** 结束任务，状态设置为成功 */
    void succeed();

    //    void fail(String detail);

    /***
     * 获取任务的状态
     */
    TaskExecutorStatus getTaskExecutorStatus();

    /**
     * 获取任务的Task配置
     */
    T getTask();

    /** 结束任务，状态设置为成功 */
    void stop(String detail);

    //    void disconnect(String detail);
}
