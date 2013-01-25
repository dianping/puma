package com.dianping.puma.syncserver.job.executor.builder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dianping.puma.core.sync.model.config.PumaServerConfig;
import com.dianping.puma.core.sync.model.task.CatchupTask;
import com.dianping.puma.core.sync.model.task.Type;
import com.dianping.puma.syncserver.job.container.TaskExecutionContainer;
import com.dianping.puma.syncserver.job.executor.CatchupTaskExecutor;
import com.dianping.puma.syncserver.job.executor.SyncTaskExecutor;
import com.dianping.puma.syncserver.service.PumaServerConfigService;

@Service("catchupTaskExecutorBuilder")
public class CatchupTaskExecutorStrategy implements TaskExecutorStrategy<CatchupTask, CatchupTaskExecutor> {

    @Autowired
    private PumaServerConfigService pumaServerConfigService;
    @Autowired
    private TaskExecutionContainer taskExecutionContainer;

    @Override
    public CatchupTaskExecutor build(CatchupTask task) {
        //根据Task创建TaskExecutor
        String srcMysqlName = task.getSrcMysqlName();
        PumaServerConfig pumaServerConfig = pumaServerConfigService.find(srcMysqlName);
        String pumaServerHostAndPort = pumaServerConfig.getHosts().get(0);
        String[] splits = pumaServerHostAndPort.split(":");
        String pumaServerHost = splits[0];
        int pumaServerPort = Integer.parseInt(splits[1]);
        String target = pumaServerConfig.getTarget();
        //从taskContainer获取syncTaskExecutor
        SyncTaskExecutor syncTaskExecutor = (SyncTaskExecutor) taskExecutionContainer.get(Type.SYNC,
                task.getSyncTaskId());
        return new CatchupTaskExecutor(task, pumaServerHost, pumaServerPort, target, syncTaskExecutor);
    }

    @Override
    public Type getType() {
        return Type.CATCHUP;
    }

}
