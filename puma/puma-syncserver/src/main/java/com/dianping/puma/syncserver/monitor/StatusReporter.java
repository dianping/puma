package com.dianping.puma.syncserver.monitor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import com.dianping.puma.core.sync.model.notify.TaskStatusEvent;

/**
 * 监控状态，报警<br>
 * 推送状态消息
 * 
 * @author wukezhu
 */
public class StatusReporter {
    @Autowired
    private SwallowStatusEventPulisher swallowStatusEventPulisher;
    @Autowired
    private SystemStatusContainer systemStatusContainer;

    /**
     * 启动一个定时任务汇报状态
     */
    @Scheduled(cron = "0/5 * * * * ?")
    public void report() {
        TaskStatusEvent event = systemStatusContainer.getTaskStatusEvent();
        swallowStatusEventPulisher.publish(event);
    }
}
