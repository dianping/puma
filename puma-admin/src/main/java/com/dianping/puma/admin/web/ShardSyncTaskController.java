package com.dianping.puma.admin.web;

import com.dianping.puma.admin.remote.reporter.ShardSyncTaskControllerReporter;
import com.dianping.puma.admin.remote.reporter.ShardSyncTaskOperationReporter;
import com.dianping.puma.core.constant.ActionOperation;
import com.dianping.puma.core.entity.ShardSyncTask;
import com.dianping.puma.core.entity.SyncServer;
import com.dianping.puma.core.model.state.ShardSyncTaskState;
import com.dianping.puma.core.service.ShardSyncTaskService;
import com.dianping.puma.core.service.ShardSyncTaskStateService;
import com.dianping.puma.core.service.SyncServerService;
import com.dianping.swallow.common.producer.exceptions.SendFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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
public class ShardSyncTaskController {
    private static final Logger LOG = LoggerFactory.getLogger(ShardSyncTaskController.class);

    private static final int PAGESIZE = 30;

    @Autowired
    private ShardSyncTaskService shardSyncTaskService;

    @Autowired
    private SyncServerService syncServerService;

    @Autowired
    private ShardSyncTaskStateService shardSyncTaskStateService;

    @Autowired
    private ShardSyncTaskControllerReporter shardSyncTaskControllerReporter;

    @Autowired
    private ShardSyncTaskOperationReporter shardSyncTaskOperationReporter;


    @RequestMapping(value = {"/shard-sync-task/create"}, method = RequestMethod.GET)
    public ModelAndView create() {
        List<SyncServer> syncServers = syncServerService.findAll();

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("subPath", "create");
        map.put("path", "shard-sync-task");
        map.put("syncServers", syncServers);
        return new ModelAndView("main/container", map);
    }

    @RequestMapping(value = {"/shard-sync-task/create"}, method = RequestMethod.POST)
    public String create(String tableName, String ruleName, String syncServerName) throws SendFailedException {
        ShardSyncTask task = new ShardSyncTask();
        task.setTableName(tableName);
        task.setRuleName(ruleName);
        task.setSyncServerName(syncServerName);
        task.setName("ShardSyncTask-" + ruleName + "-" + tableName);
        shardSyncTaskService.create(task);

        shardSyncTaskOperationReporter.report(syncServerName, task.getName(), ActionOperation.CREATE);

        return "redirect:/shard-sync-task";
    }

    @RequestMapping(value = {"/shard-sync-task/remove}"}, method = RequestMethod.POST)
    public String remove(String name) throws SendFailedException {

        ShardSyncTask task = shardSyncTaskService.find(name);
        if (task != null) {
            shardSyncTaskService.remove(name);
            shardSyncTaskOperationReporter.report(task.getSyncServerName(), task.getName(), ActionOperation.REMOVE);
        }

        return "redirect:/shard-sync-task";
    }

    @RequestMapping(value = {"/shard-sync-task"})
    public ModelAndView index(HttpServletRequest request, HttpServletResponse response) {
        return index(request, response, 1);
    }

    @RequestMapping(value = {"/shard-sync-task/{pageNum}"})
    public ModelAndView index(HttpServletRequest request, HttpServletResponse response,
                              @PathVariable("pageNum") Integer pageNum) {
        Map<String, Object> map = new HashMap<String, Object>();
        int offset = pageNum == null ? 0 : (pageNum - 1) * PAGESIZE;
        List<ShardSyncTask> shardSyncTasks = shardSyncTaskService.find(offset, PAGESIZE);
        Map<String, ShardSyncTaskState> states = new HashMap<String, ShardSyncTaskState>();
        for (ShardSyncTask task : shardSyncTasks) {
            ShardSyncTaskState state = shardSyncTaskStateService.find(task.getName());
            if (state != null) {
                states.put(task.getName(), state);
            }
        }

        map.put("shardSyncTasks", shardSyncTasks);
        map.put("shardSyncTaskStates", states);
        map.put("createdActive", "active");
        map.put("subPath", "main");
        map.put("path", "shard-sync-task");
        return new ModelAndView("main/container", map);
    }
}
