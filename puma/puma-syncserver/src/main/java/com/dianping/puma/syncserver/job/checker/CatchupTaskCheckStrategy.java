package com.dianping.puma.syncserver.job.checker;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.dianping.puma.core.sync.model.config.PumaServerConfig;
import com.dianping.puma.core.sync.model.task.CatchupTask;
import com.dianping.puma.core.sync.model.task.TaskState.State;
import com.dianping.puma.syncserver.conf.Config;
import com.dianping.puma.syncserver.job.executor.CatchupTaskExecutor;
import com.dianping.puma.syncserver.job.executor.SyncTaskExecutor;
import com.dianping.puma.syncserver.job.executor.TaskExecutionException;
import com.dianping.puma.syncserver.job.executor.TaskExecutor;
import com.dianping.puma.syncserver.service.CatchupTaskService;
import com.dianping.puma.syncserver.service.PumaServerConfigService;

public class CatchupTaskCheckStrategy implements TaskCheckStrategy {

    @Autowired
    private CatchupTaskService catchupTaskService;
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
        List<CatchupTask> syncTasks = catchupTaskService.find(states, syncServerName);

        List<TaskExecutor> taskExecutors = new ArrayList<TaskExecutor>();
        for (CatchupTask task : syncTasks) {
            String srcMysqlName = task.getSrcMysqlName();
            PumaServerConfig pumaServerConfig = pumaServerConfigService.find(srcMysqlName);
            String pumaServerHostAndPort = pumaServerConfig.getHosts().get(0);
            String[] splits = pumaServerHostAndPort.split(":");
            String pumaServerHost = splits[0];
            int pumaServerPort = Integer.parseInt(splits[1]);
            String target = pumaServerConfig.getTarget();

            //TODO 从taskContainer获取syncTaskExecutor
            //            syncTaskExecutor syncTaskExecutor = 
            SyncTaskExecutor syncTaskExecutor = null;
            TaskExecutor excutor = new CatchupTaskExecutor(task, pumaServerHost, pumaServerPort, target, syncTaskExecutor);
            taskExecutors.add(excutor);
        }

        return taskExecutors;
    }

    @Override
    public String getName() {
        return CatchupTaskCheckStrategy.class.getName();
    }

}
