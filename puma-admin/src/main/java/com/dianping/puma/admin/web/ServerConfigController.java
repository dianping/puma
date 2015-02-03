package com.dianping.puma.admin.web;

import com.dianping.puma.admin.service.ServerConfigService;
import com.dianping.puma.admin.util.GsonUtil;
import com.dianping.puma.core.replicate.model.config.ServerConfig;
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
public class ServerConfigController {

	private static final Logger LOG = LoggerFactory.getLogger(DBInstanceConfigController.class);

	@Autowired
	ServerConfigService serverConfigService;

	@RequestMapping(value = { "/serverConfig" })
	public ModelAndView view(HttpServletRequest request, HttpServletResponse response) {
		Map<String, Object> map = new HashMap<String, Object>();

		List<ServerConfig> serverConfigs = serverConfigService.findAll();

		map.put("serverConfigs", serverConfigs);
		map.put("path", "serverConfig");
		return new ModelAndView("main/container", map);
	}

	@RequestMapping(value = { "/serverConfig/create" }, method = RequestMethod.GET)
	public ModelAndView create(HttpServletRequest request, HttpServletResponse response) {
		Map<String, Object> map = new HashMap<String, Object>();

		map.put("path", "serverConfig");
		map.put("subPath", "create");
		return new ModelAndView("main/container", map);
	}

	@RequestMapping(value = { "/serverConfig/create" }, method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	public String createPost(String name, String host, String port) {

		Map<String, Object> map = new HashMap<String, Object>();

		ServerConfig serverConfig = new ServerConfig();
		serverConfig.setName(name);
		serverConfig.setHost(host + ":" + port);

		try {
			this.serverConfigService.save(serverConfig);
		}
		catch(Exception e) {
			map.put("success", false);
		}

		map.put("success", true);

		return GsonUtil.toJson(map);
	}
}
