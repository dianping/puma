package com.dianping.puma.syncserver.job.checker;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.dianping.puma.core.sync.model.task.DumpTask;
import com.dianping.puma.core.sync.model.task.TaskState.State;
import com.dianping.puma.syncserver.conf.Config;
import com.dianping.puma.syncserver.job.executor.DumpTaskExecutor;
import com.dianping.puma.syncserver.job.executor.TaskExecutionException;
import com.dianping.puma.syncserver.job.executor.TaskExecutor;
import com.dianping.puma.syncserver.service.DumpTaskService;

public class SyncTaskCheckStrategy implements TaskCheckStrategy {

    @Autowired
    private DumpTaskService dumpTaskService;
    @Autowired
    private Config config;

    @Override
    public List<TaskExecutor> check() throws TaskExecutionException {
        String syncServerName = config.getSyncServerName();
        List<State> states = new ArrayList<State>();
        states.add(State.PAUSE);
        states.add(State.PREPARABLE);
        states.add(State.RESOLVED);
        states.add(State.RUNNABLE);
        List<DumpTask> dumpTasks = dumpTaskService.find(states, syncServerName);

        List<TaskExecutor> taskExecutors = new ArrayList<TaskExecutor>();
        for (DumpTask task : dumpTasks) {
            TaskExecutor excutor = new DumpTaskExecutor(task);
            taskExecutors.add(excutor);
        }

        return taskExecutors;
    }

    @Override
    public String getName() {
        return SyncTaskCheckStrategy.class.getName();
    }

}
