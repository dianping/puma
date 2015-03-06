package com.dianping.puma.admin.web;

import com.dianping.puma.admin.util.GsonUtil;
import com.dianping.puma.core.entity.DstDBInstance;
import com.dianping.puma.core.entity.PumaTask;
import com.dianping.puma.core.service.DstDBInstanceService;
import com.dianping.puma.core.service.PumaTaskService;
import com.mongodb.MongoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class DstDBInstanceController {

	private static final Logger LOG = LoggerFactory.getLogger(DstDBInstanceController.class);

	@Autowired
	DstDBInstanceService dstDBInstanceService;

	@Autowired
	PumaTaskService pumaTaskService;

	@RequestMapping(value = { "/dst-db-instance" })
	public ModelAndView view(HttpServletRequest request, HttpServletResponse response) {
		Map<String, Object> map = new HashMap<String, Object>();

		List<DstDBInstance> dstDBInstanceEntities = dstDBInstanceService.findAll();

		map.put("entities", dstDBInstanceEntities);
		map.put("path", "dst-db-instance");
		return new ModelAndView("main/container", map);
	}

	@RequestMapping(value = { "/dst-db-instance/create" })
	public ModelAndView create(HttpServletRequest request, HttpServletResponse response) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("path", "dst-db-instance");
		map.put("subPath", "create");
		return new ModelAndView("main/container", map);
	}

	@RequestMapping(value = { "/dst-db-instance/update" })
	public ModelAndView update(String id) {
		Map<String, Object> map = new HashMap<String, Object>();

		List<PumaTask> pumaTasks = pumaTaskService.findBySrcDBInstanceId(id);
		if (pumaTasks != null && pumaTasks.size() != 0) {
			map.put("lock", true);
		} else {
			map.put("lock", false);
		}

		try {
			DstDBInstance entity = dstDBInstanceService.find(id);
			map.put("entity", entity);
			map.put("path", "dst-db-instance");
			map.put("subPath", "create");
		} catch (Exception e) {
			// @TODO: error page.
		}

		return new ModelAndView("main/container", map);
	}

	@RequestMapping(value = { "/dst-db-instance/create" }, method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	public String createPost(
			String id,
			String name,
			Integer serverId,
			String host,
			int port,
			String username,
			String password,
			String metaHost,
			int metaPort,
			String metaUsername,
			String metaPassword) {

		Map<String, Object> map = new HashMap<String, Object>();

		DstDBInstance dstDBInstance;

		try {
			if (id != null) {
				// Update.
				dstDBInstance = dstDBInstanceService.find(id);
			} else {
				// Create.

				// Duplicated name?
				dstDBInstance = dstDBInstanceService.findByName(name);
				if (dstDBInstance == null) {
					dstDBInstance = new DstDBInstance();
				} else {
					throw new Exception("duplicated");
				}
			}

			dstDBInstance.setName(name);
			dstDBInstance.setServerId(serverId);
			dstDBInstance.setHost(host);
			dstDBInstance.setPort(port);
			dstDBInstance.setUsername(username);
			dstDBInstance.setPassword(password);
			dstDBInstance.setMetaHost(metaHost);
			dstDBInstance.setMetaPort(metaPort);
			dstDBInstance.setMetaUsername(metaUsername);
			dstDBInstance.setMetaPassword(metaPassword);

			if (id != null) {
				dstDBInstanceService.update(dstDBInstance);
			} else {
				dstDBInstanceService.create(dstDBInstance);
			}

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

	@RequestMapping(value = { "/dst-db-instance/remove" }, method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	public String removePost(String id) {
		Map<String, Object> map = new HashMap<String, Object>();

		try {
			List<PumaTask> pumaTasks = pumaTaskService.findBySrcDBInstanceId(id);

			if (pumaTasks != null && pumaTasks.size() != 0) {
				throw new Exception("lock");
			}

			this.dstDBInstanceService.remove(id);
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
