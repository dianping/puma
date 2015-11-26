package com.dianping.puma.server.checker;

import com.dianping.puma.biz.entity.PumaServerTargetEntity;
import com.dianping.puma.biz.entity.SrcDbEntity;
import com.dianping.puma.biz.service.PumaServerTargetService;
import com.dianping.puma.instance.InstanceManager;
import com.dianping.puma.server.container.TaskContainer;
import com.dianping.puma.server.server.TaskServerManager;
import com.dianping.puma.storage.manage.InstanceStorageManager;
import com.dianping.puma.taskexecutor.TaskExecutor;
import com.dianping.puma.taskexecutor.task.DatabaseTask;
import com.dianping.puma.taskexecutor.task.InstanceTask;
import com.google.common.collect.MapDifference;
import com.google.common.collect.MapDifference.ValueDifference;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ScheduledTaskChecker implements TaskChecker {

    private final Logger logger = LoggerFactory.getLogger(ScheduledTaskChecker.class);

    private boolean inited = false;

    @Autowired
    TaskServerManager taskServerManager;

    @Autowired
    PumaServerTargetService pumaServerTargetService;

    @Autowired
    TaskContainer taskContainer;

    @Autowired
    InstanceStorageManager instanceStorageManager;

    @Autowired
    InstanceManager instanceManager;

    private Map<String, Set<SrcDbEntity>> sources = new HashMap<String, Set<SrcDbEntity>>();

    protected Map<String, DatabaseTask> loadDatabaseTasks() {
        List<PumaServerTargetEntity> pumaServerTargets
                = pumaServerTargetService.findByServerHost(taskServerManager.findSelfHost());

        Map<String, DatabaseTask> databaseTasks = new HashMap<String, DatabaseTask>();
        for (PumaServerTargetEntity pumaServerTarget : pumaServerTargets) {
            String database = pumaServerTarget.getTargetDb();
            List<String> tables = pumaServerTarget.getTables();
            Date beginTime = pumaServerTarget.getBeginTime();
            DatabaseTask databaseTask = new DatabaseTask(database, tables, beginTime);
            databaseTasks.put(database, databaseTask);
        }

        return databaseTasks;
    }

    protected Map<String, InstanceTask> loadInstanceTasks() {
        Map<String, InstanceTask> instanceTasks = new HashMap<String, InstanceTask>();

        Map<String, DatabaseTask> databaseTasks = loadDatabaseTasks();
        for (Map.Entry<String, DatabaseTask> entry : databaseTasks.entrySet()) {
            String database = entry.getKey();
            DatabaseTask databaseTask = entry.getValue();

            String instance = findInstance(database);
            if (instance == null) {
                logger.error("failed to find instance");
                continue;
            }

            String slaveTaskName = genSlaveTaskName(instance, database);
            if (instanceStorageManager.getBinlogInfo(slaveTaskName) != null) {
                InstanceTask instanceTask = new InstanceTask(false, instance, databaseTask);
                instanceTasks.put(slaveTaskName, instanceTask);
                continue;
            }

            String masterTaskName = genMasterTaskName(instance, database);
            if (instanceStorageManager.getBinlogInfo(masterTaskName) != null) {
                InstanceTask instanceTask = instanceTasks.get(masterTaskName);
                if (instanceTask == null) {
                    instanceTask = new InstanceTask(true, instance, databaseTask);
                } else {
                    instanceTask.create(databaseTask);
                }
                instanceTasks.put(masterTaskName, instanceTask);
            }
        }

        return instanceTasks;

    }

    protected void handleInstanceTask(Map<String, InstanceTask> instanceTasks) {
        for (InstanceTask instanceTask : instanceTasks.values()) {
            taskContainer.create(instanceTask);
        }
    }

    protected void handleCreatedDatabaseTask(Map<String, DatabaseTask> createdDatabaseTasks) {
        for (Map.Entry<String, DatabaseTask> entry : createdDatabaseTasks.entrySet()) {
            DatabaseTask databaseTask = entry.getValue();
            addSource(databaseTask.getDatabase());
            try {
                taskContainer.create(databaseTask);
            } catch (Throwable t) {
                logger.error("failed to create task.", t);
            }
        }
    }

    protected void handleRemovedDatabaseTask(Map<String, DatabaseTask> removedDatabaseTasks) {
        for (Map.Entry<String, DatabaseTask> entry : removedDatabaseTasks.entrySet()) {
            String database = entry.getKey();
            removeSource(database);
            try {
                taskContainer.remove(database);
            } catch (Throwable t) {
                logger.error("failed to remove task.", t);
            }
        }
    }

    protected void handleUpdatedDatabaseTask(Map<String, DatabaseTask> updatedDatabaseTasks) {
        for (Map.Entry<String, DatabaseTask> entry : updatedDatabaseTasks.entrySet()) {
            DatabaseTask databaseTask = entry.getValue();
            try {
                taskContainer.update(databaseTask);
            } catch (Throwable t) {
                logger.error("failed to update task.", t);
            }
        }
    }

    protected void handleCommonDatabaseTask(Map<String, DatabaseTask> commonDatabaseTasks) {
        for (Map.Entry<String, DatabaseTask> entry : commonDatabaseTasks.entrySet()) {
            String database = entry.getKey();
            if (checkSource(database)) {
                TaskExecutor taskExecutor = taskContainer.getExecutor(database);
                if (taskExecutor != null) {
                    taskContainer.stop(taskExecutor);
                    taskContainer.start(taskExecutor);
                }
            }
        }
    }

    protected void addSource(String database) {
        String instance = instanceManager.getClusterByDb(database);
        if (instance != null) {
            Set<SrcDbEntity> srcDbEntities = instanceManager.getUrlByCluster(instance);
            if (srcDbEntities != null) {
                sources.put(database, srcDbEntities);
            }
        }
    }

    protected void removeSource(String database) {
        sources.remove(database);
    }

    protected boolean checkSource(String database) {
        String instance = instanceManager.getClusterByDb(database);
        if (instance != null) {
            Set<SrcDbEntity> oriSrcDbEntities = sources.get(instance);
            Set<SrcDbEntity> srcDbEntities = instanceManager.getUrlByCluster(instance);
            if (oriSrcDbEntities != null) {
                if (!oriSrcDbEntities.equals(srcDbEntities)) {
                    return true;
                }
            } else {
                if (srcDbEntities != null) {
                    sources.put(database, srcDbEntities);
                }
            }
        }
        return false;
    }

    protected void checkInit() {
        Map<String, InstanceTask> instanceTasks = loadInstanceTasks();
        handleInstanceTask(instanceTasks);
    }

    protected void check0() {
        Map<String, DatabaseTask> databaseTasks = loadDatabaseTasks();
        Map<String, DatabaseTask> oriDatabaseTasks = taskContainer.getDatabaseTasks();

        MapDifference<String, DatabaseTask> difference = Maps.difference(oriDatabaseTasks, databaseTasks);

        Map<String, DatabaseTask> createdDatabaseTasks = difference.entriesOnlyOnRight();
        Map<String, DatabaseTask> removedDatabaseTasks = difference.entriesOnlyOnLeft();
        Map<String, DatabaseTask> updatedDatabaseTasks = Maps.transformEntries(difference.entriesDiffering(),
                new Maps.EntryTransformer<String, ValueDifference<DatabaseTask>, DatabaseTask>() {
                    @Override
                    public DatabaseTask transformEntry(String key, ValueDifference<DatabaseTask> value) {
                        return value.rightValue();
                    }
                }
        );
        Map<String, DatabaseTask> commonDatabaseTasks = difference.entriesInCommon();

        handleCreatedDatabaseTask(createdDatabaseTasks);
        handleRemovedDatabaseTask(removedDatabaseTasks);
        handleUpdatedDatabaseTask(updatedDatabaseTasks);
        handleCommonDatabaseTask(commonDatabaseTasks);
    }

    @Override
    public void check() {
        if (!inited) {
            checkInit();
            inited = true;
        } else {
            check0();
        }
    }

    @Scheduled(fixedDelay = 5 * 1000)
    public void scheduledCheck() {
        check();
    }

    protected String genMasterTaskName(String instance, String database) {
        return instance;
    }

    protected String genSlaveTaskName(String instance, String database) {
        return instance + "-" + database;
    }

    protected String findInstance(String database) {
        return instanceManager.getClusterByDb(database);
    }
}
