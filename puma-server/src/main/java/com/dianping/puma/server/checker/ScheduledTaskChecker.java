package com.dianping.puma.server.checker;

import com.dianping.puma.biz.entity.PumaServerTargetEntity;
import com.dianping.puma.biz.service.PumaServerTargetService;
import com.dianping.puma.server.container.DatabaseTaskContainer;
import com.dianping.puma.server.server.TaskServerManager;
import com.google.common.collect.MapDifference;
import com.google.common.collect.MapDifference.ValueDifference;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.dianping.puma.server.container.DatabaseTaskContainer.*;

@Service
public class ScheduledTaskChecker implements TaskChecker {

    private final Logger logger = LoggerFactory.getLogger(ScheduledTaskChecker.class);

    @Autowired
    TaskServerManager taskServerManager;

    @Autowired
    PumaServerTargetService pumaServerTargetService;

    @Autowired
    DatabaseTaskContainer databaseTaskContainer;

    protected Map<String, DatabaseTask> loadDatabaseTasks() {
        List<PumaServerTargetEntity> pumaServerTargets = new ArrayList<PumaServerTargetEntity>();

        for (String host: taskServerManager.findAuthorizedHosts()) {
            pumaServerTargets.addAll(pumaServerTargetService.findByServerHost(host));
        }

        Map<String, DatabaseTask> databaseTasks = new HashMap<String, DatabaseTask>();
        for (PumaServerTargetEntity pumaServerTarget: pumaServerTargets) {
            String database = pumaServerTarget.getTargetDb();
            List<String> tables = pumaServerTarget.getTables();
            Date beginTime = pumaServerTarget.getBeginTime();
            DatabaseTask databaseTask = new DatabaseTask(database, tables, beginTime);
            databaseTasks.put(database, databaseTask);
        }

        return databaseTasks;
    }

    protected void handleCreatedDatabaseTask(Map<String, DatabaseTask> createdDatabaseTasks) {
        for (Map.Entry<String, DatabaseTask> entry: createdDatabaseTasks.entrySet()) {
            DatabaseTask databaseTask = entry.getValue();
            try {
                databaseTaskContainer.create(databaseTask);
            } catch (Throwable t) {
                logger.error("failed to create task.", t);
            }
        }
    }

    protected void handleRemovedDatabaseTask(Map<String, DatabaseTask> removedDatabaseTasks) {
        for (Map.Entry<String, DatabaseTask> entry: removedDatabaseTasks.entrySet()) {
            String database = entry.getKey();
            try {
                databaseTaskContainer.remove(database);
            } catch (Throwable t) {
                logger.error("failed to remove task.", t);
            }
        }
    }

    protected void handleUpdatedDatabaseTask(Map<String, DatabaseTask> updatedDatabaseTasks) {
        for (Map.Entry<String, DatabaseTask> entry: updatedDatabaseTasks.entrySet()) {
            DatabaseTask databaseTask = entry.getValue();
            try {
                databaseTaskContainer.update(databaseTask);
            } catch (Throwable t) {
                logger.error("failed to update task.", t);
            }
        }
    }

    @Override
    public void check() {
        Map<String, DatabaseTask> databaseTasks = loadDatabaseTasks();
        Map<String, DatabaseTask> oriDatabaseTasks = databaseTaskContainer.getAll();

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

        handleCreatedDatabaseTask(createdDatabaseTasks);
        handleRemovedDatabaseTask(removedDatabaseTasks);
        handleUpdatedDatabaseTask(updatedDatabaseTasks);
    }

    @Scheduled(fixedDelay = 5 * 1000)
    public void scheduledCheck() {
        check();
    }
}
