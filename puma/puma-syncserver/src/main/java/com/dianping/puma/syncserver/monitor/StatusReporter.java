package com.dianping.puma.syncserver.monitor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.dianping.puma.core.monitor.SwallowEventPulisher;
import com.dianping.puma.core.monitor.TaskStatusEvent;

/**
 * 监控状态，报警<br>
 * 推送状态消息
 * 
 * @author wukezhu
 */
@Service
public class StatusReporter {
    @Autowired
    private SwallowEventPulisher statusEventPublisher;
    @Autowired
    private SystemStatusContainer systemStatusContainer;

    /**
     * 启动一个定时任务汇报状态
     */
    @Scheduled(cron = "0/5 * * * * ?")
    public void report() {
        TaskStatusEvent event = systemStatusContainer.getTaskStatusEvent();
        if (event.getStatusList() != null && event.getStatusList().size() > 0) {
            statusEventPublisher.publish(event);
        }
    }

}
