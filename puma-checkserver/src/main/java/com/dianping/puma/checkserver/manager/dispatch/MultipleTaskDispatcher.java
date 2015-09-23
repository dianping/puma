package com.dianping.puma.checkserver.manager.dispatch;

import com.dianping.cat.Cat;
import com.dianping.puma.biz.entity.CheckTaskEntity;
import com.dianping.puma.biz.service.CheckTaskService;
import com.dianping.puma.checkserver.manager.report.TaskReporter;
import com.dianping.puma.checkserver.manager.run.TaskRunFutureListener;
import com.dianping.puma.checkserver.manager.run.TaskRunner;
import com.dianping.puma.checkserver.model.TaskResult;
import com.dianping.puma.core.util.IPUtils;
import jodd.util.collection.SortedArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MultipleTaskDispatcher implements TaskDispatcher {

    private final Logger logger = LoggerFactory.getLogger(MultipleTaskDispatcher.class);

    private final String ip = IPUtils.getFirstNoLoopbackIP4Address();

    private final Random random = new Random();

    private final Set<Integer> tasks = Collections.newSetFromMap(new ConcurrentHashMap<Integer, Boolean>());

    @Autowired
    TaskRunner taskRunner;

    @Autowired
    TaskReporter taskReporter;

    @Autowired
    CheckTaskService checkTaskService;

    @Override
    public void dispatch(List<CheckTaskEntity> checkTasks) {
        for (final CheckTaskEntity checkTask : getRandomTask(checkTasks)) {
            if (taskRunner.isFull()) {
                break;
            }

            try {
                checkTask.setRunning(true);
                checkTask.setOwnerHost(ip);
                if (tasks.contains(checkTask.getId()) || !checkTaskService.tryLock(checkTask)) {
                    continue;
                }

                tasks.add(checkTask.getId());

                logger.info("Start run check task...");
                logger.info("Check period: {}", checkTask.getCursor());

                taskRunner.run(checkTask, new TaskRunFutureListener() {
                    @Override
                    public void onSuccess(TaskResult result) {
                        tasks.remove(checkTask.getId());
                        logger.info("Success to run check task.");
                        taskReporter.report(checkTask, result);
                    }

                    @Override
                    public void onFailure(Throwable cause) {
                        tasks.remove(checkTask.getId());
                        String msg = "Failure to run check task.";
                        logger.error(msg, cause);
                        Cat.logError(msg, cause);
                        taskReporter.report(checkTask, cause);
                    }
                });

            } catch (Exception e) {
                tasks.remove(checkTask.getId());
                String msg = "Failed to execute check task.";
                logger.error(msg, e);
                Cat.logError(msg, e);
                taskReporter.report(checkTask, e);
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
