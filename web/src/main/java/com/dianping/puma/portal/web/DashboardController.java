package com.dianping.puma.portal.web;

import com.dianping.puma.portal.model.DashboardModel;
import com.dianping.puma.portal.service.PumaTaskStatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Dozer @ 2015-11
 * mail@dozer.cc
 * http://www.dozer.cc
 */
@Controller
public class DashboardController {

    @Autowired
    PumaTaskStatusService pumaTaskStatusService;

    @RequestMapping(value = {"/dashboard"}, method = RequestMethod.GET)
    @ResponseBody
    public DashboardModel dashboard() {
        return pumaTaskStatusService.getDashboard();
    }
}
