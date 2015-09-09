package com.dianping.puma.server.controller;

import com.dianping.puma.status.SystemStatusManager;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class StatusController {

    @RequestMapping(method = RequestMethod.GET, value = "/")
    @ResponseBody
    public Object index() {
        return SystemStatusManager.getStatus();
    }

    @RequestMapping(method = RequestMethod.GET, value = "/status")
    @ResponseBody
    public Object status() {
        return SystemStatusManager.getStatus();
    }
}
