package com.dianping.puma.server.container;

import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.status.SystemStatus;
import com.dianping.puma.status.SystemStatusManager;
import com.dianping.puma.storage.manage.InstanceStorageManager;
import com.dianping.puma.taskexecutor.TaskExecutor;
import com.dianping.puma.taskexecutor.task.InstanceTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ScheduledTaskMerger implements TaskMerger {

    private final Logger logger = LoggerFactory.getLogger(ScheduledTaskMerger.class);

    @Autowired
    TaskContainer taskContainer;

    @Autowired
    InstanceStorageManager instanceStorageManager;

    @Override
    public void merge() {
        Map<String, TaskExecutor> mainTaskExecutors = taskContainer.getMainExecutors();
        Map<String, List<TaskExecutor>> tempTaskExecutors = taskContainer.getTempExecutors();

        Set<String> mainInstances = mainTaskExecutors.keySet();
        Set<String> tempInstances = tempTaskExecutors.keySet();
        Set<String> instances = new HashSet<String>();
        instances.addAll(mainInstances);
        instances.addAll(tempInstances);

        for (String instance : instances) {
            if (!isMerging(instance)) {
                if (isLeaderMissing(instance)) {
                    selectNewLeader(instance);
                    continue;
                }

                resetMerging(instance);
                tryMerging(instance);
            } else {
                tryMerge(instance);
            }
        }
    }

    @Scheduled(fixedDelay = 5000)
    protected void scheduledMerge() {
        merge();
    }

    protected boolean isMerging(String instance) {
        TaskExecutor mainTaskExecutor = taskContainer.getMainExecutors().get(instance);
        List<TaskExecutor> tempTaskExecutors = taskContainer.getTempExecutors().get(instance);

        if (mainTaskExecutor == null) {
            return false;
        }

        if (!mainTaskExecutor.isMerging()) {
            return false;
        }

        if (tempTaskExecutors == null) {
            return false;
        }

        for (TaskExecutor tempTaskExecutor : tempTaskExecutors) {
            if (tempTaskExecutor.isMerging()) {
                return true;
            }
        }

        return false;
    }

    protected boolean isLeaderMissing(String instance) {
        TaskExecutor mainTaskExecutor = taskContainer.getMainExecutors().get(instance);
        List<TaskExecutor> tempTaskExecutors = taskContainer.getTempExecutors().get(instance);

        return mainTaskExecutor == null && tempTaskExecutors != null && tempTaskExecutors.size() > 0;
    }

    protected void selectNewLeader(String instance) {
        List<TaskExecutor> tempTaskExecutors = taskContainer.getTempExecutors().get(instance);

        if (tempTaskExecutors != null && tempTaskExecutors.size() > 0) {
            TaskExecutor tempTaskExecutor = tempTaskExecutors.get(0);
            taskContainer.upgrade(tempTaskExecutor);
        }
    }

    protected void resetMerging(String instance) {
        TaskExecutor mainTaskExecutor = taskContainer.getMainExecutors().get(instance);
        List<TaskExecutor> tempTaskExecutors = taskContainer.getTempExecutors().get(instance);

        if (mainTaskExecutor != null) {
            mainTaskExecutor.cancelStopUntil();
        }

        if (tempTaskExecutors != null) {
            for (TaskExecutor tempTaskExecutor : tempTaskExecutors) {
                tempTaskExecutor.cancelStopUntil();
            }
        }
    }

    protected void tryMerging(String instance) {
        TaskExecutor mainTaskExecutor = taskContainer.getMainExecutors().get(instance);
        List<TaskExecutor> tempTaskExecutors = taskContainer.getTempExecutors().get(instance);

        TaskExecutor nearTaskExecutor = findFirstNear(mainTaskExecutor, tempTaskExecutors);
        if (nearTaskExecutor != null) {
            logger.info("start merging...");
            logger.info("main task executor time: {}.", new Date(timestamp(mainTaskExecutor) * 1000));
            logger.info("near task executor time: {}.", new Date(timestamp(nearTaskExecutor) * 1000));

            long commonTimestamp = timestamp(mainTaskExecutor) + 3 * 60;
            mainTaskExecutor.stopUntil(commonTimestamp);
            nearTaskExecutor.stopUntil(commonTimestamp);
        }
    }

    protected void tryMerge(String instance) {
        TaskExecutor mainTaskExecutor = taskContainer.getMainExecutors().get(instance);
        List<TaskExecutor> tempTaskExecutors = taskContainer.getTempExecutors().get(instance);

        if (mainTaskExecutor.isMerging()) {
            for (TaskExecutor tempTaskExecutor : tempTaskExecutors) {
                if (tempTaskExecutor.isMerging()) {
                    logger.info("main task executor time: {}.", new Date(timestamp(mainTaskExecutor) * 1000));
                    logger.info("near task executor time: {}.", new Date(timestamp(tempTaskExecutor) * 1000));

                    if (mainTaskExecutor.isStop() && tempTaskExecutor.isStop()) {
                        logger.info("start actual merging...");
                        taskContainer.merge(mainTaskExecutor, tempTaskExecutor);
                    }
                }
            }
        }
    }

    protected TaskExecutor findFirstNear(TaskExecutor mainTaskExecutor, List<TaskExecutor> tempTaskExecutors) {
        long mainTimestamp = timestamp(mainTaskExecutor);

        if (tempTaskExecutors != null) {
            for (TaskExecutor tempTaskExecutor : tempTaskExecutors) {
                long tempTimestamp = timestamp(tempTaskExecutor);
                logger.info("distance: {}.", Math.abs(mainTimestamp - tempTimestamp));
                logger.info("main: {}.", new Date(mainTimestamp * 1000));
                logger.info("temp: {}.", new Date(tempTimestamp * 1000));

                if (mainTimestamp != 0 &&
                        tempTimestamp != 0 &&
                        Math.abs(mainTimestamp - tempTimestamp) < 60) {
                    return tempTaskExecutor;
                }
            }
        }

        return null;
    }

    protected long timestamp(TaskExecutor taskExecutor) {
        InstanceTask instanceTask = taskExecutor.getInstanceTask();
        String taskName = instanceTask.getTaskName();
        SystemStatus.Server server = SystemStatusManager.getServer(taskName);
        if (server == null) {
            return 0;
        }
        BinlogInfo binlogInfo = server.getBinlogInfo();
        return binlogInfo == null ? 0 : binlogInfo.getTimestamp();
    }
}
