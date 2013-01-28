package com.dianping.puma.admin.monitor;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.dianping.puma.core.monitor.NotifyService;
import com.dianping.puma.core.monitor.TaskStatusEvent.Status;
import com.dianping.puma.core.sync.model.taskexecutor.TaskStatus;

/**
 * 监控状态，报警<br>
 * 一旦发现有fail的任务则报警
 * 
 * @author wukezhu
 */
@Service
public class StatusMonitor {
    @Autowired
    private SystemStatusContainer systemStatusContainer;
    @Autowired
    private NotifyService notifyService;
    /** 当前数据库的所有SyncTask，除非是 */

    /**
     * 启动一个定时任务
     */
    @Scheduled(cron = "0/10 * * * * ?")
    public void report() {
        //TODO 如果超过30秒没有收到某台syncServer的状态报告时，就报警
        //需要存放当前的所有SyncTask,DumpTask,CatchupTask,结合SyncServer的所有Status
        //如果发现有不在运行的Task和失败的Task，报警
        
        //如果某台syncServer有fail的task，就报警
        ConcurrentHashMap<Long, Status> syncTaskStatusMap = systemStatusContainer.getSyncTaskStatusMap();
        if (syncTaskStatusMap.size() > 0) {
            for (Status status : syncTaskStatusMap.values()) {
                if (status.getTaskStatus() == TaskStatus.FAILED) {
                    notifyService.alarm("Task failed: " + status, null, true);
                }
            }
        }
        ConcurrentHashMap<Long, Status> catchupTaskStatusMap = systemStatusContainer.getCatchupTaskStatusMap();
        if (catchupTaskStatusMap.size() > 0) {
            for (Status status : catchupTaskStatusMap.values()) {
                if (status.getTaskStatus() == TaskStatus.FAILED) {
                    notifyService.alarm("Task failed: " + status, null, true);
                }
            }
        }
        ConcurrentHashMap<Long, Status> dumpTaskStatusMap = systemStatusContainer.getDumpTaskStatusMap();
        if (dumpTaskStatusMap.size() > 0) {
            for (Status status : dumpTaskStatusMap.values()) {
                if (status.getTaskStatus() == TaskStatus.FAILED) {
                    notifyService.alarm("Task failed: " + status, null, true);
                }
            }
        }
    }
}
