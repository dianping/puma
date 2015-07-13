//package com.dianping.puma.admin.web;
//
//import com.dianping.puma.admin.model.PumaTaskDto;
//import com.dianping.puma.admin.model.mapper.PumaTaskMapper;
//import com.dianping.puma.admin.util.GsonUtil;
//import com.dianping.puma.biz.entity.*;
//import com.dianping.puma.biz.entity.old.*;
//import com.dianping.puma.biz.event.entity.PumaTaskOperationEvent;
//import com.dianping.puma.biz.service.PumaServerService;
//import com.dianping.puma.biz.service.PumaTaskService;
//import com.dianping.puma.biz.service.SrcDBInstanceService;
//import com.dianping.puma.biz.service.SyncTaskService;
//import com.dianping.puma.biz.service.impl.TaskStateServiceImpl;
//import com.dianping.puma.core.constant.ActionOperation;
//import com.mongodb.MongoException;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.servlet.ModelAndView;
//
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//@Controller
//public class PumaTaskController {
//
//    private static final Logger LOG = LoggerFactory.getLogger(PumaTaskController.class);
//
//    @Autowired
//    PumaTaskService pumaTaskService;
//
//    @Autowired
//    SrcDBInstanceService srcDBInstanceService;
//
//    @Autowired
//    PumaServerService pumaServerService;
//
//    @Autowired
//    SyncTaskService syncTaskService;
//
//    @Autowired
//    TaskStateServiceImpl TaskStateService;
//
//    @RequestMapping(value = {"/puma-task"})
//    public ModelAndView view() {
//        Map<String, Object> map = new HashMap<String, Object>();
//
//        List<PumaTaskEntity> pumaTaskEntities = pumaTaskService.findAll();
//
//        map.put("entities", pumaTaskEntities);
//        map.put("path", "puma-task");
//        return new ModelAndView("common/main-container", map);
//    }
//
//    @RequestMapping(value = {"/puma-task/list"}, method = RequestMethod.GET, produces = "application/json; charset=utf-8")
//    @ResponseBody
//    public String list(int page, int pageSize) {
//        Map<String, Object> map = new HashMap<String, Object>();
//        long count = pumaTaskService.count();
//        List<PumaTaskEntity> pumaTaskEntities = pumaTaskService.findByPage(page, pageSize);
//        for (PumaTaskEntity pumaTask : pumaTaskEntities) {
//            if (pumaTask.getPumaServerNames() == null || pumaTask.getPumaServerNames().size() == 0) {
//                List<String> serverNames = new ArrayList<String>();
//                serverNames.add(pumaTask.getPumaServerName());
//                pumaTask.setPumaServerNames(serverNames);
//            }
//        }
//        List<TaskStateEntity> TaskStates = new ArrayList<TaskStateEntity>();
//        if (pumaTaskEntities != null) {
//            for (PumaTask pumaTask : pumaTaskEntities) {
//                for (String serverName : pumaTask.getPumaServerNames()) {
//                    TaskStateEntity TaskState = TaskStateService.find(pumaTask.getName(), serverName);
//                    if (TaskState != null) {
//                        TaskStates.add(TaskState);
//                    }
//                }
//            }
//        }
//        map.put("count", count);
//        map.put("list", pumaTaskEntities);
//        map.put("state", TaskStates);
//        return GsonUtil.toJson(map);
//    }
//
//    @RequestMapping(value = {"/puma-task/create"}, method = RequestMethod.GET)
//    public ModelAndView create(HttpServletRequest request, HttpServletResponse response) {
//        Map<String, Object> map = new HashMap<String, Object>();
//        map.put("id", 0);
//        map.put("path", "puma-task");
//        map.put("subPath", "create");
//        return new ModelAndView("common/main-container", map);
//    }
//
//    @RequestMapping(value = {"/puma-task/update/{id}"}, method = RequestMethod.GET)
//    public ModelAndView update(@PathVariable long id) {
//        Map<String, Object> map = new HashMap<String, Object>();
//        try {
//            map.put("id", id);
//            map.put("path", "puma-task");
//            map.put("subPath", "create");
//        } catch (Exception e) {
//            // @TODO: error page.
//        }
//
//        return new ModelAndView("common/main-container", map);
//    }
//
//    @RequestMapping(value = {"/puma-task/find/{id}"}, method = RequestMethod.GET, produces = "application/json; charset=utf-8")
//    @ResponseBody
//    public String find(@PathVariable long id) {
//        Map<String, Object> map = new HashMap<String, Object>();
//
//        try {
//            List<SrcDBInstance> srcDBInstanceEntities = srcDBInstanceService.findAll();
//            List<PumaServer> pumaServerEntities = pumaServerService.findAll();
//            map.put("srcDBInstanceEntities", srcDBInstanceEntities);
//            map.put("pumaServerEntities", pumaServerEntities);
//            PumaTask pumaTask = null;
//            if (id > 0) {
//                pumaTask = pumaTaskService.find(id);
//            }
//            map.put("entity", PumaTaskMapper.convertToPumaTaskDto(pumaTask));
//        } catch (Exception e) {
//            // @TODO: error page.
//        }
//
//        return GsonUtil.toJson(map);
//    }
//
//    @RequestMapping(value = {"/puma-task/create"}, method = RequestMethod.POST, produces = "application/json; charset=utf-8")
//    @ResponseBody
//    public String createPost(@RequestBody PumaTaskDto entity) {
//        Map<String, Object> map = new HashMap<String, Object>();
//        try {
//            ActionOperation operation = null;
//            PumaTaskEntity pumaTask = pumaTaskService.find(entity.getName());
//            if (pumaTask != null) {
//                throw new Exception("duplicate name.");
//            }
//            pumaTask = PumaTaskMapper.convertToPumaTask(entity);
//            pumaTaskService.create(pumaTask);
//            map.put("success", true);
//        } catch (MongoException e) {
//            map.put("error", "storage");
//            map.put("success", false);
//        } catch (Exception e) {
//            map.put("error", e.getMessage());
//            map.put("success", false);
//        }
//        return GsonUtil.toJson(map);
//    }
//
//    @SuppressWarnings("unchecked")
//    @RequestMapping(value = {"/puma-task/update/{id}"}, method = RequestMethod.POST, produces = "application/json; charset=utf-8")
//    @ResponseBody
//    public String updatePost(@PathVariable long id, @RequestBody PumaTaskDto entity) {
//
//        Map<String, Object> map = new HashMap<String, Object>();
//
//        try {
//            ActionOperation operation = null;
//            PumaTaskOperationEvent event = new PumaTaskOperationEvent();
//            PumaTask pumaTask = pumaTaskService.find(id);
//            if (pumaTask == null) {
//                operation = ActionOperation.CREATE;
//            } else {
//                operation = ActionOperation.UPDATE;
//                event.setOriPumaTask(pumaTask);
//            }
//            pumaTask = PumaTaskMapper.convertToPumaTask(entity);
//            pumaTask.setId(id);
//            if (operation == ActionOperation.CREATE) {
//                pumaTaskService.create(pumaTask);
//            } else {
//                pumaTask.setId(id);
//                pumaTaskService.update(pumaTask);
//            }
//
//            map.put("success", true);
//        } catch (MongoException e) {
//            map.put("error", "storage");
//            map.put("success", false);
//        } catch (Exception e) {
//            map.put("error", e.getMessage());
//            map.put("success", false);
//        }
//        return GsonUtil.toJson(map);
//    }
//
//    @RequestMapping(value = {"/puma-task/remove"}, method = RequestMethod.POST, produces = "application/json; charset=utf-8")
//    @ResponseBody
//    public String removePost(String taskName, String serverName) {
//        Map<String, Object> map = new HashMap<String, Object>();
//
//        try {
//            PumaTask pumaTask = pumaTaskService.find(taskName);
//            if (pumaTask == null) {
//                throw new NullPointerException();
//            }
//            if (pumaTask.getPumaServerNames() == null || pumaTask.getPumaServerNames().size() == 0) {
//                List<SyncTask> syncTasks = syncTaskService.findByPumaTaskName(pumaTask.getName());
//                if (syncTasks != null && syncTasks.size() > 0) {
//                    throw new IllegalArgumentException();
//                }
//                this.pumaTaskService.remove(taskName);
//            } else {
//                if (pumaTask.getPumaServerNames().contains(serverName)) {
//                    if (pumaTask.getPumaServerNames().size() == 1) {
//                        this.pumaTaskService.remove(taskName);
//                    } else {
//                        pumaTask.getPumaServerNames().remove(serverName);
//                        pumaTaskService.update(pumaTask);
//                    }
//                }
//            }
//
//            map.put("success", true);
//        } catch (MongoException e) {
//            map.put("error", "storage");
//            map.put("success", false);
//        } catch (Exception e) {
//            map.put("error", e.getMessage());
//            map.put("success", false);
//        }
//
//        map.put("success", true);
//
//        return GsonUtil.toJson(map);
//    }
//
//    @RequestMapping(value = {"/puma-task/refresh"}, method = RequestMethod.POST, produces = "application/json; charset=utf-8")
//    @ResponseBody
//    public String refreshPost(String taskName, String serverName) {
//        Map<String, Object> map = new HashMap<String, Object>();
//
//        try {
//            map.put("success", true);
//        } catch (MongoException e) {
//            map.put("error", "storage");
//            map.put("success", false);
//        } catch (Exception e) {
//            map.put("error", e.getMessage());
//            map.put("success", false);
//        }
//
//        return GsonUtil.toJson(map);
//    }
//
//    @RequestMapping(value = {"/puma-task/resume"}, method = RequestMethod.POST, produces = "application/json; charset=utf-8")
//    @ResponseBody
//    public String resumePost(String taskName, String serverName) {
//        Map<String, Object> map = new HashMap<String, Object>();
//
//        try {
//            PumaTask pumaTask = pumaTaskService.find(taskName);
//            if (pumaTask == null) {
//                throw new Exception("Puma task not found.");
//            }
//
//            map.put("success", true);
//        } catch (MongoException e) {
//            map.put("error", "storage");
//            map.put("success", false);
//        } catch (Exception e) {
//            map.put("error", e.getMessage());
//            map.put("success", false);
//        }
//
//        return GsonUtil.toJson(map);
//    }
//
//    @RequestMapping(value = {"/puma-task/pause"}, method = RequestMethod.POST, produces = "application/json; charset=utf-8")
//    @ResponseBody
//    public String pausePost(String taskName, String serverName) {
//        Map<String, Object> map = new HashMap<String, Object>();
//
//        try {
//            PumaTask pumaTask = pumaTaskService.find(taskName);
//            if (pumaTask == null) {
//                throw new Exception("Puma task not found.");
//            }
//
//            map.put("success", true);
//        } catch (MongoException e) {
//            map.put("error", "storage");
//            map.put("success", false);
//        } catch (Exception e) {
//            map.put("error", e.getMessage());
//            map.put("success", false);
//        }
//
//        return GsonUtil.toJson(map);
//    }
//}
