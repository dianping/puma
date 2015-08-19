//package com.dianping.puma.admin.web;
//
//import com.dianping.puma.admin.model.deprecated.DstDBInstanceDto;
//import com.dianping.puma.admin.model.deprecated.mapper.DBInstanceMapper;
//import com.dianping.puma.core.util.GsonUtil;
//import com.dianping.puma.core.constant.ActionOperation;
//import com.dianping.puma.biz.entity.old.DstDBInstance;
//import com.dianping.puma.biz.entity.old.SyncTask;
//import com.dianping.puma.biz.service.DstDBInstanceService;
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
//public class DstDBInstanceController {
//
//	private static final Logger LOG = LoggerFactory.getLogger(DstDBInstanceController.class);
//
//	@Autowired
//	DstDBInstanceService dstDBInstanceService;
//
//	@Autowired
//	SyncTaskService syncTaskService;
//
//	@Value("3306")
//	Integer dbPort;
//
//	@RequestMapping(value = { "/dst-db-instance" })
//	public ModelAndView view(HttpServletRequest request, HttpServletResponse response) {
//		Map<String, Object> map = new HashMap<String, Object>();
//		map.put("path", "dst-db-instance");
//		return new ModelAndView("common/main-container", map);
//	}
//
//	@RequestMapping(value = { "/dst-db-instance/list" }, method = RequestMethod.GET, produces = "application/json; charset=utf-8")
//	@ResponseBody
//	public String list(int page, int pageSize) {
//		Map<String, Object> map = new HashMap<String, Object>();
//		long count = dstDBInstanceService.count();
//		List<DstDBInstance> dstDBInstanceEntities = dstDBInstanceService.findByPage(page, pageSize);
//		map.put("count", count);
//		map.put("list", dstDBInstanceEntities);
//		return GsonUtil.toJson(map);
//	}
//
//	@RequestMapping(value = { "/dst-db-instance/create" })
//	public ModelAndView create(HttpServletRequest request, HttpServletResponse response) {
//		Map<String, Object> map = new HashMap<String, Object>();
//		map.put("path", "dst-db-instance");
//		map.put("subPath", "create");
//		return new ModelAndView("common/main-container", map);
//	}
//
//	@RequestMapping(value = { "/dst-db-instance/update/{id}" })
//	public ModelAndView update(@PathVariable long id) {
//		Map<String, Object> map = new HashMap<String, Object>();
//		try {
//			DstDBInstance entity = dstDBInstanceService.find(id);
//			if (entity != null) {
//				List<SyncTask> syncTasks = syncTaskService.findByDstDBInstanceName(entity.getName());
//				if (syncTasks != null && syncTasks.size() != 0) {
//					map.put("lock", true);
//				} else {
//					map.put("lock", false);
//				}
//			}
//
//			// DstDBInstance entity = dstDBInstanceService.find(name);
//			map.put("entity", entity);
//			map.put("path", "dst-db-instance");
//			map.put("subPath", "create");
//		} catch (Exception e) {
//			// @TODO: error page.
//		}
//
//		return new ModelAndView("common/main-container", map);
//	}
//
//	@RequestMapping(value = { "/dst-db-instance/create" }, method = RequestMethod.POST, produces = "application/json; charset=utf-8")
//	@ResponseBody
//	public String createPost(@RequestBody DstDBInstanceDto dstDBInstanceDto) {
//
//		Map<String, Object> map = new HashMap<String, Object>();
//
//		try {
//			DstDBInstance dstDBInstance = dstDBInstanceService.find(dstDBInstanceDto.getName());
//			if (dstDBInstance != null) {
//				throw new Exception("duplicate name.");
//			}
//			dstDBInstance = (DstDBInstance)DBInstanceMapper.convertToDBInstance(dstDBInstanceDto);
//			dstDBInstanceService.create(dstDBInstance);
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
//	@RequestMapping(value = { "/dst-db-instance/update/{id}" }, method = RequestMethod.POST, produces = "application/json; charset=utf-8")
//	@ResponseBody
//	public String updatePost(@PathVariable long id, @RequestBody DstDBInstanceDto dstDBInstanceDto) {
//
//		Map<String, Object> map = new HashMap<String, Object>();
//
//		try {
//			ActionOperation operation;
//
//			DstDBInstance dstDBInstance = dstDBInstanceService.find(id);
//			if (dstDBInstance == null) {
//				operation = ActionOperation.CREATE;
//				dstDBInstance = new DstDBInstance();
//			} else {
//				operation = ActionOperation.UPDATE;
//			}
//			DBInstanceMapper.convertToDBInstance(dstDBInstance, dstDBInstanceDto);
//
//			if (operation == ActionOperation.CREATE) {
//				dstDBInstanceService.create(dstDBInstance);
//			} else {
//				dstDBInstanceService.update(dstDBInstance);
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
//	@RequestMapping(value = { "/dst-db-instance/remove" }, method = RequestMethod.POST, produces = "application/json; charset=utf-8")
//	@ResponseBody
//	public String removePost(String name) {
//		Map<String, Object> map = new HashMap<String, Object>();
//
//		try {
//			List<SyncTask> syncTasks = syncTaskService.findByDstDBInstanceName(name);
//
//			if (syncTasks != null && syncTasks.size() != 0) {
//				throw new Exception("lock");
//			}
//			this.dstDBInstanceService.remove(name);
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
