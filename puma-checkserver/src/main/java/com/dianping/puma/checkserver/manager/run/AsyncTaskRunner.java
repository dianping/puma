package com.dianping.puma.checkserver.manager.run;

import com.dianping.puma.biz.entity.CheckTaskEntity;
import com.dianping.puma.checkserver.TaskExecutor;
import com.dianping.puma.checkserver.model.TaskEntity;
import com.dianping.puma.checkserver.model.TaskResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.*;

@Service
public class AsyncTaskRunner implements TaskRunner {

    private static int MAX_POOL_SIZE = 20;
    private static int MIN_POOL_SIZE = 5;
    private static long TIMEOUT_MIN = 5;

    private final ThreadPoolExecutor threadPool = new ThreadPoolExecutor(
            MIN_POOL_SIZE, MAX_POOL_SIZE, TIMEOUT_MIN, TimeUnit.MINUTES, new LinkedBlockingQueue<Runnable>(),
            new ThreadFactory() {
                @Override
                public Thread newThread(Runnable runnable) {
                    Thread thread = new Thread(runnable);
                    thread.setName("AsyncTaskRunner");
                    thread.setDaemon(true);
                    return thread;
                }
            });


    public boolean isFull() {
        return threadPool.getActiveCount() >= MAX_POOL_SIZE;
    }

    @Override
    public TaskRunFuture run(CheckTaskEntity checkTask, TaskRunFutureListener listener) {
        Callable<TaskResult> taskExecutor = build(checkTask);

        TaskRunFuture taskRunFuture = new TaskRunFuture(taskExecutor, listener);
        threadPool.execute(taskRunFuture);
        return taskRunFuture;
    }

    protected Callable<TaskResult> build(CheckTaskEntity checkTask) {
        TaskEntity taskEntity = transform(checkTask);
        TaskExecutor.Builder builder = TaskExecutor.Builder.create(taskEntity);
        return builder.build();
    }

    protected TaskEntity transform(CheckTaskEntity checkTask) {
        TaskEntity taskEntity = new TaskEntity();
        taskEntity.setBeginTime(checkTask.getCurrTime());
        taskEntity.setEndTime(checkTask.getNextTime());
        taskEntity.setComparison(checkTask.getComparison());
        taskEntity.setComparisonProp(checkTask.getComparisonProp());
        taskEntity.setMapper(checkTask.getMapper());
        taskEntity.setMapperProp(checkTask.getMapperProp());
        taskEntity.setSourceFetcher(checkTask.getSourceFetcher());
        taskEntity.setSourceFetcherProp(checkTask.getSourceFetcherProp());
        taskEntity.setSourceDsBuilder(checkTask.getSourceDsBuilder());
        taskEntity.setSourceDsBuilderProp(checkTask.getSourceDsBuilderProp());
        taskEntity.setTargetDsBuilder(checkTask.getTargetDsBuilder());
        taskEntity.setTargetDsBuilderProp(checkTask.getTargetDsBuilderProp());
        taskEntity.setTargetFetcher(checkTask.getTargetFetcher());
        taskEntity.setTargetFetcherProp(checkTask.getTargetFetcherProp());
        return taskEntity;
    }
}
