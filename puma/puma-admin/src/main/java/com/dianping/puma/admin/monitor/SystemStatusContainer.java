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
package com.dianping.puma.admin.monitor;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.dianping.puma.core.monitor.Event;
import com.dianping.puma.core.monitor.EventListener;
import com.dianping.puma.core.monitor.TaskStatusEvent;
import com.dianping.puma.core.monitor.TaskStatusEvent.Status;
import com.dianping.puma.core.sync.model.task.Type;

/**
 * @author wukezhu
 */
@Service("systemStatusContainer")
public class SystemStatusContainer implements EventListener {

    private ConcurrentHashMap<Long, Status> syncTaskStatusMap = new ConcurrentHashMap<Long, Status>();
    private ConcurrentHashMap<Long, Status> catchupTaskStatusMap = new ConcurrentHashMap<Long, Status>();
    private ConcurrentHashMap<Long, Status> dumpTaskStatusMap = new ConcurrentHashMap<Long, Status>();

    public Status getStatus(Type type, long taskId) {
        Status status;
        switch (type) {
            case SYNC:
                status = syncTaskStatusMap.get(taskId);
                break;
            case CATCHUP:
                status = catchupTaskStatusMap.get(taskId);
                break;
            case DUMP:
                status = dumpTaskStatusMap.get(taskId);
                break;
            default:
                throw new IllegalArgumentException("error type:" + type);
        }
        return status;
    }

    public void addStatus(Status status) {
        switch (status.getType()) {
            case SYNC:
                syncTaskStatusMap.put(status.getTaskId(), status);
                break;
            case CATCHUP:
                catchupTaskStatusMap.put(status.getTaskId(), status);
                break;
            case DUMP:
                dumpTaskStatusMap.put(status.getTaskId(), status);
                break;
            default:
                throw new IllegalArgumentException("error status type:" + status);
        }
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof TaskStatusEvent) {
            TaskStatusEvent e = (TaskStatusEvent) event;
            List<Status> statusList = e.getStatusList();
            if (statusList != null) {
                for (Status status : statusList) {
                    addStatus(status);
                }
            }
        }
    }

    public ConcurrentHashMap<Long, Status> getSyncTaskStatusMap() {
        return syncTaskStatusMap;
    }

    public ConcurrentHashMap<Long, Status> getCatchupTaskStatusMap() {
        return catchupTaskStatusMap;
    }

    public ConcurrentHashMap<Long, Status> getDumpTaskStatusMap() {
        return dumpTaskStatusMap;
    }

}
