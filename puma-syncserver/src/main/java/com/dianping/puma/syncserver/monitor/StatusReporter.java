package com.dianping.puma.syncserver.monitor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.dianping.puma.core.monitor.SwallowEventPublisher;
import com.dianping.puma.core.monitor.TaskStatusEvent;
import com.dianping.swallow.common.producer.exceptions.SendFailedException;

/**
 * 监控状态，报警<br>
 * 推送状态消息
 * 
 * @author wukezhu
 */
@Service
public class StatusReporter {
    @Autowired
    private SwallowEventPublisher statusEventPublisher;
    @Autowired
    private SystemStatusContainer systemStatusContainer;

    /**
     * 启动一个定时任务汇报状态
     * 
     * @throws SendFailedException
     */
    /*
    @Scheduled(cron = "0/5 * * * * ?")
    public void report() throws SendFailedException {
        TaskStatusEvent event = systemStatusContainer.getTaskStatusEvent();
        if (event.getStatusList() != null && event.getStatusList().size() > 0) {
            statusEventPublisher.publish(event);
        }
    }*/

}
