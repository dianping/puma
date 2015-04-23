package com.dianping.puma.admin.web;

import com.dianping.puma.admin.remote.reporter.ShardSyncTaskControllerReporter;
import com.dianping.puma.admin.remote.reporter.ShardSyncTaskOperationReporter;
import com.dianping.puma.admin.util.GsonUtil;
import com.dianping.puma.core.constant.ActionOperation;
import com.dianping.puma.core.constant.SyncType;
import com.dianping.puma.core.entity.DstDBInstance;
import com.dianping.puma.core.entity.ShardDumpTask;
import com.dianping.puma.core.entity.SrcDBInstance;
import com.dianping.puma.core.entity.SyncServer;
import com.dianping.puma.core.model.state.ShardSyncTaskState;
import com.dianping.puma.core.service.*;
import com.dianping.swallow.common.producer.exceptions.SendFailedException;
import com.mongodb.MongoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Dozer @ 2015-02
 * mail@dozer.cc
 * http://www.dozer.cc
 */

@Controller
public class ShardDumpTaskController {
    private static final Logger LOG = LoggerFactory.getLogger(ShardDumpTaskController.class);

    private static final int PAGESIZE = 1000;

    @Autowired
    private SrcDBInstanceService srcDBInstanceService;

    @Autowired
    private DstDBInstanceService dstDBInstanceService;

    @Autowired
    private ShardDumpTaskService shardDumpTaskService;

    @Autowired
    private SyncServerService syncServerService;

    @Autowired
    private ShardSyncTaskStateService shardSyncTaskStateService;

    @Autowired
    private ShardSyncTaskControllerReporter shardSyncTaskControllerReporter;

    @Autowired
    private ShardSyncTaskOperationReporter shardSyncTaskOperationReporter;


    @RequestMapping(value = {"shard-dump-task/create"}, method = RequestMethod.GET)
    public ModelAndView create() {
        List<SyncServer> syncServers = syncServerService.findAll();
        List<DstDBInstance> dstDBInstances = dstDBInstanceService.findAll();
        List<SrcDBInstance> srcDBInstances = srcDBInstanceService.findAll();

        Map<String, Object> map = new HashMap<String, Object>();


        map.put("subPath", "create");
        map.put("path", "shard-dump-task");
        map.put("syncServers", syncServers);
        map.put("dstDBInstances", dstDBInstances);
        map.put("srcDBInstances", srcDBInstances);

        return new ModelAndView("main/container", map);
    }

    @RequestMapping(value = {"shard-dump-task/create"}, method = RequestMethod.POST)
    public String create(@ModelAttribute ShardDumpTask task, String syncServerName) throws SendFailedException {
//        task.setSyncServerName(syncServerName);
//        task.setName(String.format("ShardSyncTask-%s-%s-%s", task.getRuleName(), task.getTableName(), task.isMigrate() ? "migrate" : "sync"));
//        shardDumpTaskService.create(task);
//
//        shardSyncTaskOperationReporter.report(syncServerName, task.getName(), SyncType.SHARD_DUMP, ActionOperation.CREATE);

        return "redirect:/shard-dump-task";
    }

    @RequestMapping(value = {"shard-dump-task/remove"}, method = RequestMethod.POST)
    @ResponseBody
    public String remove(String name) throws SendFailedException {
        Map<String, Object> map = new HashMap<String, Object>();

        try {
            ShardDumpTask task = shardDumpTaskService.find(name);
            if (task != null) {
                shardDumpTaskService.remove(name);
                shardSyncTaskOperationReporter.report(task.getSyncServerName(), task.getName(), SyncType.SHARD_DUMP, ActionOperation.REMOVE);
            }

            map.put("success", true);
        } catch (MongoException e) {
            map.put("error", "storage");
            map.put("success", false);
        } catch (SendFailedException e) {
            map.put("error", "notify");
            map.put("success", false);
        } catch (Exception e) {
            map.put("error", e.getMessage());
            map.put("success", false);
        }

        map.put("success", true);

        return GsonUtil.toJson(map);
    }

    @RequestMapping(value = {"shard-dump-task"})
    public ModelAndView index(HttpServletRequest request, HttpServletResponse response) {
        return index(request, response, 1);
    }

    @RequestMapping(value = {"shard-dump-task/{pageNum}"})
    public ModelAndView index(HttpServletRequest request, HttpServletResponse response,
                              @PathVariable("pageNum") Integer pageNum) {
        Map<String, Object> map = new HashMap<String, Object>();
        int offset = pageNum == null ? 0 : (pageNum - 1) * PAGESIZE;
        List<ShardDumpTask> shardSyncTasks = shardDumpTaskService.find(offset, PAGESIZE);
        Map<String, ShardSyncTaskState> states = new HashMap<String, ShardSyncTaskState>();
        for (ShardDumpTask task : shardSyncTasks) {
            ShardSyncTaskState state = shardSyncTaskStateService.find(task.getName());
            if (state != null) {
                states.put(task.getName(), state);
            }
        }

        map.put("shardSyncTasks", shardSyncTasks);
        map.put("shardSyncTaskStates", states);
        map.put("createdActive", "active");
        map.put("subPath", "main");
        map.put("path", "shard-dump-task");
        return new ModelAndView("main/container", map);
    }
}
