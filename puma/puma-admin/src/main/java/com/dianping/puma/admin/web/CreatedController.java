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

import com.dianping.puma.admin.service.SyncTaskActionService;
import com.dianping.puma.admin.service.SyncTaskActionStateService;
import com.dianping.puma.admin.util.GsonUtil;
import com.dianping.puma.core.sync.model.action.ActionState.State;
import com.dianping.puma.core.sync.model.action.SyncTaskAction;
import com.dianping.puma.core.sync.model.action.SyncTaskActionState;

/**
 * 查看已经创建的所有任务
 */
@Controller
public class CreatedController {
    private static final Logger LOG = LoggerFactory.getLogger(CreatedController.class);
    @Autowired
    private SyncTaskActionService syncTaskActionService;
    @Autowired
    private SyncTaskActionStateService syncTaskActionStateService;

    private static final String errorMsg = "对不起，出了一点错误，请刷新页面试试。";
    private static final int PAGESIZE = 8;

    @RequestMapping(value = { "/created" })
    public ModelAndView created(HttpServletRequest request, HttpServletResponse response) {
        return created0(request, response, 1);
    }

    @RequestMapping(value = { "/created/{pageNum}" })
    public ModelAndView created0(HttpServletRequest request, HttpServletResponse response, @PathVariable("pageNum") Integer pageNum) {
        Map<String, Object> map = new HashMap<String, Object>();
        //        System.out.println(syncConfigService.find());
        int offset = pageNum == null ? 0 : (pageNum - 1) * PAGESIZE;
        List<SyncTaskAction> syncTaskActions = syncTaskActionService.find(offset, PAGESIZE);
        map.put("syncTaskActions", syncTaskActions);
        map.put("createdActive", "active");
        map.put("subPath", "view");
        map.put("path", "created");
        return new ModelAndView("main/container", map);
    }

    /**
     * 查询SyncTaskActionState
     */
    @RequestMapping(value = "/created/state", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    @ResponseBody
    public Object state(HttpSession session, Long actionId) {
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            SyncTaskActionState state = this.syncTaskActionStateService.find(actionId);
            map.put("stateLastUpdateTime", state);
            map.put("state", state);
            map.put("success", true);
        } catch (IllegalArgumentException e) {
            map.put("success", false);
            map.put("errorMsg", e.getMessage());
        } catch (Exception e) {
            map.put("success", false);
            map.put("errorMsg", errorMsg);
            LOG.error(e.getMessage(), e);
        }
        return GsonUtil.toJson(map);
    }

    /**
     * 查询SyncTaskActionState
     */
    @RequestMapping(value = "/created/action/{id}", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public ModelAndView action(HttpSession session, @PathVariable("id") Long actionId) {
        Map<String, Object> map = new HashMap<String, Object>();
        SyncTaskAction action = this.syncTaskActionService.find(actionId);
        SyncTaskActionState state = this.syncTaskActionStateService.find(actionId);
        map.put("action", action);
        map.put("state", state);
        map.put("createdActive", "active");
        map.put("subPath", "action");
        map.put("path", "created");
        return new ModelAndView("main/container", map);
    }

    /**
     * 暂停SyncTaskActionState
     */
    @RequestMapping(value = "/created/pause", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    @ResponseBody
    public Object pause(HttpSession session, Long actionId) {
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            this.syncTaskActionStateService.updateState(actionId, State.PAUSE, null);
            map.put("success", true);
        } catch (IllegalArgumentException e) {
            map.put("success", false);
            map.put("errorMsg", e.getMessage());
        } catch (Exception e) {
            map.put("success", false);
            map.put("errorMsg", errorMsg);
            LOG.error(e.getMessage(), e);
        }
        return GsonUtil.toJson(map);
    }

    /**
     * 恢复SyncTaskActionState的运行
     */
    @RequestMapping(value = "/created/rerun", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    @ResponseBody
    public Object rerun(HttpSession session, Long actionId) {
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            this.syncTaskActionStateService.updateState(actionId, State.PREPARABLE, null);
            map.put("success", true);
        } catch (IllegalArgumentException e) {
            map.put("success", false);
            map.put("errorMsg", e.getMessage());
        } catch (Exception e) {
            map.put("success", false);
            map.put("errorMsg", errorMsg);
            LOG.error(e.getMessage(), e);
        }
        return GsonUtil.toJson(map);
    }

    /**
     * 修复SyncTaskActionState
     */
    @RequestMapping(value = "/created/resolved", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    @ResponseBody
    public Object resolved(HttpSession session, Long actionId) {
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            this.syncTaskActionStateService.updateState(actionId, State.RESOLVED, null);
            map.put("success", true);
        } catch (IllegalArgumentException e) {
            map.put("success", false);
            map.put("errorMsg", e.getMessage());
        } catch (Exception e) {
            map.put("success", false);
            map.put("errorMsg", errorMsg);
            LOG.error(e.getMessage(), e);
        }
        return GsonUtil.toJson(map);
    }

}
