package com.dianping.puma.admin.web;

import com.dianping.puma.admin.service.DstDBInstanceService;
import com.dianping.puma.admin.util.GsonUtil;
import com.dianping.puma.core.entity.DstDBInstanceEntity;
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

	@RequestMapping(value = { "/dstDBInstance" })
	public ModelAndView view(HttpServletRequest request, HttpServletResponse response) {
		Map<String, Object> map = new HashMap<String, Object>();

		List<DstDBInstanceEntity> dstDBInstanceEntities = dstDBInstanceService.findAll();

		map.put("entities", dstDBInstanceEntities);
		map.put("path", "dstDBInstance");
		return new ModelAndView("main/container", map);
	}

	@RequestMapping(value = { "/dstDBInstance/create" })
	public ModelAndView create(HttpServletRequest request, HttpServletResponse response) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("path", "dstDBInstance");
		map.put("subPath", "create");
		return new ModelAndView("main/container", map);
	}

	@RequestMapping(value = { "/dstDBInstance/create" }, method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	public String createPost(
			String name,
			String host,
			String port,
			String username,
			String password,
			String metaHost,
			String metaPort,
			String metaUsername,
			String metaPassword) {

		Map<String, Object> map = new HashMap<String, Object>();

		DstDBInstanceEntity dstDBInstanceEntity = new DstDBInstanceEntity();
		dstDBInstanceEntity.setName(name);
		dstDBInstanceEntity.setHost(host);
		dstDBInstanceEntity.setPort(port);
		dstDBInstanceEntity.setUsername(username);
		dstDBInstanceEntity.setPassword(password);
		dstDBInstanceEntity.setMetaHost(metaHost);
		dstDBInstanceEntity.setMetaPort(metaPort);
		dstDBInstanceEntity.setMetaUsername(metaUsername);
		dstDBInstanceEntity.setMetaPassword(metaPassword);

		try {
			this.dstDBInstanceService.create(dstDBInstanceEntity);
			map.put("success", true);
		} catch (Exception e) {
			map.put("success", false);
		}

		return GsonUtil.toJson(map);
	}
}
