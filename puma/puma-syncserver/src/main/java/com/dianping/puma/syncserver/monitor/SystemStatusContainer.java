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
package com.dianping.puma.syncserver.monitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;

import com.dianping.puma.core.sync.model.notify.TaskStatusEvent;
import com.dianping.puma.core.sync.model.notify.TaskStatusEvent.Status;
import com.dianping.puma.core.sync.model.task.Type;
import com.dianping.puma.core.sync.model.taskexecutor.TaskStatus;
import com.dianping.puma.syncserver.conf.Config;
import com.dianping.puma.syncserver.job.container.DefaultTaskExecutorContainer;
import com.dianping.puma.syncserver.job.executor.TaskExecutor;

/**
 * @author wukezhu
 */
//TODO 对taskExecutorMap进行状态的监控，每隔n秒记录和处理状态(binlog,state)：更新数据库+通知报警(如果有fail的任务)
public class SystemStatusContainer {

    @Autowired
    private DefaultTaskExecutorContainer taskExecutorContainer;
    @Autowired
    private Config config;

    @SuppressWarnings("rawtypes")
    public TaskStatusEvent getTaskStatusEvents() {
        TaskStatusEvent event = new TaskStatusEvent();
        List<Status> statusList = new ArrayList<Status>();
        ConcurrentHashMap<Type, ConcurrentHashMap<Long, TaskExecutor>> taskExecutorMapMap = taskExecutorContainer
                .getTaskExecutorMapMap();
        if (taskExecutorMapMap != null) {
            for (Entry<Type, ConcurrentHashMap<Long, TaskExecutor>> entry : taskExecutorMapMap.entrySet()) {
                Type type = entry.getKey();
                ConcurrentHashMap<Long, TaskExecutor> taskExecutorMap = entry.getValue();
                for (TaskExecutor executor : taskExecutorMap.values()) {
                    long taskId = executor.getTask().getId();
                    TaskStatus taskStatus = executor.getStatus();
                    Status status = new Status();
                    status.setTaskId(taskId);
                    status.setType(type);
                    status.setTaskStatus(taskStatus);
                    status.setBinlogInfo(executor.getTask().getBinlogInfo());
                    statusList.add(status);
                }
            }
        }
        event.setStatus(statusList);
        event.setSyncServerName(config.getSyncServerName());
        return event;
    }
}
