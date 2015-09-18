package com.dianping.puma.checkserver.manager.dispatch;

import com.dianping.puma.biz.entity.CheckTaskEntity;
import com.dianping.puma.checkserver.manager.lock.TaskLock;
import com.dianping.puma.checkserver.manager.lock.TaskLockBuilder;
import com.dianping.puma.checkserver.manager.report.TaskReporter;
import com.dianping.puma.checkserver.manager.run.TaskRunFutureListener;
import com.dianping.puma.checkserver.manager.run.TaskRunner;
import com.dianping.puma.checkserver.model.TaskResult;
import jodd.util.collection.SortedArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Random;

@Service
public class MultipleTaskDispatcher implements TaskDispatcher {

    private final Logger logger = LoggerFactory.getLogger(MultipleTaskDispatcher.class);

    private final Random random = new Random();

    @Autowired
    TaskRunner taskRunner;

    @Autowired
    TaskLockBuilder taskLockBuilder;

    @Autowired
    TaskReporter taskReporter;

    private final long checkTimePeriod = 4 * 60 * 60 * 1000;

    @Override
    public void dispatch(List<CheckTaskEntity> checkTasks) {
        for (final CheckTaskEntity checkTask : getRandomTask(checkTasks)) {
            if (taskRunner.isFull()) {
                break;
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

                logger.info("Start run check task...");
                logger.info("Check period: {}", checkTask.getCursor());

                taskRunner.run(checkTask, new TaskRunFutureListener() {
                    @Override
                    public void onSuccess(TaskResult result) {
                        try {
                            logger.info("Success to run check task.");
                            taskReporter.report(checkTask, result);
                        } finally {
                            localTaskLock.unlock();
                            remoteTaskLock.unlock();
                        }
                    }

                    @Override
                    public void onFailure(Throwable cause) {
                        try {
                            logger.info("Failure to run check task.");
                            taskReporter.report(checkTask, cause);
                        } finally {
                            localTaskLock.unlock();
                            remoteTaskLock.unlock();
                        }
                    }
                });

            } catch (Throwable t) {
                logger.error("Failed to execute check task.", t);
                localTaskLock.unlock();
                remoteTaskLock.unlock();
            }
        }
    }

    protected SortedArrayList<CheckTaskEntity> getRandomTask(List<CheckTaskEntity> checkTasks) {
        SortedArrayList<CheckTaskEntity> randomList = new SortedArrayList<CheckTaskEntity>(new Comparator<CheckTaskEntity>() {
            @Override
            public int compare(CheckTaskEntity o1, CheckTaskEntity o2) {
                return random.nextInt(3) - 1; // -1,0,1
            }
        });
        randomList.addAll(checkTasks);
        return randomList;
    }
}
