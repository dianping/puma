package com.dianping.puma.syncserver.job.checker;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.dianping.puma.core.sync.model.config.PumaServerConfig;
import com.dianping.puma.core.sync.model.task.SyncTask;
import com.dianping.puma.core.sync.model.task.TaskState.State;
import com.dianping.puma.syncserver.conf.Config;
import com.dianping.puma.syncserver.job.executor.SyncTaskExecutor;
import com.dianping.puma.syncserver.job.executor.TaskExecutionException;
import com.dianping.puma.syncserver.job.executor.TaskExecutor;
import com.dianping.puma.syncserver.service.PumaServerConfigService;
import com.dianping.puma.syncserver.service.SyncTaskService;

public class DumpTaskCheckStrategy implements TaskCheckStrategy {

    @Autowired
    private SyncTaskService syncTaskService;
    @Autowired
    private PumaServerConfigService pumaServerConfigService;
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
        List<SyncTask> syncTasks = syncTaskService.find(states, syncServerName);

        List<TaskExecutor> taskExecutors = new ArrayList<TaskExecutor>();
        for (SyncTask task : syncTasks) {
            String srcMysqlName = task.getSrcMysqlName();
            PumaServerConfig pumaServerConfig = pumaServerConfigService.find(srcMysqlName);
            String pumaServerHostAndPort = pumaServerConfig.getHosts().get(0);
            String[] splits = pumaServerHostAndPort.split(":");
            String pumaServerHost = splits[0];
            int pumaServerPort = Integer.parseInt(splits[1]);
            String target = pumaServerConfig.getTarget();
            TaskExecutor excutor = new SyncTaskExecutor(task, pumaServerHost, pumaServerPort, target);
            taskExecutors.add(excutor);
        }

        return taskExecutors;
    }

    @Override
    public String getName() {
        return DumpTaskCheckStrategy.class.getName();
    }

}
