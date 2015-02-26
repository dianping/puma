package com.dianping.puma.admin.web;

import com.dianping.puma.admin.util.GsonUtil;
import com.dianping.puma.core.entity.PumaServerEntity;
import com.dianping.puma.core.service.PumaServerService;
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
public class PumaServerController {

	private static final Logger LOG = LoggerFactory.getLogger(PumaServerController.class);

	@Autowired
	PumaServerService pumaServerService;

	@RequestMapping(value = { "/puma-server" })
	public ModelAndView view(HttpServletRequest request, HttpServletResponse response) {
		Map<String, Object> map = new HashMap<String, Object>();
		List<PumaServerEntity> pumaServerEntities = pumaServerService.findAll();
		map.put("entities", pumaServerEntities);
		map.put("path", "puma-server");
		return new ModelAndView("main/container", map);
	}

	@RequestMapping(value = { "/puma-server/create" })
	public ModelAndView create(HttpServletRequest request, HttpServletResponse response) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("path", "puma-server");
		map.put("subPath", "create");
		return new ModelAndView("main/container", map);
	}

	@RequestMapping(value = { "/puma-server/create" }, method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	public String createPost(String name, String host, String port) {
		Map<String, Object> map = new HashMap<String, Object>();

		PumaServerEntity pumaServerEntity = new PumaServerEntity();
		pumaServerEntity.setName(name);
		pumaServerEntity.setHost(host);
		pumaServerEntity.setPort(port);

		try {
			this.pumaServerService.create(pumaServerEntity);
			map.put("success", true);
		} catch (Exception e) {
			map.put("success", false);
		}

		return GsonUtil.toJson(map);
	}
}
