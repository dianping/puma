package com.dianping.puma.admin.monitor;

import java.util.Date;
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
    public void monitor() {
        //如果有Status超过60秒没有更新，就报警
        //如果有fail的task，就报警
        ConcurrentHashMap<Long, Status> syncTaskStatusMap = systemStatusContainer.getSyncTaskStatusMap();
        check(syncTaskStatusMap);
        ConcurrentHashMap<Long, Status> catchupTaskStatusMap = systemStatusContainer.getCatchupTaskStatusMap();
        check(catchupTaskStatusMap);
        ConcurrentHashMap<Long, Status> dumpTaskStatusMap = systemStatusContainer.getDumpTaskStatusMap();
        check(dumpTaskStatusMap);
    }

    private void check(ConcurrentHashMap<Long, Status> saskStatusMap) {
        if (saskStatusMap.size() > 0) {
            for (Status status : saskStatusMap.values()) {
                if (status.getTaskStatus() == TaskStatus.FAILED) {
                    notifyService.alarm("Task failed: " + status, null, true);
                } else {
                    Date lastUpdateTime = status.getGmtCreate();
                    Date curTime = new Date();
                    long time = curTime.getTime() - lastUpdateTime.getTime();
                    if (time > 60 * 1000) {//一分钟
                        notifyService
                                .alarm("Task's status have " + time / 1000 + " seconds not updated, last Status is: " + status,
                                        null, true);
                    }
                }
                //TODO 已经报警，不能再报警
            }
        }
    }
}
