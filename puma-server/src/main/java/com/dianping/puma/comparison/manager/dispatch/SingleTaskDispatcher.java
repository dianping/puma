package com.dianping.puma.comparison.manager.dispatch;

import com.dianping.puma.biz.entity.CheckTaskEntity;
import com.dianping.puma.comparison.manager.lock.TaskLock;
import com.dianping.puma.comparison.manager.lock.TaskLockBuilder;
import com.dianping.puma.comparison.manager.report.TaskReporter;
import com.dianping.puma.comparison.manager.run.TaskRunFutureListener;
import com.dianping.puma.comparison.manager.run.TaskRunner;
import com.dianping.puma.comparison.model.TaskResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Service
public class SingleTaskDispatcher implements TaskDispatcher {

    private final Logger logger = LoggerFactory.getLogger(SingleTaskDispatcher.class);

    @Autowired
    TaskRunner taskRunner;

    @Autowired
    TaskLockBuilder taskLockBuilder;

    @Autowired
    TaskReporter taskReporter;

    private final long checkTimePeriod = 120 * 60 * 1000; // 120 min.

    @Override
    public void dispatch(List<CheckTaskEntity> checkTasks) {
        for (final CheckTaskEntity checkTask : checkTasks) {

            if (isFailure(checkTask)) {
                continue;
            }

            if (!setNextTimeIfEnough(checkTask)) {
                continue;
            }

            final TaskLock localTaskLock = taskLockBuilder.buildLocalLock(checkTask);
            final TaskLock remoteTaskLock = taskLockBuilder.buildRemoteLock(checkTask);

            try {
                if (!localTaskLock.tryLock()) {
                    continue;
                }

                if (!remoteTaskLock.tryLock()) {
                    localTaskLock.unlock();
                    continue;
                }

                logger.info("start run check task...");

                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                logger.info("check period: {} - {}.",
                        dateFormat.format(checkTask.getCurrTime()),
                        dateFormat.format(checkTask.getNextTime()));

                taskRunner.run(checkTask, new TaskRunFutureListener() {
                    @Override
                    public void onSuccess(TaskResult result) {
                        logger.info("success to run check task.");

                        taskReporter.report(checkTask, result);
                        localTaskLock.unlock();
                        remoteTaskLock.unlock();
                    }

                    @Override
                    public void onFailure(Throwable cause) {
                        logger.info("failure to run check task.");

                        taskReporter.report(checkTask, cause);
                        localTaskLock.unlock();
                        remoteTaskLock.unlock();
                    }
                });

            } catch (Throwable t) {
                logger.error("failed to execute check task.", t);

                localTaskLock.unlock();
                remoteTaskLock.unlock();
            }

            break;
        }
    }

    protected boolean isFailure(CheckTaskEntity checkTask) {
        return !checkTask.isSuccess();
    }

    protected boolean setNextTimeIfEnough(CheckTaskEntity checkTask) {
        Date currTime = checkTask.getCurrTime();
        Date nextTime = new Date(currTime.getTime() + checkTimePeriod);
        if (nextTime.getTime() > new Date().getTime()) {
            return false;
        }
        checkTask.setNextTime(nextTime);
        return true;
    }
}
