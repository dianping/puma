package com.dianping.puma.syncserver.job.container;

import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.dianping.puma.core.sync.model.notify.TaskStatusActionEvent;
import com.dianping.puma.core.sync.model.task.Task;
import com.dianping.puma.core.sync.model.task.TaskState.State;
import com.dianping.puma.core.sync.model.task.Type;
import com.dianping.puma.syncserver.job.executor.TaskExecutionException;
import com.dianping.puma.syncserver.job.executor.TaskExecutor;

/**
 * @author Leo Liang
 */
@Service
public class DefaultTaskExecutorContainer implements TaskExecutionContainer {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultTaskExecutorContainer.class);
    private volatile boolean stopped = true;
    private ConcurrentHashMap<Type, ConcurrentHashMap<Long, TaskExecutor>> taskExecutorMap = new ConcurrentHashMap<Type, ConcurrentHashMap<Long, TaskExecutor>>();

    @Override
    public void submit(final TaskExecutor taskExecutor) throws TaskExecutionException {
        LOG.info("TaskExecutor submit: " + taskExecutor.getTask());
        if (!stopped) {
            Task task = taskExecutor.getTask();
            Type type = task.getType();
            ConcurrentHashMap<Long, TaskExecutor> taskExecutorMap0 = taskExecutorMap.get(type);
            if (taskExecutorMap0 == null) {
                synchronized (type) {
                    taskExecutorMap0 = taskExecutorMap.get(type);
                    if (taskExecutorMap0 == null) {
                        taskExecutorMap0 = new ConcurrentHashMap<Long, TaskExecutor>();
                        taskExecutorMap.put(type, taskExecutorMap0);
                    }
                }
            }
            //获取现有的TaskExecutor
            //执行taskExecutor
            TaskExecutor taskExecutor0 = taskExecutorMap0.get(taskExecutor.getTask().getId());
            if (taskExecutor0 != null) {
                //如果有的话，一定是SyncTaskExecutor，说明是修改后重启或暂停/恢复
                action(taskExecutorMap0, taskExecutor0, taskExecutor);
            } else {
                //新的TaskExecutor，启动即可
                action(taskExecutorMap0, taskExecutor);
            }
        }
    }

    /**
     * 重启：如果现有的SyncTaskExecutor状态是fail或succeed，则新的SyncTaskExecutor状态是RUNNABLE
     * 暂停：如果现有的SyncTaskExecutor状态是running，则新的SyncTaskExecutor状态是PAUSE（只更新状态）
     * 恢复：如果现有的SyncTaskExecutor状态是SUSPPENDED，则新的SyncTaskExecutor状态是RESOLVED/RUNNABLE（只更新状态）
     */
    private void action(ConcurrentHashMap<Long, TaskExecutor> taskExecutorMap0, TaskExecutor taskExecutor0,
                        TaskExecutor taskExecutor) {
        if (taskExecutor0.getTask().getType() != Type.SYNC || taskExecutor.getTask().getType() != Type.SYNC) {
            LOG.error("receive a TaskExecutor which id is exists: " + taskExecutor.getTask());
            return;
        }
        //重启
        if ((taskExecutor0.getTask().getTaskState().getState() == State.FAILED || taskExecutor0.getTask().getTaskState().getState() == State.FAILED)
                && taskExecutor.getTask().getTaskState().getState() == State.RUNNABLE) {
            taskExecutorMap0.put(taskExecutor.getTask().getId(), taskExecutor);
            taskExecutor.start();
        } else
        //暂停
        if ((taskExecutor0.getTask().getTaskState().getState() == State.RUNNING)
                && taskExecutor.getTask().getTaskState().getState() == State.PAUSE) {
            taskExecutor0.pause();
        } else
        //恢复
        if ((taskExecutor0.getTask().getTaskState().getState() == State.SUSPPENDED)
                && (taskExecutor.getTask().getTaskState().getState() == State.RESOLVED || taskExecutor.getTask().getTaskState()
                        .getState() == State.RUNNABLE)) {
            taskExecutor0.start();
        }
    }

    private void action(ConcurrentHashMap<Long, TaskExecutor> taskExecutorMap0, TaskExecutor taskExecutor) {
        taskExecutorMap0.put(taskExecutor.getTask().getId(), taskExecutor);
        if (taskExecutor.getTask().getTaskState().getState() != State.PAUSE) {
            taskExecutor.start();
        }
    }

    @Override
    public TaskExecutor get(Type type, long taskId) {
        ConcurrentHashMap<Long, TaskExecutor> taskExcutors = taskExecutorMap.get(type);
        return taskExcutors.get(taskId);
    }

    @Override
    public void changeStatus(TaskStatusActionEvent taskStatusActionEvent) {
        // TODO Auto-generated method stub

    }

    //TODO 对taskExecutorMap进行状态的监控，每隔n秒记录和处理状态(binlog,state)：更新数据库+通知报警

}
