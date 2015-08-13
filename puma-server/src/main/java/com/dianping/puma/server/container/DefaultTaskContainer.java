package com.dianping.puma.server.container;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dianping.puma.biz.entity.PumaTaskEntity;
import com.dianping.puma.core.model.Table;
import com.dianping.puma.core.model.TableSet;
import com.dianping.puma.core.util.IPUtils;
import com.dianping.puma.server.builder.TaskBuilder;
import com.dianping.puma.server.extension.registry.RegistryService;
import com.dianping.puma.storage.EventStorage;
import com.dianping.puma.taskexecutor.TaskExecutor;

import java.util.Map;


@Service
public class DefaultTaskContainer implements TaskContainer {

    private ConcurrentHashMap<String, TaskExecutor> taskExecutors = new ConcurrentHashMap<String, TaskExecutor>();

    private ExecutorService executorService = Executors.newCachedThreadPool();

    public static DefaultTaskContainer instance;

    @Autowired
    private TaskBuilder taskBuilder;

    @Autowired
    RegistryService registryService;

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
            e.printStackTrace();
        }
    }

    @Override
    public void update(String taskName, PumaTaskEntity oriTask, PumaTaskEntity task) {

    }

    @Override
    public void delete(String taskName, PumaTaskEntity task) {
        if (taskExecutors.remove(taskName) == null) {
            throw new RuntimeException("delete puma task failure, not exists.");
        }

        stop(taskName);
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

                register(taskExecutor);
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void stop(String taskName) {
        try {
            TaskExecutor taskExecutor = taskExecutors.get(taskName);
            if (taskExecutor != null) {
                taskExecutor.stop();

                unregister(taskExecutor);
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    protected void register(TaskExecutor taskExecutor) {
        PumaTaskEntity pumaTask = taskExecutor.getTask();
        Map<String, List<String>> databaseTableMap = pumaTask.getTableSet().mapSchemaTables();
        for (String database: databaseTableMap.keySet()) {
            registryService.register(IPUtils.getFirstNoLoopbackIP4Address() + ":4040", database, null);
        }
    }

    protected void unregister(TaskExecutor taskExecutor) {
        PumaTaskEntity pumaTask = taskExecutor.getTask();
        Map<String, List<String>> databaseTableMap = pumaTask.getTableSet().mapSchemaTables();
        for (String database: databaseTableMap.keySet()) {
            registryService.unregister(IPUtils.getFirstNoLoopbackIP4Address() + ":4040", database, null);
        }
    }
}