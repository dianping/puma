package com.dianping.puma.checkserver.manager.run;

import com.dianping.puma.biz.entity.CheckTaskEntity;
import com.dianping.puma.checkserver.TaskExecutor;
import com.dianping.puma.checkserver.model.TaskEntity;
import com.dianping.puma.checkserver.model.TaskResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

@Service
public class AsyncTaskRunner implements TaskRunner {

    private final int threadPoolSize = 5;

    private final ExecutorService threadPool = Executors.newFixedThreadPool(threadPoolSize, new ThreadFactory() {
        @Override
        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable);
            thread.setName("async-task-runner");
            thread.setDaemon(true);
            return thread;
        }
    });

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
