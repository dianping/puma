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
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.dianping.puma.core.monitor.TaskStatusEvent;
import com.dianping.puma.core.monitor.TaskStatusEvent.Status;
import com.dianping.puma.core.sync.model.BinlogInfo;
import com.dianping.puma.core.sync.model.task.Type;
import com.dianping.puma.core.sync.model.taskexecutor.TaskStatus;
import com.dianping.puma.syncserver.conf.Config;
import com.dianping.puma.syncserver.job.container.DefaultTaskExecutorContainer;
import com.dianping.puma.syncserver.job.executor.TaskExecutor;
import com.dianping.puma.syncserver.service.TaskService;

/**
 * @author wukezhu
 */
@Service
public class SystemStatusContainer {

    @Autowired
    private DefaultTaskExecutorContainer taskExecutorContainer;
    @Autowired
    private Config config;
    @Autowired
    private TaskService taskService;

    @SuppressWarnings("rawtypes")
    public TaskStatusEvent getTaskStatusEvent() {
        TaskStatusEvent event = new TaskStatusEvent();
        List<Status> statusList = new ArrayList<Status>();
        ConcurrentHashMap<Integer, TaskExecutor> taskExecutorMap = taskExecutorContainer.getTaskExecutorMap();
        for (TaskExecutor executor : taskExecutorMap.values()) {
            long taskId = executor.getTask().getId();
            TaskStatus taskStatus = executor.getStatus();
            Status status = new Status();
            status.setTaskId(taskId);
            status.setType(executor.getTask().getType());
            status.setTaskStatus(taskStatus);
            status.setBinlogInfo(executor.getTask().getBinlogInfo());
            statusList.add(status);
        }
        event.setStatusList(statusList);
        event.setSyncServerName(config.getSyncServerName());
        return event;
    }

    /**
     * 记录SyncTask的binlog位置
     */
    @SuppressWarnings("rawtypes")
    @Scheduled(cron = "0/3 * * * * ?")
    public void recordBinlog() {
        ConcurrentHashMap<Integer, TaskExecutor> taskExecutorMap = taskExecutorContainer.getTaskExecutorMap();
        for (TaskExecutor taskExecutor : taskExecutorMap.values()) {
            if (taskExecutor.getTask().getType() == Type.SYNC) {
                long taskId = taskExecutor.getTask().getId();
                BinlogInfo binlogInfo = taskExecutor.getTask().getBinlogInfo();
                Type type = taskExecutor.getTask().getType();
                taskService.recordBinlog(type, taskId, binlogInfo);
            }
        }
    }
}
