package com.dianping.puma.admin.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.dianping.puma.admin.reporter.SyncTaskOperationReporter;
import com.dianping.puma.admin.util.GsonUtil;
import com.dianping.puma.core.constant.ActionController;
import com.dianping.puma.core.constant.ActionOperation;
import com.dianping.puma.core.constant.Status;
import com.dianping.puma.core.constant.SyncType;
import com.dianping.puma.core.container.SyncTaskStateContainer;
import com.dianping.puma.core.model.SyncTaskState;
import com.dianping.swallow.common.producer.exceptions.SendFailedException;
import com.mongodb.MongoException;

import org.codehaus.plexus.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

	private static final Logger LOG = LoggerFactory.getLogger(SyncTaskController.class);
	@Autowired
	private SyncTaskService syncTaskService;

	@Autowired
	private SyncTaskStateContainer syncTaskStateContainer;

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
			syncTaskOperationReporter.report(syncTask.getSyncServerName(), SyncType.SYNC, id, ActionOperation.REMOVE);

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

    /**
     * 查询SyncTask状态
     */
    @RequestMapping(value = "/sync-task/status", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    @ResponseBody
    public Object status(HttpSession session, String taskName) {
        Map<String, Object> map = new HashMap<String, Object>();
         try {
        	SyncTaskState syncTaskState = syncTaskStateContainer.get(taskName);
            //binlog信息，从数据库查询binlog位置即可，不需要从SyncServer实时发过来的status中的binlog获取
            //binlogInfoOfIOThread则是从status中获取
            SyncTask syncTask = this.syncTaskService.find(taskName);
            syncTaskState.setBinlogInfo(syncTask.getBinlogInfo());

            map.put("status", syncTaskState);
            map.put("success", true);
        } catch (IllegalArgumentException e) {
            map.put("success", false);
            map.put("errorMsg", e.getMessage());
        } catch (Exception e) {
            map.put("success", false);
            map.put("errorMsg", e.getMessage());
            LOG.error(e.getMessage(), e);
        }
        return GsonUtil.toJson(map);
    }

    /**
     * 查询SyncTask
     */
    @RequestMapping(value = "/sync-task/detail/{taskName}", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public ModelAndView task(HttpSession session, @PathVariable String taskName) {
        Map<String, Object> map = new HashMap<String, Object>();
        SyncTask syncTask = this.syncTaskService.find(taskName);
        SyncTaskState syncState = syncTaskStateContainer.get(taskName);
        map.put("syncTask", syncTask);
        map.put("syncState", syncState);
        map.put("createdActive", "active");
        map.put("subPath", "detail");
        map.put("path", "sync-task");
        return new ModelAndView("main/container", map);
    }

    /**
     * 暂停SyncTask状态
     */
    @RequestMapping(value = "/sync-task/pause", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    @ResponseBody
    public Object pause(HttpSession session, String taskName) {
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            this.syncTaskService.updateStatusAction(taskName, ActionController.PAUSE);
            map.put("success", true);
        } catch (IllegalArgumentException e) {
            map.put("success", false);
            map.put("errorMsg", e.getMessage());
        } catch (Exception e) {
            map.put("success", false);
            map.put("errorMsg", e.getMessage());
            LOG.error(e.getMessage(), e);
        }
        
        return GsonUtil.toJson(map);
    }

    /**
     * 恢复SyncTask的运行
     */
    @RequestMapping(value = "/sync-task/rerun", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    @ResponseBody
    public Object rerun(HttpSession session, String taskName) {
        Map<String, Object> map = new HashMap<String, Object>();

        try {
            this.syncTaskService.updateStatusAction(taskName, ActionController.RESUME);
            map.put("success", true);
        } catch (IllegalArgumentException e) {
            map.put("success", false);
            map.put("errorMsg", e.getMessage());
        } catch (Exception e) {
            map.put("success", false);
            map.put("errorMsg", e.getMessage());
            LOG.error(e.getMessage(), e);
        }
        return GsonUtil.toJson(map);
    }

    /**
     * 修复SyncTask
     */
    @RequestMapping(value = "/sync-task/resolved", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    @ResponseBody
    public Object resolved(HttpSession session, String taskName) {
        Map<String, Object> map = new HashMap<String, Object>();
        
        try {
            this.syncTaskService.updateStatusAction(taskName, ActionController.RESUME);
            map.put("success", true);
        } catch (IllegalArgumentException e) {
            map.put("success", false);
            map.put("errorMsg", e.getMessage());
        } catch (Exception e) {
            map.put("success", false);
            map.put("errorMsg", e.getMessage());
            LOG.error(e.getMessage(), e);
        }
        return GsonUtil.toJson(map);
    }

}
