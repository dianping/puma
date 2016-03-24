package com.dianping.puma.portal.web;//package com.dianping.puma.admin.web;
//
//import com.dianping.puma.syncserver.biz.converter.Converter;
//import com.dianping.puma.syncserver.biz.dto.SyncServerDto;
//import com.dianping.puma.syncserver.biz.dto.SyncTaskDto;
//import com.dianping.puma.syncserver.common.model.SyncServer;
//import com.dianping.puma.syncserver.common.model.SyncTask;
//import com.dianping.puma.syncserver.common.service.IdService;
//import com.dianping.puma.syncserver.common.service.SyncServerService;
//import com.dianping.puma.syncserver.common.service.SyncTaskService;
//import com.google.gson.reflect.TypeToken;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpStatus;
//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@Controller
//@RequestMapping(value = {"/puma-sync"})
//public class PumaSyncController extends BasicController {
//
//    @Autowired
//    Converter converter;
//
//    @Autowired
//    IdService idService;
//
//    @Autowired
//    SyncTaskService syncTaskService;
//
//    @Autowired
//    SyncServerService syncServerService;
//
//    @RequestMapping(method = RequestMethod.POST, value = {"/create"})
//    @ResponseStatus(value = HttpStatus.OK)
//    public void create(@RequestBody SyncTaskDto syncTaskDto) {
//        SyncTask syncTask = converter.convert(syncTaskDto, SyncTask.class);
//        syncTaskService.create(syncTask);
//    }
//
//    @RequestMapping(method = RequestMethod.DELETE, value = {"/remove"})
//    @ResponseStatus(value = HttpStatus.OK)
//    public void remove(@RequestParam int taskId) {
//        syncTaskService.remove(taskId);
//    }
//
//    @RequestMapping(method = RequestMethod.GET, value = {"/servers"})
//    @ResponseBody
//    public List<SyncServerDto> findServers() {
//        List<SyncServer> syncServers = syncServerService.findAll();
//        return converter.convert(syncServers, new TypeToken<List<SyncServerDto>>(){}.getType());
//    }
//
//    @RequestMapping(method = RequestMethod.GET, value = {"/tasks"})
//    @ResponseBody
//    public List<SyncTaskDto> findTasks() {
//        List<SyncTask> syncTasks = syncTaskService.findAllTasks();
//        return converter.convert(syncTasks, new TypeToken<List<SyncTaskDto>>(){}.getType());
//    }
//}
