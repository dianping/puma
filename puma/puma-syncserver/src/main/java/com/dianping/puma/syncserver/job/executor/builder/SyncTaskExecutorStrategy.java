package com.dianping.puma.syncserver.job.executor.builder;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dianping.puma.core.sync.model.config.PumaServerConfig;
import com.dianping.puma.core.sync.model.task.SyncTask;
import com.dianping.puma.core.sync.model.task.Type;
import com.dianping.puma.syncserver.job.executor.SyncTaskExecutor;
import com.dianping.puma.syncserver.service.PumaServerConfigService;

@Service("syncTaskExecutorStrategy")
public class SyncTaskExecutorStrategy implements TaskExecutorStrategy<SyncTask, SyncTaskExecutor> {

    @Autowired
    private PumaServerConfigService pumaServerConfigService;

    @Override
    public SyncTaskExecutor build(SyncTask task) {
        //根据Task创建TaskExecutor
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

        SyncTaskExecutor excutor = new SyncTaskExecutor(task, pumaServerHost, pumaServerPort, target);
        return excutor;
    }

    @Override
    public Type getType() {
        return Type.SYNC;
    }
}
