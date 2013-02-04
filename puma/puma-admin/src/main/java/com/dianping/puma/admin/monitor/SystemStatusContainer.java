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
import com.dianping.puma.core.sync.model.task.SyncTask;
import com.dianping.puma.core.sync.model.task.Type;
import com.dianping.puma.core.sync.model.taskexecutor.TaskExecutorStatus;

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

    private ConcurrentHashMap<Integer, TaskExecutorStatus> taskStatusMap = new ConcurrentHashMap<Integer, TaskExecutorStatus>();
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
                TaskExecutorStatus status = new TaskExecutorStatus();
                status.setTaskId(syncTask.getId());
                status.setType(syncTask.getType());
                status.setStatus(TaskExecutorStatus.Status.WAITING);
                taskStatusMap.put(status.hashCode(), status);
            }
        }
    }

    /**
     * 启动一个定时任务
     */
    @Scheduled(cron = "0/10 * * * * ?")
    public void monitor() {
        //对于Sync，如果有Status超过60秒没有更新状态，则状态无用，要删除其状态
        deleteTaskStatusLaterThan60s(taskStatusMap);
    }

    private void deleteTaskStatusLaterThan60s(ConcurrentHashMap<Integer, TaskExecutorStatus> taskStatusMap) {
        if (taskStatusMap.size() > 0) {
            for (TaskExecutorStatus status : taskStatusMap.values()) {
                if (status.getType() == Type.SYNC) {
                    Date lastUpdateTime = status.getGmtCreate();
                    Date curTime = new Date();
                    long time = curTime.getTime() - lastUpdateTime.getTime();
                    if (time > 60 * 1000) {//超过一分钟的状态，都无用
                        status.setStatus(null);
                        status.setDetail(null);
                    }
                }
            }
        }
    }

    public TaskExecutorStatus getStatus(Type type, long taskId) {
        TaskExecutorStatus status = taskStatusMap.get(TaskExecutorStatus.calHashCode(type, taskId));
        return status;
    }

    /**
     * admin创建新的SyncTask,DumpTask,CatchupTask后，调用此方法，添加Status
     */
    public void addStatus(Type type, long taskId) {
        if (taskStatusMap.get(TaskExecutorStatus.calHashCode(type, taskId)) != null) {
            notifyService.alarm("Status is already exist! Why add again?! Receive Status is [taskId=" + taskId + ",type=" + type
                    + "]", null, true);
        } else {
            TaskExecutorStatus status = new TaskExecutorStatus();
            status.setTaskId(taskId);
            status.setType(type);
            status.setStatus(TaskExecutorStatus.Status.WAITING);
            taskStatusMap.put(status.hashCode(), status);
        }
    }

    /**
     * 更新状态(从SyncServer收到状态心跳后，更新状态)
     */
    public void updateStatus(TaskExecutorStatus status) {
        if (taskStatusMap.get(status.hashCode()) != null) {
            status.setGmtCreate(new Date());
            taskStatusMap.put(status.hashCode(), status);
        } else {
            //收到的status对应的在admin这边不存在，则忽略
        }
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof TaskStatusEvent) {
            TaskStatusEvent e = (TaskStatusEvent) event;
            List<TaskExecutorStatus> statusList = e.getStatusList();
            if (statusList != null) {
                for (TaskExecutorStatus status : statusList) {
                    updateStatus(status);
                }
            }
        }
    }

    public ConcurrentHashMap<Integer, TaskExecutorStatus> getTaskStatusMap() {
        return taskStatusMap;
    }

}
