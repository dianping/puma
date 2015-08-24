package com.dianping.puma.admin.web;

import com.dianping.puma.admin.model.PumaDto;
import com.dianping.puma.biz.entity.PumaServerTargetEntity;
import com.dianping.puma.biz.service.PumaServerService;
import com.dianping.puma.biz.service.PumaServerTargetService;
import com.dianping.puma.biz.service.PumaTargetService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class PumaController extends BasicController {

    private final Logger logger = LoggerFactory.getLogger(PumaController.class);

    @Autowired
    PumaServerService pumaServerService;

    @Autowired
    PumaTargetService pumaTargetService;

    @Autowired
    PumaServerTargetService pumaServerTargetService;

    @RequestMapping(value = {"/puma-target"}, method = RequestMethod.POST)
    @ResponseBody
    public Object ajaxList(@RequestBody PumaDto pumaDto) {
        Map<String, Object> result = new HashMap<String, Object>();

        String database = pumaDto.getDatabase();
        List<PumaServerTargetEntity> pumaServerTargets = pumaServerTargetService.findByDatabase(database);
        for (PumaServerTargetEntity pumaServerTarget : pumaServerTargets) {
            pumaDto.setTables(pumaServerTarget.getTables());
            pumaDto.addServerName(pumaServerTarget.getServerName());
            pumaDto.addBeginTime(pumaServerTarget.getServerName(), pumaServerTarget.getBeginTime());
        }

        result.put("status", "success");
        result.put("result", pumaDto);
        return result;
    }

    @RequestMapping(value = {"/puma-create"}, method = RequestMethod.POST)
    @ResponseBody
    public Object ajaxCreate(@RequestBody PumaDto pumaDto) {
        Map<String, Object> result = new HashMap<String, Object>();

        String database = pumaDto.getDatabase();
        List<String> tables = pumaDto.getTables();

        for (String serverName : pumaDto.getServerNames()) {
            PumaServerTargetEntity pumaServerTarget = new PumaServerTargetEntity();
            pumaServerTarget.setServerName(serverName);
            pumaServerTarget.setTargetDb(database);
            pumaServerTarget.setTables(tables);
            pumaServerTarget.setBeginTime(pumaDto.getBeginTimes().get(serverName));
            pumaServerTargetService.replace(pumaServerTarget);
        }

        result.put("status", "success");
        return result;
    }
}
