package com.dianping.puma.syncserver.job.checker;

import java.util.List;

import javax.annotation.PostConstruct;

import com.dianping.puma.core.constant.Operation;
import com.dianping.puma.core.constant.SyncType;
import com.dianping.puma.core.entity.BaseSyncTask;
import com.dianping.puma.core.entity.SyncTask;
import com.dianping.puma.core.monitor.*;
import com.dianping.puma.core.service.BaseSyncTaskService;
import com.dianping.puma.core.service.SyncTaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dianping.puma.syncserver.conf.Config;
import com.dianping.puma.syncserver.job.container.TaskExecutionContainer;
import com.dianping.puma.syncserver.job.executor.TaskExecutionException;
import com.dianping.puma.syncserver.job.executor.TaskExecutor;
import com.dianping.puma.syncserver.job.executor.builder.TaskExecutorBuilder;

@Service("taskChecker")
public class TaskChecker implements EventListener {
    private static final Logger LOG = LoggerFactory.getLogger(TaskChecker.class);

    @Autowired
    private SyncTaskService syncTaskService;
    @Autowired
    private TaskExecutionContainer taskExecutionContainer;
    @Autowired
    private TaskExecutorBuilder taskExecutorBuilder;
    @Autowired
    private Config config;
    @Autowired
    private NotifyService notifyService;

    @Autowired
    BaseSyncTaskService baseSyncTaskService;

    @SuppressWarnings("rawtypes")
    @PostConstruct
    public void init() {
        //加载所有Task
        String syncServerName = config.getSyncServerName();

        List<SyncTask> syncTasks = syncTaskService.findBySyncServerName(syncServerName);
        //构造成SyncTaskExecutor
        if (syncTasks != null && syncTasks.size() > 0) {
            for (SyncTask syncTask : syncTasks) {
                TaskExecutor executor = taskExecutorBuilder.build(syncTask);
                //将Task交给Container
                try {
                    taskExecutionContainer.submit(executor);
                } catch (TaskExecutionException e) {
                    notifyService.alarm(e.getMessage(), e, false);
                }
            }
        }
        LOG.info("TaskChecker loaded " + (syncTasks != null ? syncTasks.size() : 0) + " tasks.");
        LOG.info("TaskChecker inited.");
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void onEvent(Event event) {
        LOG.info("Receive event: " + event);

        if (event instanceof SyncTaskOperationEvent) {

            SyncTaskOperationEvent syncTaskOperationEvent = (SyncTaskOperationEvent) event;
            String taskName = syncTaskOperationEvent.getTaskName();
            Operation operation = syncTaskOperationEvent.getOperation();
            SyncType syncType = syncTaskOperationEvent.getSyncType();

            switch (operation) {
            case CREATE:
                BaseSyncTask task = baseSyncTaskService.find(syncType, taskName);
                TaskExecutor executor = taskExecutorBuilder.build(task);

                try {
                    taskExecutionContainer.submit(executor);
                } catch (TaskExecutionException e) {
                    notifyService.alarm(e.getMessage(), e, false);
                }
                break;

            case REMOVE:
                taskExecutionContainer.deleteSyncTask(taskName);
            }

        } else if (event instanceof SyncTaskControllerEvent) {

            SyncTaskControllerEvent syncTaskControllerEvent = (SyncTaskControllerEvent) event;
            taskExecutionContainer.changeStatus(syncTaskControllerEvent.getTaskName(), syncTaskControllerEvent.getController());

        } else {
            LOG.error("Receive error event.");
        }


        /*
        if (event instanceof SyncTaskStatusActionEvent) {
            //收到状态变化的事件，通知Container修改状态
            taskExecutionContainer.changeStatus(((SyncTaskStatusActionEvent) event).getSyncTaskId(),
                  ((SyncTaskStatusActionEvent) event).getTaskStatusAction());
        } else if (event instanceof SyncTaskDeleteEvent) {
            //收到状态变化的事件，通知Container修改状态
            taskExecutionContainer.deleteSyncTask(((SyncTaskDeleteEvent) event).getSyncTaskId());
        } else if (event instanceof TaskEvent) {
            //收到task的事件（创建或修改）
            //查询出该Task
            Task task = taskService.find(((TaskEvent) event).getType(), ((TaskEvent) event).getTaskId());
            TaskExecutor executor = taskExecutorBuilder.build(task);
            //将Task交给Container
            try {
                taskExecutionContainer.submit(executor);
            } catch (TaskExecutionException e) {
                notifyService.alarm(e.getMessage(), e, false);
            }
        }*/
    }
}
