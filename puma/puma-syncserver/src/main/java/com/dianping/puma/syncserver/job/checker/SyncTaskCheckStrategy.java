package com.dianping.puma.syncserver.job.checker;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dianping.puma.core.sync.model.config.PumaServerConfig;
import com.dianping.puma.core.sync.model.task.SyncTask;
import com.dianping.puma.core.sync.model.task.TaskState.State;
import com.dianping.puma.syncserver.conf.Config;
import com.dianping.puma.syncserver.job.executor.SyncTaskExecutor;
import com.dianping.puma.syncserver.job.executor.TaskExecutionException;
import com.dianping.puma.syncserver.job.executor.TaskExecutor;
import com.dianping.puma.syncserver.service.PumaServerConfigService;
import com.dianping.puma.syncserver.service.SyncTaskService;

@Service("syncTaskCheckStrategy")
public class SyncTaskCheckStrategy implements TaskCheckStrategy {

    @Autowired
    private SyncTaskService syncTaskService;
    @Autowired
    private PumaServerConfigService pumaServerConfigService;
    @Autowired
    private Config config;

    @Override
    public List<TaskExecutor> check() throws TaskExecutionException {
        //访问数据库，获取对应的Task
        String syncServerName = config.getSyncServerName();
        List<State> states = new ArrayList<State>();
        states.add(State.PAUSE);
        states.add(State.RESOLVED);
        states.add(State.RUNNABLE);
        List<SyncTask> syncTasks = syncTaskService.find(states, syncServerName);
        //更新状态
        for (SyncTask task : syncTasks) {
            if (task.getTaskState().getState() != State.PAUSE) {
                syncTaskService.updateState(task.getId(), State.PREPARING, null);
            }
        }
        //根据Task创建TaskExecutor
        List<TaskExecutor> taskExecutors = new ArrayList<TaskExecutor>();
        for (SyncTask task : syncTasks) {
            String srcMysqlName = task.getSrcMysqlName();
            PumaServerConfig pumaServerConfig = pumaServerConfigService.find(srcMysqlName);
            String pumaServerHostAndPort = pumaServerConfig.getHosts().get(0);
            String pumaServerHost = pumaServerHostAndPort;
            int pumaServerPort = 80;
            if (StringUtils.contains(pumaServerHostAndPort, ':')) {
                String[] splits = pumaServerHostAndPort.split(":");
                pumaServerHost = splits[0];
                pumaServerPort = Integer.parseInt(splits[1]);
            }
            String target = pumaServerConfig.getTarget();

            TaskExecutor excutor = new SyncTaskExecutor(task, pumaServerHost, pumaServerPort, target);
            taskExecutors.add(excutor);
        }

        return taskExecutors;
    }

    @Override
    public String getName() {
        return SyncTaskCheckStrategy.class.getName();
    }

}
