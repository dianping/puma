//package com.dianping.puma.syncserver.monitor;
//
//import java.util.HashMap;
//import java.util.List;
//
//import org.apache.commons.lang.time.DateUtils;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Service;
//
//import com.dianping.puma.core.monitor.NotifyService;
//import com.dianping.puma.core.monitor.TaskStatusEvent;
//import com.dianping.puma.core.monitor.TaskStatusEvent.Status;
//import com.dianping.puma.core.sync.model.taskexecutor.TaskStatus;
//
///**
// * 监控状态，报警<br>
// * 一旦发现有fail的任务则报警
// * 
// * @author wukezhu
// */
//@Service
//public class StatusMonitor {
//    @Autowired
//    private SystemStatusContainer systemStatusContainer;
//    @Autowired
//    private NotifyService notifyService;
//    /** 存放正在报警的task的status，同一个Task在5分钟内只报警一次 */
//    private HashMap<Integer, Long> alarmingStatusMap = new HashMap<Integer, Long>();
//
//    /**
//     * 启动一个定时任务
//     */
//    @Scheduled(cron = "0/10 * * * * ?")
//    public void report() {
//        TaskStatusEvent event = systemStatusContainer.getTaskStatusEvent();
//        List<Status> statusList = event.getStatusList();
//        if (statusList != null) {
//            for (Status status : statusList) {
//                if (status.getTaskStatus() == TaskStatus.FAILED && shouldAlarm(status)) {
//                    notifyService.alarm(status + " status is failed!", null, true);
//                    alarmingStatusMap.put(status.hashCode(), System.currentTimeMillis());
//                }
//            }
//        }
//    }
//
//    /**
//     * 同一个Task在5分钟内只报警一次
//     */
//    private boolean shouldAlarm(Status status) {
//        boolean shouldAlarm = true;
//        Long statusStartAlarmAddTime = alarmingStatusMap.get(status.hashCode());
//        if (statusStartAlarmAddTime != null) {
//            long lastTime = System.currentTimeMillis() - statusStartAlarmAddTime;
//            if (lastTime < 5 * DateUtils.MILLIS_PER_MINUTE) {
//                shouldAlarm = false;
//            }
//        }
//        return shouldAlarm;
//    }
//}
