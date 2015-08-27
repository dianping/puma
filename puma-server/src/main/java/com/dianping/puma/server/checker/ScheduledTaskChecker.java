package com.dianping.puma.server.checker;

import com.dianping.cat.Cat;
import com.dianping.puma.biz.entity.PumaTargetEntity;
import com.dianping.puma.biz.entity.PumaTaskEntity;
import com.dianping.puma.biz.entity.SrcDbEntity;
import com.dianping.puma.biz.service.PumaTargetService;
import com.dianping.puma.core.config.ConfigManager;
import com.dianping.puma.core.model.Table;
import com.dianping.puma.core.model.TableSet;
import com.dianping.puma.instance.InstanceManager;
import com.dianping.puma.server.container.TaskContainer;
import com.dianping.puma.server.server.TaskServerManager;
import com.google.common.base.Equivalence;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
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
    PumaTargetService pumaTargetService;

    @Autowired
    TaskContainer taskContainer;

    @Autowired
    InstanceManager instanceManager;

    private Map<String, PumaTaskEntity> tasks = new ConcurrentHashMap<String, PumaTaskEntity>();

    protected Map<String, PumaTaskEntity> loadPumaTask() {
        final List<PumaTargetEntity> targets = new ArrayList<PumaTargetEntity>();
        for (String host : taskServerManager.findAuthorizedHosts()) {
            targets.addAll(pumaTargetService.findByHost(host));
        }

        Map<String, Set<PumaTargetEntity>> clusterTargetMap = new HashMap<String, Set<PumaTargetEntity>>();
        for (PumaTargetEntity target : targets) {
            String cluster = instanceManager.getClusterByDb(target.getDatabase());

            if (Strings.isNullOrEmpty(cluster)) {
                Cat.logError(String.format("%s not exists", target.getDatabase()), new IllegalArgumentException(target.getDatabase()));
                continue;
            }

            Set<PumaTargetEntity> clusterTarget = clusterTargetMap.get(cluster);
            if (clusterTarget == null) {
                clusterTarget = new HashSet<PumaTargetEntity>();
                clusterTargetMap.put(cluster, clusterTarget);
            }

            clusterTarget.add(target);
        }

        Map<String, PumaTaskEntity> tasks = new ConcurrentHashMap<String, PumaTaskEntity>();
        for (Map.Entry<String, Set<PumaTargetEntity>> entry : clusterTargetMap.entrySet()) {
            PumaTaskEntity entity = new PumaTaskEntity();
            entity.setName(entry.getKey());
            entity.setClusterName(entry.getKey());
            entity.setPreservedDay(3);

            TableSet tableSet = new TableSet();
            Date beginTime = null;
            for (PumaTargetEntity target : entry.getValue()) {
                tableSet.add(new Table(target.getDatabase(), target.getTable()));
                if (target.getBeginTime() != null &&
                        (beginTime == null || target.getBeginTime().compareTo(beginTime) < 0)) {
                    beginTime = target.getBeginTime();
                }
            }
            entity.setTableSet(tableSet);
            entity.setBeginTime(beginTime);

            ImmutableSet.Builder<SrcDbEntity> srcDbBuilder = ImmutableSet.builder();
            for (SrcDbEntity db : instanceManager.getUrlByCluster(entry.getKey())) {
                srcDbBuilder.add(db);
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

        MapDifference<String, PumaTaskEntity> taskDifference = Maps.difference(oriTasks, tasks, new PumaTaskEquivalence());

        // Created.
        handleCreatedTasks(taskDifference.entriesOnlyOnRight());

        // Updated.
        handleUpdatedTasks(taskDifference.entriesDiffering());

        // Deleted.
        handleDeletedTasks(taskDifference.entriesOnlyOnLeft());
    }

    @Scheduled(fixedDelay = 5 * 1000)
    public void scheduledCheck() {
        check();
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

    class PumaTaskEquivalence extends Equivalence<PumaTaskEntity> {
        @Override
        protected boolean doEquivalent(PumaTaskEntity a, PumaTaskEntity b) {
            if (!a.getName().equals(b.getName())) {
                return false;
            }

            if (!a.getTableSet().equals(b.getTableSet())) {
                return false;
            }


            Sets.SetView<SrcDbEntity> diff = Sets.difference(a.getSrcDbEntityList(), b.getSrcDbEntityList());
            return diff.size() == 0;
        }

        @Override
        protected int doHash(PumaTaskEntity entity) {
            return entity.hashCode();
        }
    }
}
