package com.dianping.puma.admin.monitor;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.dianping.puma.admin.service.SyncTaskService;
import com.dianping.puma.core.monitor.NotifyService;
import com.dianping.puma.core.sync.model.task.SyncTask;
import com.dianping.puma.core.sync.model.task.SyncTaskStatusAction;
import com.dianping.puma.core.sync.model.task.Type;
import com.dianping.puma.core.sync.model.taskexecutor.TaskExecutorStatus;

/**
 * 监控状态，报警<br>
 * 一旦发现有fail的任务则报警
 * 
 * @author wukezhu
 */
@Service
public class StatusMonitor {
    @Autowired
    private SyncTaskService syncTaskService;
    @Autowired
    private SystemStatusContainer systemStatusContainer;
    @Autowired
    private NotifyService notifyService;
    /** 存放正在报警的task的status，同一个Task在5分钟内只报警一次 */
    private HashMap<Integer, Long> alarmingStatusMap = new HashMap<Integer, Long>();

    @Scheduled(cron = "0/10 * * * * ?")
    public void monitor() {
        //如果有Status超过60秒没有更新，就报警
        //如果有fail的task，就报警
        ConcurrentHashMap<Integer, TaskExecutorStatus> taskStatusMap = systemStatusContainer.getTaskStatusMap();
        check(taskStatusMap);
    }

    private void check(ConcurrentHashMap<Integer, TaskExecutorStatus> taskStatusMap) {
        if (taskStatusMap.size() > 0) {
            for (TaskExecutorStatus status : taskStatusMap.values()) {
                if (status.getStatus() == TaskExecutorStatus.Status.FAILED && shouldAlarm(status)) {
                    notifyService.alarm("Task failed: " + status, null, true);
                    alarmingStatusMap.put(status.hashCode(), System.currentTimeMillis());
                } else if (status.getStatus() == null && shouldAlarm(status)) {
                    notifyService.alarm("Task's status is not Update recently: " + status, null, true);
                    alarmingStatusMap.put(status.hashCode(), System.currentTimeMillis());
                }
            }
        }
    }

    /**
     * 同一个SyncTask在5分钟内只报警一次，暂停状态的不报警<br>
     * CatchupTask和DumpTask永远仅报警一次
     */
    private boolean shouldAlarm(TaskExecutorStatus status) {
        boolean shouldAlarm = true;
        Long statusStartAlarmAddTime = alarmingStatusMap.get(status.hashCode());
        if (statusStartAlarmAddTime != null) {
            switch (status.getType()) {
                case CATCHUP:
                case DUMP:
                    shouldAlarm = false;
                    break;
                case SYNC:
                    long lastTime = System.currentTimeMillis() - statusStartAlarmAddTime;
                    if (lastTime < 5 * DateUtils.MILLIS_PER_MINUTE) {
                        shouldAlarm = false;
                    }
                    break;
            }
        }
        //暂停状态的SyncTask不报警
        if (shouldAlarm && status.getType() == Type.SYNC) {
            SyncTask syncTask = syncTaskService.find(status.getTaskId());
            if (syncTask.getSyncTaskStatusAction() == SyncTaskStatusAction.PAUSE) {
                shouldAlarm = false;
            }
        }
        return shouldAlarm;
    }

}
