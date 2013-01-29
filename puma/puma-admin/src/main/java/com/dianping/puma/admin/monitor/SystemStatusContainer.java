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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dianping.puma.admin.service.SyncTaskService;
import com.dianping.puma.core.monitor.Event;
import com.dianping.puma.core.monitor.EventListener;
import com.dianping.puma.core.monitor.NotifyService;
import com.dianping.puma.core.monitor.TaskStatusEvent;
import com.dianping.puma.core.monitor.TaskStatusEvent.Status;
import com.dianping.puma.core.sync.model.task.SyncTask;
import com.dianping.puma.core.sync.model.task.Type;

/**
 * 此类存放admin所知晓的所有Task(SyncTask,CatchupTask,DumpTask)，来源包括：<br>
 * (1)初始化时从数据库加载SyncTask (2)通过admin创建Task时，追加Task <br>
 * <br>
 * 另一方面，此类接收来自SyncServer的状态心跳，实时更新对应Task的状态。
 * 
 * @author wukezhu
 */
@Service("systemStatusContainer")
public class SystemStatusContainer implements EventListener {

    private ConcurrentHashMap<Long, Status> syncTaskStatusMap = new ConcurrentHashMap<Long, Status>();
    private ConcurrentHashMap<Long, Status> catchupTaskStatusMap = new ConcurrentHashMap<Long, Status>();
    private ConcurrentHashMap<Long, Status> dumpTaskStatusMap = new ConcurrentHashMap<Long, Status>();
    @Autowired
    private SyncTaskService syncTaskService;
    @Autowired
    private NotifyService notifyService;

    public void init() {
        //加载数据库的所有SyncTask
        List<SyncTask> syncTasks = syncTaskService.findAll();
        //将SyncTask放到syncTaskStatusMap中
        if (syncTasks != null) {
            for (SyncTask syncTask : syncTasks) {
                Status s = new Status();
                s.setTaskId(syncTask.getId());
                s.setType(Type.SYNC);
                syncTaskStatusMap.put(s.getTaskId(), s);
            }
        }
    }

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

    /**
     * admin创建新的SyncTask,DumpTask,CatchupTask后，调用此方法，添加Status
     */
    public void addStatus(Type type, long taskId) {
        switch (type) {
            case SYNC:
                if (syncTaskStatusMap.contains(taskId)) {
                    notifyService.alarm("Status is already exist! Why add again?! Receive Status is [taskId=" + taskId + ",type="
                            + type + "]", null, true);
                }
                break;
            case CATCHUP:
                if (catchupTaskStatusMap.contains(taskId)) {
                    notifyService.alarm("Status is already exist! Why add again?! Receive Status is [taskId=" + taskId + ",type="
                            + type + "]", null, true);
                }
                break;
            case DUMP:
                if (dumpTaskStatusMap.contains(taskId)) {
                    notifyService.alarm("Status is already exist! Why add again?! Receive Status is [taskId=" + taskId + ",type="
                            + type + "]", null, true);
                }
                break;
            default:
                throw new IllegalArgumentException("error type:" + type);
        }
    }

    /**
     * 更新状态(从SyncServer收到状态心跳后，更新状态)
     */
    public void updateStatus(Status status) {
        switch (status.getType()) {
            case SYNC:
                if (syncTaskStatusMap.contains(status.getTaskId())) {
                    syncTaskStatusMap.put(status.getTaskId(), status);
                } else {
                    notifyService.alarm("Receive A Status which is not exist in Admin! Receive Status is " + status, null, true);
                }
                break;
            case CATCHUP:
                if (catchupTaskStatusMap.contains(status.getTaskId())) {
                    catchupTaskStatusMap.put(status.getTaskId(), status);
                } else {
                    notifyService.alarm("Receive A Status which is not exist in Admin! Receive Status is " + status, null, true);
                }
                break;
            case DUMP:
                if (dumpTaskStatusMap.contains(status.getTaskId())) {
                    dumpTaskStatusMap.put(status.getTaskId(), status);
                } else {
                    notifyService.alarm("Receive A Status which is not exist in Admin! Receive Status is " + status, null, true);
                }
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
                    updateStatus(status);
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
