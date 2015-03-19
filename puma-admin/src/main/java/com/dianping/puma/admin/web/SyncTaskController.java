package com.dianping.puma.admin.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.dianping.puma.admin.reporter.SyncTaskOperationReporter;
import com.dianping.puma.admin.util.GsonUtil;
import com.dianping.puma.core.constant.Operation;
import com.dianping.puma.core.constant.SyncType;
import com.dianping.swallow.common.producer.exceptions.SendFailedException;
import com.mongodb.MongoException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
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

	@Autowired
	private SyncTaskOperationReporter syncTaskOperationReporter;

	private static final int PAGESIZE = 30;

	@RequestMapping(value = { "/sync-task" })
	public ModelAndView created(HttpServletRequest request, HttpServletResponse response) {
		return created0(request, response, 1);
	}

	@RequestMapping(value = { "/sync-task/{pageNum}" })
	public ModelAndView created0(HttpServletRequest request, HttpServletResponse response,
			@PathVariable("pageNum") Integer pageNum) {
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

	@RequestMapping(value = { "/sync-task/remove" }, method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	public String removePost(String id) {
		Map<String, Object> map = new HashMap<String, Object>();

		try {
			// Actually name here.
			SyncTask syncTask = syncTaskService.find(id);

			syncTaskService.remove(id);

			// Publish puma task operation event to puma server.
			syncTaskOperationReporter.report(syncTask.getSyncServerName(), SyncType.SYNC, id, Operation.REMOVE);

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

}
