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

import com.dianping.puma.core.sync.model.BinlogInfo;
import com.dianping.puma.core.sync.model.task.TaskState;

/**
 * @author Leo Liang
 */
public interface TaskExecutor {

    long getTaskId();

    /** 开始 */
    void start();

    void pause();

    void succeed();

    void fail();

    BinlogInfo getCurBinlogInfo();

    TaskState getTaskState();
}
