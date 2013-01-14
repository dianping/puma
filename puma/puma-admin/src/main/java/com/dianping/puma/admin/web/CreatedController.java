package com.dianping.puma.admin.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.dianping.puma.admin.service.SyncConfigService;
import com.dianping.puma.admin.service.SyncTaskActionService;
import com.dianping.puma.admin.service.SyncTaskActionStateService;
import com.dianping.puma.admin.service.SyncTaskService;
import com.dianping.puma.admin.util.GsonUtil;
import com.dianping.puma.core.sync.SyncConfig;
import com.dianping.puma.core.sync.SyncTask;
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
    private SyncConfigService syncConfigService;
    @Autowired
    private SyncTaskService syncTaskService;
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
    public Object state(HttpSession session, String actionId) {
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            ObjectId id = new ObjectId(actionId);
            SyncTaskActionState state = this.syncTaskActionStateService.find(id);
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
    public ModelAndView action(HttpSession session, @PathVariable("id") String actionId) {
        Map<String, Object> map = new HashMap<String, Object>();
        ObjectId id = new ObjectId(actionId);
        SyncTaskAction action = this.syncTaskActionService.find(id);
        SyncTaskActionState state = this.syncTaskActionStateService.find(id);
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
    public Object pause(HttpSession session, String actionId) {
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            ObjectId id = new ObjectId(actionId);
            this.syncTaskActionStateService.updateState(id, State.PAUSE, null);
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
    public Object rerun(HttpSession session, String actionId) {
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            ObjectId id = new ObjectId(actionId);
            this.syncTaskActionStateService.updateState(id, State.PREPARABLE, null);
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
    public Object resolved(HttpSession session, String actionId) {
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            ObjectId id = new ObjectId(actionId);
            this.syncTaskActionStateService.updateState(id, State.RESOLVED, null);
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

    //********************************************************************************************************
    @RequestMapping(value = "/loadSyncConfigs", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    @ResponseBody
    public Object loadSyncConfigs(HttpSession session, HttpServletRequest request, Integer pageNum) {
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            int offset = pageNum == null ? 0 : (pageNum - 1) * PAGESIZE;
            List<SyncConfig> syncConfigs = syncConfigService.findSyncConfigs(offset, PAGESIZE);
            Long totalSyncConfig = syncConfigService.countSyncConfigs();
            List<SyncTask> syncTasks = syncTaskService.findSyncTasksBySyncConfigId(getSyncConfigIds(syncConfigs));
            //            
            //            List<HashMap<String,Object>> resultMaps = new ArrayList<HashMap<String,Object>>();
            //            for(SyncConfig syncConfig:syncConfigs){
            //                for(SyncTask task:syncTasks){
            //                    if(task.getSyncConfigId().equals(syncConfig.getId())){
            //                        HashMap<String,Object> m = new HashMap<String, Object>();
            //                        m.put("", syncConfig.getDest());
            //                    }
            //                }
            //            }

            map.put("syncConfigs", syncConfigs);
            map.put("syncTasks", syncTasks);
            map.put("totalPage", totalSyncConfig / PAGESIZE + (totalSyncConfig % PAGESIZE == 0 ? 0 : 1));
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

    private List<ObjectId> getSyncConfigIds(List<SyncConfig> syncConfigs) {
        List<ObjectId> list = new ArrayList<ObjectId>(syncConfigs.size());
        for (SyncConfig syncConfig : syncConfigs) {
            list.add(syncConfig.getId());
        }
        return list;
    }
}
