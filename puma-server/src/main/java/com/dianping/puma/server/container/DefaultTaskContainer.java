package com.dianping.puma.server.container;

import com.dianping.cat.Cat;
import com.dianping.puma.biz.entity.PumaTaskEntity;
import com.dianping.puma.core.model.Table;
import com.dianping.puma.core.model.TableSet;
import com.dianping.puma.server.builder.TaskBuilder;
import com.dianping.puma.status.SystemStatusManager;
import com.dianping.puma.storage.EventStorage;
import com.dianping.puma.taskexecutor.TaskExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@Service
public class DefaultTaskContainer implements TaskContainer {

    private ConcurrentHashMap<String, TaskExecutor> taskExecutors = new ConcurrentHashMap<String, TaskExecutor>();

    private ExecutorService executorService = Executors.newCachedThreadPool();

    public static DefaultTaskContainer instance;

    @Autowired
    private TaskBuilder taskBuilder;

    @Override
    public TaskExecutor get(String taskName) {
        return taskExecutors.get(taskName);
    }

    @Override
    public List<TaskExecutor> getAll() {
        return new ArrayList<TaskExecutor>(taskExecutors.values());
    }

    @Override
    public EventStorage getTaskStorage(String database) {
        for (TaskExecutor taskExecutor : taskExecutors.values()) {
            TableSet tableSet = taskExecutor.getTask().getTableSet();
            List<Table> tables = tableSet.listSchemaTables();

            for (Table table : tables) {
                if (table.getSchemaName().equals(database)) {
                    return taskExecutor.getFileSender().get(0).getStorage(database);
                }
            }
        }

        return null;
    }

    @Override
    public void create(String taskName, PumaTaskEntity task) {
        try {
            TaskExecutor taskExecutor = taskBuilder.build(task);

            if (taskExecutors.putIfAbsent(taskName, taskExecutor) != null) {
                throw new RuntimeException("create puma task failure, duplicate exists.");
            }

            start(taskName);
        } catch (Exception e) {
            Cat.logError(e.getMessage(), e);
        }
    }

    @Override
    public void update(String taskName, PumaTaskEntity oriTask, PumaTaskEntity task) {
        delete(taskName, oriTask);
        create(taskName, task);
    }

    @Override
    public void delete(String taskName, PumaTaskEntity task) {
        stop(taskName);

        if (taskExecutors.remove(taskName) == null) {
            throw new RuntimeException("delete puma task failure, not exists.");
        }

        SystemStatusManager.deleteServer(taskName);
    }

    @Override
    public void start(String taskName) {
        try {
            final TaskExecutor taskExecutor = taskExecutors.get(taskName);
            if (taskExecutor != null && taskExecutor.isStop()) {
                executorService.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            taskExecutor.start();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        } catch (Exception e) {
            Cat.logError(e.getMessage(), e);
        }
    }

    @Override
    public void stop(String taskName) {
        try {
            TaskExecutor taskExecutor = taskExecutors.get(taskName);
            if (taskExecutor != null) {
                taskExecutor.stop();
            }
        } catch (Exception e) {
            Cat.logError(e.getMessage(), e);
        }
    }
}