package com.dianping.puma.syncserver.job.checker;

import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dianping.puma.core.monitor.NotifyService;
import com.dianping.puma.core.sync.model.notify.Event;
import com.dianping.puma.core.sync.model.notify.EventListener;
import com.dianping.puma.core.sync.model.notify.TaskEvent;
import com.dianping.puma.core.sync.model.notify.TaskStatusActionEvent;
import com.dianping.puma.core.sync.model.task.SyncTask;
import com.dianping.puma.core.sync.model.task.Task;
import com.dianping.puma.syncserver.conf.Config;
import com.dianping.puma.syncserver.job.container.TaskExecutionContainer;
import com.dianping.puma.syncserver.job.executor.TaskExecutionException;
import com.dianping.puma.syncserver.job.executor.TaskExecutor;
import com.dianping.puma.syncserver.job.executor.builder.TaskExecutorBuilder;
import com.dianping.puma.syncserver.service.TaskService;

@Service
public class TaskChecker implements EventListener {
    @Autowired
    private TaskService taskService;
    @Autowired
    private TaskExecutionContainer taskExecutionContainer;
    @Autowired
    private TaskExecutorBuilder taskExecutorBuilder;
    @Autowired
    private Config config;
    @Autowired
    private NotifyService notifyService;

    @PostConstruct
    public void init() {
        //加载所有Task
        String syncServerName = config.getSyncServerName();
        List<SyncTask> syncTasks = taskService.findSyncTasks(syncServerName);
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
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof TaskStatusActionEvent) {
            //收到状态变化的事件，通知Container修改状态
            taskExecutionContainer.changeStatus(((TaskStatusActionEvent) event));
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
        }
    }
}
