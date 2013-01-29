package com.dianping.puma.admin.monitor;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.time.DateUtils;
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
    /** 存放正在报警的task的status，同一个Task在5分钟内只报警一次 */
    private HashMap<Status, Long> alarmingStatusMap = new HashMap<Status, Long>();

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

    private void check(ConcurrentHashMap<Long, Status> taskStatusMap) {
        if (taskStatusMap.size() > 0) {
            for (Status status : taskStatusMap.values()) {
                if (status.getTaskStatus() == TaskStatus.FAILED && shouldAlarm(status)) {
                    notifyService.alarm("Task failed: " + status, null, true);
                } else if (status.getTaskStatus() == null && shouldAlarm(status)) {
                    notifyService.alarm("Task's status is not Update recently: " + status, null, true);
                }
            }
        }
    }

    /**
     * 同一个Task在5分钟内只报警一次
     */
    private boolean shouldAlarm(Status status) {
        boolean shouldAlarm = true;
        Long statusStartAlarmAddTime = alarmingStatusMap.get(status);
        if (statusStartAlarmAddTime != null) {
            long lastTime = System.currentTimeMillis() - statusStartAlarmAddTime;
            if (lastTime < 5 * DateUtils.MILLIS_PER_MINUTE) {
                shouldAlarm = false;
            }
        }
        return shouldAlarm;
    }
}
