package com.dianping.puma.admin.web;

import com.dianping.puma.admin.service.DBInstanceConfigService;
import com.dianping.puma.admin.util.GsonUtil;
import com.dianping.puma.core.replicate.model.config.DBInstanceConfig;
import com.dianping.puma.core.replicate.model.config.DBInstanceHost;
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
public class DBInstanceConfigController {

	private static final Logger LOG = LoggerFactory.getLogger(DBInstanceConfigController.class);

	@Autowired
	DBInstanceConfigService dbInstanceConfigService;

	@RequestMapping(value = { "/dbInstanceConfig" })
	public ModelAndView view(HttpServletRequest request, HttpServletResponse response) {
		Map<String, Object> map = new HashMap<String, Object>();

		List<DBInstanceConfig> dbInstanceConfigs = dbInstanceConfigService.findAll();

		map.put("dbInstanceConfigs", dbInstanceConfigs);
		map.put("path", "dbInstanceConfig");
		return new ModelAndView("main/container", map);
	}

	@RequestMapping(value = { "/dbInstanceConfig/create" }, method = RequestMethod.GET)
	public ModelAndView create(HttpServletRequest request, HttpServletResponse response) {
		Map<String, Object> map = new HashMap<String, Object>();

		map.put("path", "dbInstanceConfig");
		map.put("subPath", "create");
		return new ModelAndView("main/container", map);
	}

	@RequestMapping(value = {
			"/dbInstanceConfig/create" }, method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	public String createPost(String name, String host, String port, String username, String password,
			String metaHost, String metaPort, String metaUsername, String metaPassword) {

		Map<String, Object> map = new HashMap<String, Object>();

		DBInstanceConfig dbInstanceConfig = new DBInstanceConfig();
		DBInstanceHost dbInstanceHost = new DBInstanceHost();
		DBInstanceHost dbInstanceMetaHost = new DBInstanceHost();

		dbInstanceHost.setHost(host);
		dbInstanceHost.setPort(port);
		dbInstanceHost.setUsername(username);
		dbInstanceHost.setPassword(password);

		dbInstanceMetaHost.setHost(metaHost);
		dbInstanceMetaHost.setPort(metaPort);
		dbInstanceMetaHost.setUsername(metaUsername);
		dbInstanceMetaHost.setPassword(metaPassword);

		dbInstanceConfig.setName(name);
		dbInstanceConfig.setDbInstanceHost(dbInstanceHost);
		dbInstanceConfig.setDbInstanceMetaHost(dbInstanceMetaHost);

		try {
			this.dbInstanceConfigService.save(dbInstanceConfig);
		} catch (Exception e) {
			map.put("success", false);
		}

		map.put("success", true);

		return GsonUtil.toJson(map);
	}
}
