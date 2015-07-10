//package com.dianping.puma.syncserver.status;
//
//import com.dianping.puma.biz.entity.TaskStateEntity;
//import com.dianping.puma.biz.service.DumpTaskService;
//import com.dianping.puma.biz.service.impl.DumpTaskStateServiceImpl;
//import com.dianping.puma.syncserver.job.container.TaskExecutorContainer;
//import com.dianping.puma.syncserver.job.executor.DumpTaskExecutor;
//import com.dianping.puma.syncserver.job.executor.TaskExecutor;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Service;
//
//@Service("dumpTaskStateCollector")
//public class DumpTaskStateCollector {
//
//    @Autowired
//    DumpTaskStateServiceImpl dumpTaskStateService;
//
//    @Autowired
//    DumpTaskService dumpTaskService;
//
//    @Autowired
//    TaskExecutorContainer taskExecutorContainer;
//
//    @Scheduled(fixedDelay = 10 * 1000)
//    public void collect() {
//        for (TaskExecutor taskExecutor : taskExecutorContainer.getAll()) {
//            if (taskExecutor instanceof DumpTaskExecutor) {
//                TaskStateEntity dumpTaskState = ((DumpTaskExecutor) taskExecutor).getTaskState();
//                dumpTaskStateService.createOrUpdate(dumpTaskState);
//            }
//        }
//    }
//}