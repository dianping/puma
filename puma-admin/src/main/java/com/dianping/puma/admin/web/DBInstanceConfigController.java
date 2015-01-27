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
import java.util.ArrayList;
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

	@RequestMapping(value = { "/dbInstanceConfig/create" }, method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	public String createPost(String name, String[] host, String[] port, String[] username, String[] password,
			String[] metaHost, String[] metaPort, String[] metaUsername, String[] metaPassword) {

		Map<String, Object> map = new HashMap<String, Object>();

		DBInstanceConfig dbInstanceConfig = new DBInstanceConfig();
		List<DBInstanceHost> dbInstanceHosts = new ArrayList<DBInstanceHost>();
		List<DBInstanceHost> dbInstanceMetaHosts = new ArrayList<DBInstanceHost>();

		for(int i = 0; i != host.length; ++i) {
			DBInstanceHost dbInstanceHost = new DBInstanceHost();
			dbInstanceHost.setHost(host[i]);
			dbInstanceHost.setPort(port[i]);
			dbInstanceHost.setUsername(username[i]);
			dbInstanceHost.setPassword(password[i]);
			dbInstanceHosts.add(dbInstanceHost);

			DBInstanceHost dbInstanceMetaHost = new DBInstanceHost();
			dbInstanceMetaHost.setHost(metaHost[i]);
			dbInstanceMetaHost.setPort(metaPort[i]);
			dbInstanceMetaHost.setUsername(metaUsername[i]);
			dbInstanceMetaHost.setPassword(metaPassword[i]);
			dbInstanceMetaHosts.add(dbInstanceMetaHost);
		}

		dbInstanceConfig.setName(name);
		dbInstanceConfig.setDbInstanceHosts(dbInstanceHosts);
		dbInstanceConfig.setDbInstanceMetaHosts(dbInstanceMetaHosts);

		try {
			this.dbInstanceConfigService.save(dbInstanceConfig);
		}
		catch(Exception e) {
			map.put("success", false);
		}

		map.put("success", true);

		return GsonUtil.toJson(map);
	}
}
