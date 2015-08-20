package com.dianping.puma.server.controller;

import com.dianping.puma.server.container.TaskContainer;
import com.dianping.puma.status.SystemStatusManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping(value = "/status")
public class StatusController {

    @Autowired
    TaskContainer taskContainer;

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> index() {
        Map<String, Object> status = new HashMap<String, Object>();

        status.put("status", SystemStatusManager.status);

        return status;
    }
}
