package com.dianping.puma.admin.web;

import com.dianping.puma.admin.config.Config;
import com.dianping.puma.admin.model.SyncTaskDto;
import com.dianping.puma.admin.model.mapper.ErrorListMapper;
import com.dianping.puma.admin.model.mapper.SyncTaskMapper;
import com.dianping.puma.admin.util.GsonUtil;
import com.dianping.puma.biz.entity.*;
import com.dianping.puma.biz.entity.old.*;
import com.dianping.puma.biz.service.DstDBInstanceService;
import com.dianping.puma.biz.service.PumaTaskService;
import com.dianping.puma.biz.service.SyncServerService;
import com.dianping.puma.biz.service.SyncTaskService;
import com.dianping.puma.biz.service.impl.TaskStateServiceImpl;
import com.dianping.puma.core.constant.ActionController;
import com.dianping.puma.core.constant.ActionOperation;
import com.mongodb.MongoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class SyncTaskController {

    private static final Logger LOG = LoggerFactory.getLogger(SyncTaskController.class);

    @Autowired
    private SyncTaskService syncTaskService;

    @Autowired
    private PumaTaskService pumaTaskService;

    @Autowired
    private DstDBInstanceService dstDBInstanceService;

    @Autowired
    private SyncServerService syncServerService;

    @Autowired
    TaskStateServiceImpl syncTaskStateService;

    private static final int PAGESIZE = 30;

    @RequestMapping(value = {"/sync-task"})
    public ModelAndView view(HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("path", "sync-task");
        return new ModelAndView("common/main-container", map);
    }

    @RequestMapping(value = {"/sync-task/{pageNum}"})
    public ModelAndView created0(HttpServletRequest request, HttpServletResponse response,
                                 @PathVariable("pageNum") Integer pageNum) {
        Map<String, Object> map = new HashMap<String, Object>();
        int offset = pageNum == null ? 0 : (pageNum - 1) * PAGESIZE;
        List<SyncTask> syncTasks = syncTaskService.find(offset, PAGESIZE);
        map.put("syncTasks", syncTasks);
        map.put("createdActive", "active");
        map.put("subPath", "main");
        map.put("path", "sync-task");
        return new ModelAndView("main/container", map);
    }

    @RequestMapping(value = {"/sync-task/list"}, method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    @ResponseBody
    public String list(int page, int pageSize) {
        Map<String, Object> map = new HashMap<String, Object>();
        long count = syncTaskService.count();
        List<SyncTask> syncTasks = syncTaskService.findByPage(page, pageSize);
        List<TaskState> TaskStates = new ArrayList<TaskState>();
        if (syncTasks != null) {
            for (SyncTask syncTask : syncTasks) {
                TaskStates.addAll(syncTaskStateService.find(syncTask.getName()));
            }
        }
        map.put("count", count);
        map.put("list", syncTasks);
        map.put("state", TaskStates);
        return GsonUtil.toJson(map);
    }

    @RequestMapping(value = {"/sync-task/remove"}, method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    @ResponseBody
    public String removePost(String name) {
        Map<String, Object> map = new HashMap<String, Object>();

        try {
            // Actually name here.

            SyncTask syncTask = syncTaskService.find(name);

            syncTaskService.remove(name);

            map.put("success", true);
        } catch (MongoException e) {
            map.put("error", "storage");
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
            TaskState TaskState = syncTaskStateService.find(taskName).get(0);
            // binlog信息，从数据库查询binlog位置即可，不需要从SyncServer实时发过来的status中的binlog获取
            // binlogInfoOfIOThread则是从status中获取
            SyncTask syncTask = this.syncTaskService.find(taskName);
            TaskState.setBinlogInfo(syncTask.getBinlogInfo());

            map.put("status", TaskState);
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
    @RequestMapping(value = "/sync-task/detail/{id}", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public ModelAndView task(HttpSession session, @PathVariable long id) {
        Map<String, Object> map = new HashMap<String, Object>();
        SyncTask syncTask = this.syncTaskService.find(id);
        if (syncTask != null) {
            List<TaskState> TaskState = syncTaskStateService.find(syncTask.getName());
            map.put("syncTask", syncTask);
            map.put("TaskState", TaskState.get(0));
            map.put("createdActive", "active");
            map.put("subPath", "detail");
            map.put("path", "sync-task");
        }
        return new ModelAndView("main/container", map);
    }

    /**
     * 暂停SyncTask状态
     */
    @RequestMapping(value = "/sync-task/pause", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    @ResponseBody
    public Object pause(HttpSession session, String name) {
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            SyncTask syncTask = syncTaskService.find(name);
            this.syncTaskService.updateStatusAction(name, ActionController.PAUSE);
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
    @RequestMapping(value = "/sync-task/resume", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    @ResponseBody
    public Object rerun(HttpSession session, String name) {
        Map<String, Object> map = new HashMap<String, Object>();

        try {
            SyncTask syncTask = syncTaskService.find(name);
            this.syncTaskService.updateStatusAction(name, ActionController.RESUME);
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

    @RequestMapping(value = {"/sync-task/create"}, method = RequestMethod.GET)
    public ModelAndView create(HttpSession session) {
        Map<String, Object> map = new HashMap<String, Object>();
        // 查询MysqlConfig
        map.put("id", 0);
        map.put("path", "sync-task");
        map.put("subPath", "create");
        return new ModelAndView("common/main-container", map);
    }

    @RequestMapping(value = {"/sync-task/update/{id}"}, method = RequestMethod.GET)
    public ModelAndView update(@PathVariable long id) {
        Map<String, Object> map = new HashMap<String, Object>();
        // 查询MysqlConfig
        map.put("id", id);
        map.put("path", "sync-task");
        map.put("subPath", "create");
        return new ModelAndView("common/main-container", map);
    }

    @RequestMapping(value = {"/sync-task/find/{id}"}, method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    @ResponseBody
    public String find(@PathVariable long id) {
        Map<String, Object> map = new HashMap<String, Object>();
        List<PumaTask> pumaTasks = pumaTaskService.findAll();
        List<DstDBInstance> dstDBInstances = dstDBInstanceService.findAll();
        List<SyncServer> syncServers = syncServerService.findAll();

        map.put("pumaTasks", pumaTasks);
        map.put("dstDBInstances", dstDBInstances);
        map.put("syncServers", syncServers);
        map.put("errorSet", ErrorListMapper.convertToErrorSetDto(Config.getInstance().getErrorCodeHandlerMap()));
        SyncTask syncTask = null;
        if (id > 0) {
            syncTask = syncTaskService.find(id);
        }
        map.put("entity", SyncTaskMapper.convertToSyncTaskDto(syncTask));
        return GsonUtil.toJson(map);
    }

    @RequestMapping(value = {"/sync-task/create"}, method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    @ResponseBody
    public String createPost(@RequestBody SyncTaskDto syncTaskDto) {
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            SyncTask syncTask = syncTaskService.find(syncTaskDto.getName());
            if (syncTask != null) {
                throw new Exception("duplicate name.");
            }
            syncTask = SyncTaskMapper.convertToSyncTask(syncTaskDto);
            syncTask.setController(ActionController.START);
            syncTaskService.create(syncTask);
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

    @RequestMapping(value = {"/sync-task/update/{id}"}, method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    @ResponseBody
    public String updatePost(@PathVariable long id, @RequestBody SyncTaskDto entity) {
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            SyncTask syncTask = syncTaskService.find(id);
            ActionOperation operation = null;
            if (syncTask == null) {
                operation = ActionOperation.CREATE;
            } else {
                operation = ActionOperation.UPDATE;
            }
            syncTask = SyncTaskMapper.convertToSyncTask(entity);
            if (operation == ActionOperation.CREATE) {
                syncTaskService.create(syncTask);
            } else {
                syncTask.setId(id);
                syncTaskService.update(syncTask);
            }
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

    @RequestMapping(value = {"/sync-task/refresh"}, method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    @ResponseBody
    public String refreshPost(String name) {
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            List<TaskState> TaskState = syncTaskStateService.find(name);
            map.put("state", TaskState.get(0));
            map.put("success", true);
        } catch (MongoException e) {
            map.put("error", "storage");
            map.put("success", false);
        } catch (Exception e) {
            map.put("error", e.getMessage());
            map.put("success", false);
        }

        return GsonUtil.toJson(map);
    }
}
