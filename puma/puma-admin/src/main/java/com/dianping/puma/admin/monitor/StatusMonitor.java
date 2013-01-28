package com.dianping.puma.admin.monitor;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.dianping.puma.core.monitor.NotifyService;
import com.dianping.puma.core.monitor.SwallowEventPulisher;
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
    //    @Autowired
    //    private SwallowEventPulisher taskEventPublisher;
    @Autowired
    private SystemStatusContainer systemStatusContainer;
    @Autowired
    private NotifyService notifyService;

    /**
     * 启动一个定时任务
     */
    @Scheduled(cron = "0/10 * * * * ?")
    public void report() {
        //TODO 如果超过30秒没有收到某台syncServer的状态报告时，就报警

        //TODO 如果某台syncServer有fail的task，就报警
    }
}
