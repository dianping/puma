package com.dianping.puma.syncserver.monitor;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.dianping.puma.core.monitor.NotifyService;
import com.dianping.puma.core.monitor.TaskStatusEvent;
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

    /**
     * 启动一个定时任务
     */
    @Scheduled(cron = "0/10 * * * * ?")
    public void report() {
        TaskStatusEvent event = systemStatusContainer.getTaskStatusEvent();
        List<Status> statusList = event.getStatusList();
        if (statusList != null) {
            for (Status status : statusList) {
                if (status.getTaskStatus() == TaskStatus.FAILED) {
                    String msg = status.toString() + " status is failed!";
                    notifyService.alarm(msg, null, true);
                }
            }
        }
    }
}