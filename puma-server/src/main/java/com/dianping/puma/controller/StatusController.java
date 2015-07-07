package com.dianping.puma.controller;

import com.dianping.puma.common.SystemStatusContainer;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

/**
 * Dozer @ 7/7/15
 * mail@dozer.cc
 * http://www.dozer.cc
 */

@Controller
@RequestMapping(value = "/status")
public class StatusController {

    @RequestMapping(value = "", method = RequestMethod.GET)
    @ResponseBody
    public Object index() {
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
}
