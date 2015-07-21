//package com.dianping.puma.admin.web;
//
//import com.dianping.puma.admin.model.SrcDbDto;
//import com.dianping.puma.admin.model.mapper.DBInstanceMapper;
//import com.dianping.puma.core.util.GsonUtil;
//import com.dianping.puma.biz.entity.old.PumaTask;
//import com.dianping.puma.biz.entity.old.SrcDBInstance;
//import com.dianping.puma.biz.service.PumaTaskService;
//import com.dianping.puma.biz.service.SrcDBInstanceService;
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
//public class SrcDbController {
//
//	private static final Logger LOG = LoggerFactory.getLogger(SrcDbController.class);
//
//	@Autowired
//	SrcDBInstanceService srcDBInstanceService;
//
//	@Autowired
//	PumaTaskService pumaTaskService;
//
//	@Value("3306")
//	Integer dbPort;
//
//	@RequestMapping(value = { "/src-db-instance" })
//	public ModelAndView view(HttpServletRequest request, HttpServletResponse response) {
//		Map<String, Object> map = new HashMap<String, Object>();
//		map.put("path", "src-db-instance");
//		return new ModelAndView("common/main-container", map);
//	}
//
//	@RequestMapping(value = { "/src-db-instance/list" }, method = RequestMethod.GET, produces = "application/json; charset=utf-8")
//	@ResponseBody
//	public String list(int page, int pageSize) {
//		Map<String, Object> map = new HashMap<String, Object>();
//		long count = srcDBInstanceService.count();
//		List<SrcDBInstance> srcDBInstanceEntities = srcDBInstanceService.findByPage(page, pageSize);
//		map.put("count", count);
//		map.put("list", srcDBInstanceEntities);
//		return GsonUtil.toJson(map);
//	}
//
//	@RequestMapping(value = { "/src-db-instance/create" })
//	public ModelAndView create(HttpServletRequest request, HttpServletResponse response) {
//		Map<String, Object> map = new HashMap<String, Object>();
//		map.put("path", "src-db-instance");
//		map.put("subPath", "create");
//		return new ModelAndView("common/main-container", map);
//	}
//
//	@RequestMapping(value = { "/src-db-instance/update/{id}" }, method = RequestMethod.GET)
//	public ModelAndView update(@PathVariable long id) {
//		Map<String, Object> map = new HashMap<String, Object>();
//		try {
//			SrcDBInstance entity = srcDBInstanceService.find(id);
//			if (entity != null) {
//				List<PumaTask> pumaTasks = pumaTaskService.findBySrcDBInstanceName(entity.getName());
//				if (pumaTasks != null && pumaTasks.size() != 0) {
//					map.put("lock", true);
//				} else {
//					map.put("lock", false);
//				}
//			}
//			// SrcDBInstance entity = srcDBInstanceService.find(name);
//			map.put("entity", entity);
//			map.put("path", "src-db-instance");
//			map.put("subPath", "create");
//
//		} catch (Exception e) {
//			// @TODO: error page.
//		}
//
//		return new ModelAndView("common/main-container", map);
//	}
//
//	@RequestMapping(value = { "/src-db-instance/create" }, method = RequestMethod.POST, produces = "application/json; charset=utf-8")
//	@ResponseBody
//	public String createPost(@RequestBody SrcDbDto srcDbDto) {
//
//		Map<String, Object> map = new HashMap<String, Object>();
//
//		try {
//			SrcDBInstance srcDBInstance = srcDBInstanceService.find(srcDbDto.getName());
//
//			if (srcDBInstance != null) {
//				throw new Exception("duplicate name.");
//			}
//			//srcDBInstance = (SrcDBInstance)DBInstanceMapper.convertToDBInstance(srcDbModel);
//			srcDBInstanceService.create(srcDBInstance);
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
//	@RequestMapping(value = { "/src-db-instance/update/{id}" }, method = RequestMethod.POST, produces = "application/json; charset=utf-8")
//	@ResponseBody
//	public String updatePost(@PathVariable long id, @RequestBody SrcDbDto srcDbDto) {
//		Map<String, Object> map = new HashMap<String, Object>();
//
//		try {
//			boolean create;
//
//			SrcDBInstance srcDBInstance = srcDBInstanceService.find(id);
//
//			if (srcDBInstance == null) {
//				create = true;
//				srcDBInstance = new SrcDBInstance();
//			} else {
//				create = false;
//			}
//			//DBInstanceMapper.convertToDBInstance(srcDBInstance, srcDbDto);
//
//			if (create) {
//				srcDBInstanceService.create(srcDBInstance);
//			} else {
//				srcDBInstanceService.update(srcDBInstance);
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
//	@RequestMapping(value = { "/src-db-instance/remove" }, method = RequestMethod.POST, produces = "application/json; charset=utf-8")
//	@ResponseBody
//	public String removePost(String name) {
//		Map<String, Object> map = new HashMap<String, Object>();
//
//		try {
//			List<PumaTask> pumaTasks = pumaTaskService.findBySrcDBInstanceName(name);
//
//			if (pumaTasks != null && pumaTasks.size() != 0) {
//				throw new Exception("lock");
//			}
//
//			this.srcDBInstanceService.remove(name);
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
