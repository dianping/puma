package com.dianping.puma.server.container;

import com.dianping.puma.taskexecutor.TaskExecutor;
import com.dianping.puma.taskexecutor.task.DatabaseTask;
import com.dianping.puma.taskexecutor.task.InstanceTask;

import java.util.List;
import java.util.Map;

public interface TaskContainer {

    Map<String, DatabaseTask> getDatabaseTasks();

    Map<String, TaskExecutor> getMainExecutors();

    Map<String, List<TaskExecutor>> getTempExecutors();

    void create(InstanceTask instanceTask);

    void create(DatabaseTask databaseTask);

    void update(DatabaseTask databaseTask);

    void remove(String database);

    void merge(TaskExecutor mainTaskExecutor, TaskExecutor tempTaskExecutor);

    void upgrade(TaskExecutor taskExecutor);

    void start(TaskExecutor taskExecutor);

    void stop(TaskExecutor taskExecutor);

    TaskExecutor getExecutor(String database);
}
