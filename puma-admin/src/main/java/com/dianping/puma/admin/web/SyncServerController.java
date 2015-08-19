//package com.dianping.puma.admin.web;
//
//import com.dianping.puma.admin.model.SyncServerDto;
//import com.dianping.puma.admin.model.deprecated.mapper.SyncServerMapper;
//import com.dianping.puma.core.util.GsonUtil;
//import com.dianping.puma.core.constant.ActionOperation;
//import com.dianping.puma.biz.entity.old.SyncServer;
//import com.dianping.puma.biz.entity.old.SyncTask;
//import com.dianping.puma.biz.service.SyncServerService;
//import com.dianping.puma.biz.service.SyncTaskService;
//import com.mongodb.MongoException;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestMethod;
//import org.springframework.web.bind.annotation.ResponseBody;
//import org.springframework.web.servlet.ModelAndView;
//
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//@Controller
//public class SyncServerController {
//
//	private static final Logger LOG = LoggerFactory.getLogger(SyncServerController.class);
//
//	@Autowired
//	SyncServerService syncServerService;
//
//	@Autowired
//	SyncTaskService syncTaskService;
//
//	@Value("8080")
//	Integer serverPort;
//
//	@RequestMapping(value = { "/sync-server" })
//	public ModelAndView view(HttpServletRequest request, HttpServletResponse response) {
//		Map<String, Object> map = new HashMap<String, Object>();
//		map.put("path", "sync-server");
//		return new ModelAndView("common/main-container", map);
//	}
//
//	@RequestMapping(value = {
//			"/sync-server/list" }, method = RequestMethod.GET, produces = "application/json; charset=utf-8")
//	@ResponseBody
//	public String list(int page, int pageSize) {
//		Map<String, Object> map = new HashMap<String, Object>();
//		long count = syncServerService.count();
//		List<SyncServer> syncServerEntities = syncServerService.findByPage(page, pageSize);
//		map.put("count", count);
//		map.put("list", syncServerEntities);
//		return GsonUtil.toJson(map);
//	}
//
//	@RequestMapping(value = { "/sync-server/create" })
//	public ModelAndView create(HttpServletRequest request, HttpServletResponse response) {
//		Map<String, Object> map = new HashMap<String, Object>();
//		map.put("path", "sync-server");
//		map.put("subPath", "create");
//		return new ModelAndView("common/main-container", map);
//	}
//
//	@RequestMapping(value = { "/sync-server/update/{id}" })
//	public ModelAndView update(@PathVariable long id) {
//		Map<String, Object> map = new HashMap<String, Object>();
//
//		try {
//			SyncServer entity = syncServerService.find(id);
//			if (entity != null) {
//				List<SyncTask> syncTasks = syncTaskService.findBySyncServerName(entity.getName());
//
//				if (syncTasks != null && syncTasks.size() != 0) {
//					map.put("lock", true);
//				} else {
//					map.put("lock", false);
//				}
//			}
//			map.put("entity", entity);
//			map.put("path", "sync-server");
//			map.put("subPath", "create");
//		} catch (Exception e) {
//			// @TODO: error page.
//		}
//
//		return new ModelAndView("common/main-container", map);
//	}
//
//	@RequestMapping(value = {
//			"/sync-server/create" }, method = RequestMethod.POST, produces = "application/json; charset=utf-8")
//	@ResponseBody
//	public String createPost(@RequestBody SyncServerDto syncServerDto) {
//		Map<String, Object> map = new HashMap<String, Object>();
//
//		try {
//
//			SyncServer syncServer = syncServerService.find(syncServerDto.getName());
//			if (syncServer != null) {
//				throw new Exception("duplicate name.");
//			}
//			syncServer = SyncServerMapper.convertToSyncServer(syncServerDto);
//			syncServerService.create(syncServer);
//			map.put("success", true);
//		} catch (MongoException e) {
//			map.put("error", "storage");
//			map.put("success", false);
//		} catch (Exception e) {
//			map.put("error", e.getMessage());
//			map.put("success", false);
//		}
//
//		return GsonUtil.toJson(map);
//	}
//
//	@RequestMapping(value = {
//			"/sync-server/update/{id}" }, method = RequestMethod.POST, produces = "application/json; charset=utf-8")
//	@ResponseBody
//	public String updatePost(@PathVariable long id, @RequestBody SyncServerDto syncServerDto) {
//		Map<String, Object> map = new HashMap<String, Object>();
//
//		try {
//			ActionOperation operation;
//			SyncServer syncServer = syncServerService.find(id);
//			if (syncServer == null) {
//				syncServer = new SyncServer();
//				operation = ActionOperation.CREATE;
//			} else {
//				operation = ActionOperation.UPDATE;
//			}
//
//			SyncServerMapper.convertToSyncServer(syncServer, syncServerDto);
//			if (operation == ActionOperation.CREATE) {
//				syncServerService.create(syncServer);
//			}else{
//				syncServer.setId(id);
//				syncServerService.update(syncServer);
//			}
//
//			map.put("success", true);
//		} catch (MongoException e) {
//			map.put("error", "storage");
//			map.put("success", false);
//		} catch (Exception e) {
//			map.put("error", e.getMessage());
//			map.put("success", false);
//		}
//
//		return GsonUtil.toJson(map);
//	}
//
//	@RequestMapping(value = {
//			"/sync-server/remove" }, method = RequestMethod.POST, produces = "application/json; charset=utf-8")
//	@ResponseBody
//	public String removePost(String name) {
//		Map<String, Object> map = new HashMap<String, Object>();
//
//		try {
//			List<SyncTask> pumaTasks = syncTaskService.findBySyncServerName(name);
//
//			if (pumaTasks != null && pumaTasks.size() != 0) {
//				throw new Exception("lock");
//			}
//
//			syncServerService.remove(name);
//			map.put("success", true);
//		} catch (MongoException e) {
//			map.put("error", "storage");
//			map.put("success", false);
//		} catch (Exception e) {
//			map.put("error", e.getMessage());
//			map.put("success", false);
//		}
//
//		return GsonUtil.toJson(map);
//	}
//}
