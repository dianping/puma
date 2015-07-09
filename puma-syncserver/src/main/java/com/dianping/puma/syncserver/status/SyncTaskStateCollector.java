package com.dianping.puma.syncserver.status;

import com.dianping.puma.biz.entity.TaskState;
import com.dianping.puma.biz.service.SyncTaskService;
import com.dianping.puma.biz.service.impl.SyncTaskStateServiceImpl;
import com.dianping.puma.syncserver.job.container.TaskExecutorContainer;
import com.dianping.puma.syncserver.job.executor.SyncTaskExecutor;
import com.dianping.puma.syncserver.job.executor.TaskExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service("syncTaskStateCollector")
public class SyncTaskStateCollector {

    @Autowired
    SyncTaskStateServiceImpl syncTaskStateService;

    @Autowired
    SyncTaskService syncTaskService;

    @Autowired
    TaskExecutorContainer taskExecutorContainer;

    @Scheduled(fixedDelay = 10 * 1000)
    public void collect() {
        for (TaskExecutor taskExecutor : taskExecutorContainer.getAll()) {
            if (taskExecutor instanceof SyncTaskExecutor) {
                TaskState syncTaskState = ((SyncTaskExecutor) taskExecutor).getTaskState();
                syncTaskStateService.createOrUpdate(syncTaskState);
            }
        }
    }
}
