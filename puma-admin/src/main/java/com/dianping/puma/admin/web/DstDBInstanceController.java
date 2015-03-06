package com.dianping.puma.admin.web;

import com.dianping.puma.admin.util.GsonUtil;
import com.dianping.puma.core.entity.DstDBInstance;
import com.dianping.puma.core.service.DstDBInstanceService;
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

		DstDBInstance dstDBInstance = new DstDBInstance();
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

		try {
			this.dstDBInstanceService.create(dstDBInstance);
			map.put("success", true);
		} catch (Exception e) {
			map.put("success", false);
		}

		return GsonUtil.toJson(map);
	}

	@RequestMapping(value = { "/dst-db-instance/remove" }, method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	public String removePost(String id) {
		Map<String, Object> map = new HashMap<String, Object>();

		try {
			this.dstDBInstanceService.remove(id);
			map.put("success", true);
		} catch (Exception e) {
			map.put("success", false);
		}

		return GsonUtil.toJson(map);
	}
}
