package com.dianping.puma.controller;

import com.dianping.puma.common.SystemStatusContainer;
import com.dianping.puma.config.PumaServerConfig;
import com.dianping.puma.biz.entity.PumaTaskState;
import com.dianping.puma.server.TaskExecutor;
import com.dianping.puma.server.TaskExecutorContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

/**
 * Dozer @ 7/7/15
 * mail@dozer.cc
 * http://www.dozer.cc
 */

@Controller
@RequestMapping(value = "/status")
public class StatusController {

    @Autowired
    TaskExecutorContainer taskExecutorContainer;

    @Autowired
    PumaServerConfig pumaServerConfig;

    @RequestMapping(value = "", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> index() {
        Map<String, Object> status = new HashMap<String, Object>();
        status.put("serverStatus", SystemStatusContainer.instance.listServerStatus());
        status.put("serverDdlCounters", SystemStatusContainer.instance.listServerDdlCounters());
        status.put("serverRowDeleteCounters", SystemStatusContainer.instance.listServerRowDeleteCounters());
        status.put("serverRowInsertCounters", SystemStatusContainer.instance.listServerRowInsertCounters());
        status.put("serverRowUpdateCounters", SystemStatusContainer.instance.listServerRowUpdateCounters());
        status.put("clientStatus", SystemStatusContainer.instance.listClientStatus());
        status.put("storageStatus", SystemStatusContainer.instance.listStorageStatus());

        return status;
    }

    @RequestMapping(value = "puma-task", method = RequestMethod.GET)
    @ResponseBody
    public List<PumaTaskState> pumaTask() {
        List<PumaTaskState> pumaTaskStates = new ArrayList<PumaTaskState>();
        for (TaskExecutor taskExecutor : taskExecutorContainer.getAll()) {
            PumaTaskState taskState = taskExecutor.getTaskState();
            taskState.setServerName(pumaServerConfig.getName());
            taskState.setName(taskState.getTaskName());
            taskState.setGmtUpdate(new Date());
            pumaTaskStates.add(taskState);
        }
        return pumaTaskStates;
    }
}
