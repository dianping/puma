package com.dianping.puma.syncserver.job.container;

import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dianping.puma.core.monitor.NotifyService;
import com.dianping.puma.core.sync.model.notify.SyncTaskStatusActionEvent;
import com.dianping.puma.core.sync.model.task.SyncTaskStatusAction;
import com.dianping.puma.core.sync.model.task.Task;
import com.dianping.puma.core.sync.model.task.Type;
import com.dianping.puma.core.sync.model.taskexecutor.TaskStatus;
import com.dianping.puma.syncserver.job.executor.CatchupTaskExecutor;
import com.dianping.puma.syncserver.job.executor.DumpTaskExecutor;
import com.dianping.puma.syncserver.job.executor.SyncTaskExecutor;
import com.dianping.puma.syncserver.job.executor.TaskExecutionException;
import com.dianping.puma.syncserver.job.executor.TaskExecutor;

/**
 * @author wukezhu
 */
@Service
@SuppressWarnings("rawtypes")
public class DefaultTaskExecutorContainer implements TaskExecutionContainer {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultTaskExecutorContainer.class);
    private volatile boolean stopped = true;

    private ConcurrentHashMap<Type, ConcurrentHashMap<Long, TaskExecutor>> taskExecutorMapMap = new ConcurrentHashMap<Type, ConcurrentHashMap<Long, TaskExecutor>>();
    @Autowired
    private NotifyService notifyService;

    @Override
    public void submit(final TaskExecutor newTaskExecutor) throws TaskExecutionException {
        LOG.info("TaskExecutor submit: " + newTaskExecutor.getTask());
        if (!stopped) {
            Task task = newTaskExecutor.getTask();
            Type type = task.getType();
            ConcurrentHashMap<Long, TaskExecutor> taskExecutorMap = taskExecutorMapMap.get(type);
            if (taskExecutorMap == null) {
                synchronized (type) {
                    taskExecutorMap = taskExecutorMapMap.get(type);
                    if (taskExecutorMap == null) {
                        taskExecutorMap = new ConcurrentHashMap<Long, TaskExecutor>();
                        taskExecutorMapMap.put(type, taskExecutorMap);
                    }
                }
            }
            //获取已有的TaskExecutor
            //执行taskExecutor
            TaskExecutor taskExecutor = taskExecutorMap.get(newTaskExecutor.getTask().getId());
            if (taskExecutor != null) {
                //如果有的话，一定是SyncTaskExecutor，是修改后重启
                refreshSyncTask(taskExecutorMap, taskExecutor, newTaskExecutor);
            } else {
                //新的TaskExecutor(Sync,Dump,Catchup)，无论如何先put到container。
                //接着如果是Sync且StatusAction是START/RESTART，则启动即可；如果是Dump/Catchup，则直接启动
                startTask(taskExecutorMap, newTaskExecutor);
            }
        }
    }

    /**
     * 此情形，是对SyncTask修改后的重启。 故需验证，只允许以下情形出现：<br>
     * 新SyncTaskExecutor的StatusAction是RESTART，旧的SyncTaskExecutor状态是SUSPPENDED/FAILED/SUCCEED
     */
    private void refreshSyncTask(ConcurrentHashMap<Long, TaskExecutor> taskExecutorMap, TaskExecutor taskExecutor,
                                 TaskExecutor newTaskExecutor) {
        //验证
        if (!(taskExecutor instanceof SyncTaskExecutor && taskExecutor instanceof SyncTaskExecutor)) {
            notifyService.alarm("ignored a TaskExecutor which id is exists: " + newTaskExecutor.getTask(), null, false);
            return;
        }
        SyncTaskExecutor syncTaskExecutor0 = (SyncTaskExecutor) taskExecutor;
        SyncTaskExecutor syncTaskExecutor = (SyncTaskExecutor) newTaskExecutor;
        if (syncTaskExecutor0.getTask().getSyncTaskStatusAction() != SyncTaskStatusAction.RESTART
                && (syncTaskExecutor.getStatus() != TaskStatus.SUSPPENDED) || syncTaskExecutor.getStatus() != TaskStatus.FAILED
                || syncTaskExecutor.getStatus() != TaskStatus.SUCCEED) {
            notifyService.alarm("ignored a TaskExecutor which status is not correct: " + newTaskExecutor.getTask(), null, false);
            return;
        }
        //使用新的newTaskExecutor替换现有的taskExecutor
        taskExecutorMap.put(newTaskExecutor.getTask().getId(), newTaskExecutor);
        taskExecutor.pause();
        newTaskExecutor.start();
    }

    /**
     * 新的TaskExecutor(Sync,Dump,Catchup)，无论如何先put到container。 <br>
     * 接着如果是Sync且StatusAction是START/RESTART，则启动即可；如果是Dump/Catchup，则直接启动
     */
    private void startTask(ConcurrentHashMap<Long, TaskExecutor> taskExecutorMap, TaskExecutor newTaskExecutor) {
        taskExecutorMap.put(newTaskExecutor.getTask().getId(), newTaskExecutor);
        if (newTaskExecutor instanceof SyncTaskExecutor) {
            SyncTaskExecutor syncTaskExecutor = (SyncTaskExecutor) newTaskExecutor;
            if (syncTaskExecutor.getTask().getSyncTaskStatusAction() == SyncTaskStatusAction.RESTART
                    || syncTaskExecutor.getTask().getSyncTaskStatusAction() == SyncTaskStatusAction.START) {
                syncTaskExecutor.start();
            }
        } else if (newTaskExecutor instanceof DumpTaskExecutor || newTaskExecutor instanceof CatchupTaskExecutor) {
            newTaskExecutor.start();
        } else {
            notifyService
                    .alarm("ignored a new TaskExecutor which status is not correct: " + newTaskExecutor.getTask(), null, false);
        }
    }

    @Override
    public TaskExecutor get(Type type, long taskId) {
        ConcurrentHashMap<Long, TaskExecutor> taskExcutors = taskExecutorMapMap.get(type);
        return taskExcutors.get(taskId);
    }

    /**
     * 验证，只允许以下情况出现：<br>
     * taskStatusActionEvent的StatusAction是PAUSE，旧的SyncTaskExecutor状态是PREPARING/RUNNING
     * taskStatusActionEvent的StatusAction是RESTART，旧的SyncTaskExecutor状态是SUSPPENDED/FAILED/SUCCEED
     */
    @Override
    public void changeStatus(SyncTaskStatusActionEvent taskStatusActionEvent) {
        SyncTaskExecutor syncTaskExecutor = (SyncTaskExecutor) this.get(Type.SYNC, taskStatusActionEvent.getSyncTaskId());
        if (syncTaskExecutor != null) {
            SyncTaskStatusAction statusAction = taskStatusActionEvent.getTaskStatusAction();
            if (statusAction == SyncTaskStatusAction.PAUSE
                    && (syncTaskExecutor.getStatus() == TaskStatus.PREPARING || syncTaskExecutor.getStatus() == TaskStatus.RUNNING)) {
                syncTaskExecutor.pause();
            } else if (statusAction == SyncTaskStatusAction.RESTART
                    && (syncTaskExecutor.getStatus() == TaskStatus.SUSPPENDED || syncTaskExecutor.getStatus() == TaskStatus.SUCCEED || syncTaskExecutor
                            .getStatus() == TaskStatus.FAILED)) {
                syncTaskExecutor.start();
            } else {
                notifyService.alarm("ignored a incorret SyncTaskStatusActionEvent: " + taskStatusActionEvent
                        + " , the SyncTaskExecutor's status is " + syncTaskExecutor.getStatus() + " .", null, false);
            }
        }
    }

    public ConcurrentHashMap<Type, ConcurrentHashMap<Long, TaskExecutor>> getTaskExecutorMapMap() {
        return taskExecutorMapMap;
    }

}
