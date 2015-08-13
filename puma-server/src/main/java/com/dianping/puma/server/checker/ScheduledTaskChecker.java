package com.dianping.puma.server.checker;

import com.dianping.cat.Cat;
import com.dianping.puma.biz.entity.PumaTaskEntity;
import com.dianping.puma.biz.entity.PumaTaskTargetEntity;
import com.dianping.puma.biz.entity.SrcDbEntity;
import com.dianping.puma.biz.service.PumaTaskTargetService;
import com.dianping.puma.core.config.ConfigManager;
import com.dianping.puma.core.model.Table;
import com.dianping.puma.core.model.TableSet;
import com.dianping.puma.instance.InstanceManager;
import com.dianping.puma.server.container.TaskContainer;
import com.dianping.puma.server.server.TaskServerManager;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ScheduledTaskChecker implements TaskChecker {

    @Autowired
    TaskServerManager taskServerManager;

    @Autowired
    PumaTaskTargetService pumaTaskTargetService;

    @Autowired
    TaskContainer taskContainer;

    @Autowired
    InstanceManager instanceManager;

    @Autowired
    ConfigManager configManager;

    private Map<String, PumaTaskEntity> tasks = new ConcurrentHashMap<String, PumaTaskEntity>();

    protected Map<String, PumaTaskEntity> loadPumaTask() {
        final List<PumaTaskTargetEntity> targets = new ArrayList<PumaTaskTargetEntity>();
        for (String host : taskServerManager.findAuthorizedHosts()) {
            targets.addAll(pumaTaskTargetService.findTargetByServerName(host));
        }

        Map<String, Set<PumaTaskTargetEntity>> clusterTargetMap = new HashMap<String, Set<PumaTaskTargetEntity>>();
        for (PumaTaskTargetEntity target : targets) {
            String cluster = instanceManager.getClusterByDb(target.getDatabase());

            if (Strings.isNullOrEmpty(cluster)) {
                Cat.logError(String.format("%s not exists", target.getDatabase()), new IllegalArgumentException(target.getDatabase()));
                continue;
            }

            Set<PumaTaskTargetEntity> clusterTarget = clusterTargetMap.get(cluster);
            if (clusterTarget == null) {
                clusterTarget = new HashSet<PumaTaskTargetEntity>();
                clusterTargetMap.put(cluster, clusterTarget);
            }

            clusterTarget.add(target);
        }

        Map<String, PumaTaskEntity> tasks = new ConcurrentHashMap<String, PumaTaskEntity>();
        for (Map.Entry<String, Set<PumaTaskTargetEntity>> entry : clusterTargetMap.entrySet()) {
            PumaTaskEntity entity = new PumaTaskEntity();
            entity.setName(entry.getKey());
            entity.setClusterName(entry.getKey());
            entity.setPreservedDay(3);

            TableSet tableSet = new TableSet();
            for (PumaTaskTargetEntity target : entry.getValue()) {
                tableSet.add(new Table(target.getDatabase(), target.getTable()));
            }
            entity.setTableSet(tableSet);

            ImmutableList.Builder<SrcDbEntity> srcDbBuilder = ImmutableList.builder();
            for (String url : instanceManager.getUrlByCluster(entry.getKey())) {
                SrcDbEntity srcDbEntity = new SrcDbEntity();
                String[] urlAndPort = url.split(":");
                srcDbEntity.setHost(urlAndPort[0]);
                srcDbEntity.setPort(Integer.parseInt(urlAndPort[1]));
                srcDbEntity.setUsername(configManager.getConfig("puma.server.binlog.username"));
                srcDbEntity.setPassword(configManager.getConfig("puma.server.binlog.password"));
                srcDbBuilder.add(srcDbEntity);
            }
            entity.setSrcDbEntityList(srcDbBuilder.build());

            tasks.put(entity.getName(), entity);
        }

        return tasks;
    }

    @Override
    public void check() {
        Map<String, PumaTaskEntity> oriTasks = tasks;

        try {
            tasks = loadPumaTask();
        } catch (Exception e) {
            // @todo.
            tasks = oriTasks;
            return;
        }

        // Created.
        handleCreatedTasks(findCreatedTasks(oriTasks, tasks));

        // Updated.
        handleUpdatedTasks(findUpdatedTasks(oriTasks, tasks));

        // Deleted.
        handleDeletedTasks(findDeletedTasks(oriTasks, tasks));
    }

    @Scheduled(fixedDelay = 5 * 1000)
    public void scheduledCheck() {
        check();
    }

    protected Map<String, PumaTaskEntity> findCreatedTasks(Map<String, PumaTaskEntity> oriTasks,
                                                           Map<String, PumaTaskEntity> tasks) {
        MapDifference<String, PumaTaskEntity> taskDifference = Maps.difference(oriTasks, tasks);
        return taskDifference.entriesOnlyOnRight();
    }

    protected Map<String, MapDifference.ValueDifference<PumaTaskEntity>> findUpdatedTasks(
            Map<String, PumaTaskEntity> oriTasks, Map<String, PumaTaskEntity> tasks) {
        MapDifference<String, PumaTaskEntity> taskDifference = Maps.difference(oriTasks, tasks);
        return taskDifference.entriesDiffering();
    }

    protected Map<String, PumaTaskEntity> findDeletedTasks(Map<String, PumaTaskEntity> oriTasks,
                                                           Map<String, PumaTaskEntity> tasks) {
        MapDifference<String, PumaTaskEntity> taskDifference = Maps.difference(oriTasks, tasks);
        return taskDifference.entriesOnlyOnLeft();
    }

    private void handleCreatedTasks(Map<String, PumaTaskEntity> createdTasks) {
        for (Map.Entry<String, PumaTaskEntity> entry : createdTasks.entrySet()) {
            try {
                taskContainer.create(entry.getKey(), entry.getValue());
            } catch (Exception e) {
                // @todo.
            }
        }
    }

    private void handleUpdatedTasks(Map<String, MapDifference.ValueDifference<PumaTaskEntity>> updatedTasks) {
        for (Map.Entry<String, MapDifference.ValueDifference<PumaTaskEntity>> entry : updatedTasks.entrySet()) {
            try {
                taskContainer.update(entry.getKey(), entry.getValue().leftValue(), entry.getValue().rightValue());
            } catch (Exception e) {
                // @todo.
            }
        }
    }

    private void handleDeletedTasks(Map<String, PumaTaskEntity> deletedTasks) {
        for (Map.Entry<String, PumaTaskEntity> entry : deletedTasks.entrySet()) {
            try {
                taskContainer.delete(entry.getKey(), entry.getValue());
            } catch (Exception e) {
                // @todo.
            }
        }
    }
}
