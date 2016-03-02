package com.dianping.puma.server.container;

import com.dianping.cat.Cat;
import com.dianping.puma.instance.InstanceManager;
import com.dianping.puma.model.Table;
import com.dianping.puma.server.builder.TaskBuilder;
import com.dianping.puma.server.registry.RegistryService;
import com.dianping.puma.server.server.TaskServerManager;
import com.dianping.puma.storage.manage.DatabaseStorageManager;
import com.dianping.puma.storage.manage.InstanceStorageManager;
import com.dianping.puma.taskexecutor.TaskExecutor;
import com.dianping.puma.taskexecutor.task.DatabaseTask;
import com.dianping.puma.taskexecutor.task.InstanceTask;
import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class DefaultTaskContainer implements TaskContainer {

    private final Logger logger = LoggerFactory.getLogger(DefaultTaskContainer.class);

    @Autowired
    InstanceManager instanceManager;

    @Autowired
    InstanceStorageManager instanceStorageManager;

    @Autowired
    DatabaseStorageManager databaseStorageManager;

    @Autowired
    RegistryService registryService;

    @Autowired
    TaskServerManager taskServerManager;

    @Autowired
    TaskBuilder taskBuilder;

    public static DefaultTaskContainer instance;

    private ExecutorService pool = Executors.newCachedThreadPool();

    private Map<String, TaskExecutor> taskExecutors = new ConcurrentHashMap<String, TaskExecutor>();

    @Override
    public TaskExecutor getExecutor(String database) {
        return taskExecutors.get(database);
    }

    @Override
    public Map<String, DatabaseTask> getDatabaseTasks() {
        Map<String, DatabaseTask> databaseTaskMap = new HashMap<String, DatabaseTask>();
        for (TaskExecutor taskExecutor : taskExecutors.values()) {
            InstanceTask instanceTask = taskExecutor.getInstanceTask();
            if (instanceTask != null) {
                List<DatabaseTask> databaseTasks = instanceTask.getDatabaseTasks();
                if (databaseTasks != null) {
                    for (DatabaseTask databaseTask : databaseTasks) {
                        String database = databaseTask.getDatabase();
                        databaseTaskMap.put(database, databaseTask);
                    }
                }
            }
        }
        return databaseTaskMap;
    }

    @Override
    public Map<String, TaskExecutor> getMainExecutors() {
        Map<String, TaskExecutor> mainExecutors = new HashMap<String, TaskExecutor>();
        for (TaskExecutor taskExecutor : taskExecutors.values()) {
            InstanceTask instanceTask = taskExecutor.getInstanceTask();
            if (instanceTask != null) {
                if (instanceTask.isMain()) {
                    mainExecutors.put(instanceTask.getInstance(), taskExecutor);
                }
            }
        }
        return mainExecutors;
    }

    @Override
    public Map<String, List<TaskExecutor>> getTempExecutors() {
        Map<String, List<TaskExecutor>> tempExecutors = new HashMap<String, List<TaskExecutor>>();
        for (TaskExecutor taskExecutor : taskExecutors.values()) {
            InstanceTask instanceTask = taskExecutor.getInstanceTask();
            if (instanceTask != null) {
                if (!instanceTask.isMain()) {
                    List<TaskExecutor> taskExecutorList = tempExecutors.get(instanceTask.getInstance());
                    if (taskExecutorList == null) {
                        taskExecutorList = new ArrayList<TaskExecutor>();
                        tempExecutors.put(instanceTask.getInstance(), taskExecutorList);
                    }
                    taskExecutorList.add(taskExecutor);
                }
            }
        }
        return tempExecutors;
    }

    @Override
    public void create(InstanceTask instanceTask) {
        logger.info("start creating instance task...");
        logger.info("instance task: {}.", instanceTask);

        if (instanceTask.isMain()) {
            createMain(instanceTask);
        } else {
            createTemp(instanceTask);
        }

        for (DatabaseTask databaseTask : instanceTask.getDatabaseTasks()) {
            registryService.register(getServerHostAndPort(), databaseTask.getDatabase());
        }

        logger.info("success to create instance task.");
    }

    @Override
    public void create(DatabaseTask databaseTask) {
        logger.info("start creating temp task...");
        logger.info("database task: {}.", databaseTask);

        String database = databaseTask.getDatabase();
        String instance = findInstance(database);

        InstanceTask instanceTask = new InstanceTask(count(instance) == 0, instance, databaseTask);
        TaskExecutor taskExecutor = taskBuilder.build(instanceTask);
        start(taskExecutor);
        add(taskExecutor);

        registryService.register(getServerHostAndPort(), database);

        logger.info("success to create task.");
    }

    protected void createMain(InstanceTask instanceTask) {
        logger.info("start creating main instance task...");

        TaskExecutor mainTaskExecutor = taskBuilder.build(instanceTask);

        start(mainTaskExecutor);

        add(mainTaskExecutor);

        logger.info("success to create main instance task.");
    }

    protected void createTemp(InstanceTask instanceTask) {
        logger.info("start creating temp instance task...");

        TaskExecutor tempTaskExecutor = taskBuilder.build(instanceTask);

        start(tempTaskExecutor);

        add(tempTaskExecutor);

        logger.info("success to create temp instance task.");
    }

    @Override
    public void update(DatabaseTask databaseTask) {
        String database = databaseTask.getDatabase();
        remove(database);
        create(databaseTask);
    }

    @Override
    public void remove(String database) {
        logger.info("start removing database task..., database = {}.", database);

        TaskExecutor taskExecutor = findTaskExecutor(database);
        stop(taskExecutor);

        // Remove database level.
        unregisterDatabase(database);
        clearDatabase(database);

        InstanceTask instanceTask = taskExecutor.getInstanceTask();
        instanceTask.remove(database);

        String taskName = instanceTask.getTaskName();
        if (instanceTask.size() == 0) {
            // Remove task level.
            if (taskName != null) {
                unregisterTask(taskName);
                clearTask(taskName);
            }
        } else {
            unregisterTask(taskName);

            TaskExecutor newTaskExecutor = taskBuilder.build(instanceTask);
            start(newTaskExecutor);

            registerTask(newTaskExecutor);
        }

        registryService.unregister(getServerHostAndPort(), database);

        logger.info("success to remove task.");
    }

    private String getServerHostAndPort() {
        return taskServerManager.findSelfHost() + ":4040";
    }

    @Scheduled(fixedDelay = 1000)
    public void registryAliveTask() {
        try {
            Set<String> dbs = FluentIterable.from(taskExecutors.values()).transformAndConcat(new Function<TaskExecutor, Iterable<Table>>() {
                @Override
                public Iterable<Table> apply(TaskExecutor input) {
                    return input.getTableSet().getTables();
                }
            }).transform(new Function<Table, String>() {
                @Override
                public String apply(Table input) {
                    return input.getSchemaName();
                }
            }).toSet();

            for (String db : dbs) {
                registryService.register(getServerHostAndPort(), db);
            }
        } catch (Exception e) {
            Cat.logError("Registry Alive Task Failed", e);
        }
    }

    public void merge(TaskExecutor mainTaskExecutor, TaskExecutor tempTaskExecutor) {
        if (!(mainTaskExecutor.isMerging() && mainTaskExecutor.isStop()
                && tempTaskExecutor.isMerging() && tempTaskExecutor.isStop())) {
            return;
        }

        InstanceTask mainInstanceTask = mainTaskExecutor.getInstanceTask();
        InstanceTask tempInstanceTask = tempTaskExecutor.getInstanceTask();
        mainInstanceTask.merge(tempInstanceTask);

        String tempTaskName = tempInstanceTask.getTaskName();
        unregisterTask(tempTaskName);
        clearTask(tempTaskName);

        TaskExecutor newTaskExecutor = taskBuilder.build(mainInstanceTask);
        start(newTaskExecutor);
        registerTask(newTaskExecutor);
    }

    public void upgrade(TaskExecutor taskExecutor) {
        logger.info("start upgrading task executor...");

        InstanceTask instanceTask = taskExecutor.getInstanceTask();
        if (instanceTask.isMain()) {
            return;
        }

        stop(taskExecutor);
        String oriTaskName = instanceTask.getTaskName();
        unregisterTask(oriTaskName);

        instanceTask.temp2Main();
        String taskName = instanceTask.getTaskName();
        instanceStorageManager.rename(oriTaskName, taskName);

        TaskExecutor newTaskExecutor = taskBuilder.build(instanceTask);
        start(newTaskExecutor);
        registerTask(newTaskExecutor);

        logger.info("success to upgrade task executor.");
    }

    protected void add(TaskExecutor taskExecutor) {
        InstanceTask instanceTask = taskExecutor.getInstanceTask();
        if (instanceTask != null) {
            List<DatabaseTask> databaseTasks = instanceTask.getDatabaseTasks();
            if (databaseTasks != null) {
                for (DatabaseTask databaseTask : databaseTasks) {
                    String database = databaseTask.getDatabase();
                    if (database != null) {
                        taskExecutors.put(database, taskExecutor);
                    }
                }
            }
        }
    }

    protected void registerDatabase(String database) {
        taskExecutors.remove(database);
    }

    protected void unregisterDatabase(String database) {
        taskExecutors.remove(database);
    }

    protected void registerTask(TaskExecutor taskExecutor) {
        InstanceTask instanceTask = taskExecutor.getInstanceTask();
        if (instanceTask != null) {
            List<DatabaseTask> databaseTasks = instanceTask.getDatabaseTasks();
            if (databaseTasks != null) {
                for (DatabaseTask databaseTask : databaseTasks) {
                    String database = databaseTask.getDatabase();
                    taskExecutors.put(database, taskExecutor);
                }
            }
        }
    }

    protected void unregisterTask(String taskName) {
        List<String> databases = new ArrayList<String>();

        for (Map.Entry<String, TaskExecutor> entry : taskExecutors.entrySet()) {
            String database = entry.getKey();
            TaskExecutor taskExecutor = entry.getValue();
            InstanceTask instanceTask = taskExecutor.getInstanceTask();
            if (instanceTask != null) {
                if (taskName.equals(instanceTask.getTaskName())) {
                    databases.add(database);
                }
            }
        }

        for (String database : databases) {
            taskExecutors.remove(database);
        }
    }

    protected void clearDatabase(String database) {
        databaseStorageManager.delete(database);
    }

    protected void clearTask(String taskName) {
        instanceStorageManager.remove(taskName);
    }

    protected int count(String instance) {
        List<TaskExecutor> taskExecutorList = new ArrayList<TaskExecutor>();
        for (TaskExecutor taskExecutor : taskExecutors.values()) {
            InstanceTask instanceTask = taskExecutor.getInstanceTask();
            if (instanceTask != null && instance.equals(instanceTask.getInstance())) {
                taskExecutorList.add(taskExecutor);
            }
        }
        return taskExecutorList.size();
    }

    protected TaskExecutor findTaskExecutor(String database) {
        return taskExecutors.get(database);
    }

    protected String findInstance(String database) {
        return instanceManager.getClusterByDb(database);
    }

    public void start(final TaskExecutor taskExecutor) {
        if (taskExecutor == null) {
            throw new NullPointerException("task executor");
        }

        try {
            pool.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        taskExecutor.start();
                    } catch (Throwable t) {
                        logger.error("task executor error occurs.", t);
                    }
                }
            });
        } catch (Throwable t) {
            throw new RuntimeException("failed to start task executor.", t);
        }
    }

    public void stop(final TaskExecutor taskExecutor) {
        if (taskExecutor == null) {
            throw new NullPointerException("task executor");
        }

        try {
            taskExecutor.stop();
        } catch (Throwable t) {
            throw new RuntimeException("failed to stop task executor.", t);
        }
    }
}
