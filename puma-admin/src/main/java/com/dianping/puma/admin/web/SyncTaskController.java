package com.dianping.puma.admin.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.dianping.puma.admin.monitor.SystemStatusContainer;
import com.dianping.puma.core.service.SyncTaskService;
import com.dianping.puma.core.entity.SyncTask;

@Controller
public class SyncTaskController {
	
	 @Autowired
	 private SyncTaskService syncTaskService;
	 @Autowired
	 private SystemStatusContainer systemStatusContainer;
	 private static final int PAGESIZE = 30;
	 
	 @RequestMapping(value = { "/sync-task" })
	 public ModelAndView created(HttpServletRequest request, HttpServletResponse response) {
	      return created0(request, response, 1);
	 }

	 @RequestMapping(value = { "/sync-task/{pageNum}" })
	 public ModelAndView created0(HttpServletRequest request, HttpServletResponse response, @PathVariable("pageNum") Integer pageNum) {
	        Map<String, Object> map = new HashMap<String, Object>();
	        //        System.out.println(syncConfigService.find());
	        int offset = pageNum == null ? 0 : (pageNum - 1) * PAGESIZE;
	        List<SyncTask> syncTasks = syncTaskService.find(offset, PAGESIZE);
	        map.put("syncTasks", syncTasks);
	        map.put("createdActive", "active");
	        map.put("subPath", "main");
	        map.put("path", "sync-task");
	        return new ModelAndView("main/container", map);
	    }

}
