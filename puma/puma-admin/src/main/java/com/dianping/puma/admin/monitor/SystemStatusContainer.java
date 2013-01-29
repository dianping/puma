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

import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.dianping.puma.admin.service.SyncTaskService;
import com.dianping.puma.core.monitor.Event;
import com.dianping.puma.core.monitor.EventListener;
import com.dianping.puma.core.monitor.NotifyService;
import com.dianping.puma.core.monitor.TaskStatusEvent;
import com.dianping.puma.core.monitor.TaskStatusEvent.Status;
import com.dianping.puma.core.sync.model.task.SyncTask;
import com.dianping.puma.core.sync.model.task.Type;
import com.dianping.puma.core.sync.model.taskexecutor.TaskStatus;

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

    @PostConstruct
    public void init() {
        //加载数据库的所有SyncTask
        List<SyncTask> syncTasks = syncTaskService.findAll();
        //将SyncTask放到syncTaskStatusMap中
        if (syncTasks != null) {
            for (SyncTask syncTask : syncTasks) {
                Status s = new Status();
                s.setTaskStatus(TaskStatus.WAITING);
                s.setTaskId(syncTask.getId());
                s.setType(Type.SYNC);
                syncTaskStatusMap.put(s.getTaskId(), s);
            }
        }
    }

    /**
     * 启动一个定时任务
     */
    @Scheduled(cron = "0/10 * * * * ?")
    public void monitor() {
        //如果有Status超过60秒没有更新状态，则状态无用，要删除其状态
        deleteTaskStatusLaterThan60s(syncTaskStatusMap);
        deleteTaskStatusLaterThan60s(catchupTaskStatusMap);
        deleteTaskStatusLaterThan60s(dumpTaskStatusMap);
    }

    private void deleteTaskStatusLaterThan60s(ConcurrentHashMap<Long, Status> taskStatusMap) {
        if (taskStatusMap.size() > 0) {
            for (Status status : taskStatusMap.values()) {
                Date lastUpdateTime = status.getGmtCreate();
                Date curTime = new Date();
                long time = curTime.getTime() - lastUpdateTime.getTime();
                if (time > 60 * 1000) {//超过一分钟的状态，都无用
                    status.setTaskStatus(null);
                }
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
                if (syncTaskStatusMap.get(taskId) != null) {
                    notifyService.alarm("Status is already exist! Why add again?! Receive Status is [taskId=" + taskId + ",type="
                            + type + "]", null, true);
                } else {
                    Status s = new Status();
                    s.setTaskId(taskId);
                    s.setType(Type.SYNC);
                    s.setTaskStatus(TaskStatus.WAITING);
                    syncTaskStatusMap.put(s.getTaskId(), s);
                }
                break;
            case CATCHUP:
                if (catchupTaskStatusMap.get(taskId) != null) {
                    notifyService.alarm("Status is already exist! Why add again?! Receive Status is [taskId=" + taskId + ",type="
                            + type + "]", null, true);
                } else {
                    Status s = new Status();
                    s.setTaskId(taskId);
                    s.setType(Type.CATCHUP);
                    s.setTaskStatus(TaskStatus.WAITING);
                    catchupTaskStatusMap.put(s.getTaskId(), s);
                }
                break;
            case DUMP:
                if (dumpTaskStatusMap.get(taskId) != null) {
                    notifyService.alarm("Status is already exist! Why add again?! Receive Status is [taskId=" + taskId + ",type="
                            + type + "]", null, true);
                } else {
                    Status s = new Status();
                    s.setTaskId(taskId);
                    s.setType(Type.DUMP);
                    s.setTaskStatus(TaskStatus.WAITING);
                    dumpTaskStatusMap.put(s.getTaskId(), s);
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
                if (syncTaskStatusMap.get(status.getTaskId()) != null) {
                    syncTaskStatusMap.put(status.getTaskId(), status);
                }
                break;
            case CATCHUP:
                if (catchupTaskStatusMap.get(status.getTaskId()) != null) {
                    catchupTaskStatusMap.put(status.getTaskId(), status);
                }
                break;
            case DUMP:
                if (dumpTaskStatusMap.get(status.getTaskId()) != null) {
                    dumpTaskStatusMap.put(status.getTaskId(), status);
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
