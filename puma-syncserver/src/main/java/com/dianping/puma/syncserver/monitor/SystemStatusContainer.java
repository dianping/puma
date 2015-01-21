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

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dianping.puma.core.monitor.TaskStatusEvent;
import com.dianping.puma.core.sync.model.BinlogInfo;
import com.dianping.puma.core.sync.model.task.Type;
import com.dianping.puma.core.sync.model.taskexecutor.TaskExecutorStatus;
import com.dianping.puma.syncserver.conf.Config;
import com.dianping.puma.syncserver.job.container.DefaultTaskExecutorContainer;
import com.dianping.puma.syncserver.job.executor.TaskExecutor;
import com.dianping.puma.syncserver.service.TaskService;

/**
 * @author wukezhu
 */
@Service
public class SystemStatusContainer implements InitializingBean {

    @Autowired
    private DefaultTaskExecutorContainer taskExecutorContainer;
    @Autowired
    private Config config;
    @Autowired
    private TaskService taskService;

    public static SystemStatusContainer instance;

    @SuppressWarnings("rawtypes")
    public TaskStatusEvent getTaskStatusEvent() {
        TaskStatusEvent event = new TaskStatusEvent();
        List<TaskExecutorStatus> statusList = new ArrayList<TaskExecutorStatus>();
        ConcurrentHashMap<Integer, TaskExecutor> taskExecutorMap = taskExecutorContainer.getTaskExecutorMap();
        for (TaskExecutor executor : taskExecutorMap.values()) {
            TaskExecutorStatus taskStatus = executor.getTaskExecutorStatus();
            statusList.add(taskStatus);
        }
        event.setStatusList(statusList);
        event.setSyncServerName(config.getSyncServerName());
        return event;
    }

    /**
     * 记录binlog位置
     */
    public void recordBinlog(Type type, long taskId, BinlogInfo binlogInfo) {
        taskService.recordBinlog(config.getSyncServerName(), type, taskId, binlogInfo);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        instance = this;
    }
}
