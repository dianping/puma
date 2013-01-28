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
import org.springframework.stereotype.Service;

import com.dianping.puma.core.monitor.TaskStatusEvent;
import com.dianping.puma.core.monitor.TaskStatusEvent.Status;
import com.dianping.puma.core.sync.model.task.Type;
import com.dianping.puma.core.sync.model.taskexecutor.TaskStatus;
import com.dianping.puma.syncserver.conf.Config;
import com.dianping.puma.syncserver.job.container.DefaultTaskExecutorContainer;
import com.dianping.puma.syncserver.job.executor.TaskExecutor;

/**
 * @author wukezhu
 */
@Service
public class SystemStatusContainer {

    @Autowired
    private DefaultTaskExecutorContainer taskExecutorContainer;
    @Autowired
    private Config config;

    @SuppressWarnings("rawtypes")
    public TaskStatusEvent getTaskStatusEvent() {
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
        event.setStatusList(statusList);
        event.setSyncServerName(config.getSyncServerName());
        return event;
    }
}
