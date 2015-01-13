package com.dianping.puma.admin.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

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
import com.dianping.puma.admin.service.SyncTaskService;
import com.dianping.puma.admin.util.GsonUtil;
import com.dianping.puma.core.sync.model.task.SyncTask;
import com.dianping.puma.core.sync.model.task.SyncTaskStatusAction;
import com.dianping.puma.core.sync.model.task.Type;
import com.dianping.puma.core.sync.model.taskexecutor.TaskExecutorStatus;

/**
 * 查看已经创建的所有任务
 */
@Controller
public class CreatedController {
    private static final Logger LOG = LoggerFactory.getLogger(CreatedController.class);
    @Autowired
    private SyncTaskService syncTaskService;
    @Autowired
    private SystemStatusContainer systemStatusContainer;

    private static final int PAGESIZE = 30;

    @RequestMapping(value = { "/created" })
    public ModelAndView created(HttpServletRequest request, HttpServletResponse response) {
        return created0(request, response, 1);
    }

    @RequestMapping(value = { "/created/{pageNum}" })
    public ModelAndView created0(HttpServletRequest request, HttpServletResponse response, @PathVariable("pageNum") Integer pageNum) {
        Map<String, Object> map = new HashMap<String, Object>();
        //        System.out.println(syncConfigService.find());
        int offset = pageNum == null ? 0 : (pageNum - 1) * PAGESIZE;
        List<SyncTask> syncTasks = syncTaskService.find(offset, PAGESIZE);
        map.put("syncTasks", syncTasks);
        map.put("createdActive", "active");
        map.put("subPath", "list");
        map.put("path", "created");
        return new ModelAndView("main/container", map);
    }

    /**
     * 查询SyncTask状态
     */
    @RequestMapping(value = "/created/status", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    @ResponseBody
    public Object status(HttpSession session, Long taskId) {
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            TaskExecutorStatus syncStatus = systemStatusContainer.getStatus(Type.SYNC, taskId);
            //binlog信息，从数据库查询binlog位置即可，不需要从SyncServer实时发过来的status中的binlog获取
            //binlogInfoOfIOThread则是从status中获取
            SyncTask syncTask = this.syncTaskService.find(taskId);
            syncStatus.setBinlogInfo(syncTask.getBinlogInfo());

            map.put("status", syncStatus);
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
    @RequestMapping(value = "/created/task/{id}", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public ModelAndView task(HttpSession session, @PathVariable("id") Long taskId) {
        Map<String, Object> map = new HashMap<String, Object>();
        SyncTask syncTask = this.syncTaskService.find(taskId);
        TaskExecutorStatus syncStatus = systemStatusContainer.getStatus(Type.SYNC, taskId);
        map.put("task", syncTask);
        map.put("status", syncStatus);
        map.put("createdActive", "active");
        map.put("subPath", "task");
        map.put("path", "created");
        return new ModelAndView("main/container", map);
    }

    /**
     * 暂停SyncTask状态
     */
    @RequestMapping(value = "/created/pause", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    @ResponseBody
    public Object pause(HttpSession session, Long taskId) {
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            this.syncTaskService.updateStatusAction(taskId, SyncTaskStatusAction.PAUSE);

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
    @RequestMapping(value = "/created/rerun", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    @ResponseBody
    public Object rerun(HttpSession session, Long taskId) {
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            this.syncTaskService.updateStatusAction(taskId, SyncTaskStatusAction.RESTART);
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
    @RequestMapping(value = "/created/resolved", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    @ResponseBody
    public Object resolved(HttpSession session, Long taskId) {
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            this.syncTaskService.updateStatusAction(taskId, SyncTaskStatusAction.RESTART);
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
