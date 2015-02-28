package com.dianping.puma.admin.web;

import com.dianping.puma.admin.util.GsonUtil;
import com.dianping.puma.core.entity.SyncServerEntity;
import com.dianping.puma.core.service.SyncServerService;
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
public class SyncServerController {

	private static final Logger LOG = LoggerFactory.getLogger(SyncServerController.class);

	@Autowired
	SyncServerService syncServerService;

	@RequestMapping(value = { "/sync-server" })
	public ModelAndView view(HttpServletRequest request, HttpServletResponse response) {
		Map<String, Object> map = new HashMap<String, Object>();
		List<SyncServerEntity> syncServerEntities = syncServerService.findAll();
		map.put("entities", syncServerEntities);
		map.put("path", "sync-server");
		return new ModelAndView("main/container", map);
	}

	@RequestMapping(value = { "/sync-server/create" })
	public ModelAndView create(HttpServletRequest request, HttpServletResponse response) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("path", "sync-server");
		map.put("subPath", "create");
		return new ModelAndView("main/container", map);
	}

	@RequestMapping(value = { "/sync-server/update" })
	public ModelAndView update(String id) {
		Map<String, Object> map = new HashMap<String, Object>();

		try {
			SyncServerEntity entity = syncServerService.find(id);
			map.put("entity", entity);
			map.put("path", "sync-server");
			map.put("subPath", "create");
		} catch (Exception e) {
			// @TODO: error page.
		}

		return new ModelAndView("main/container", map);
	}

	@RequestMapping(value = { "/sync-server/create" }, method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	public String createPost(String name, String host, String port) {
		Map<String, Object> map = new HashMap<String, Object>();

		SyncServerEntity syncServerEntity = new SyncServerEntity();
		syncServerEntity.setName(name);
		syncServerEntity.setHost(host);
		syncServerEntity.setPort(port);

		try {
			this.syncServerService.create(syncServerEntity);
			map.put("success", true);
		} catch (Exception e) {
			map.put("success", false);
		}

		return GsonUtil.toJson(map);
	}

	@RequestMapping(value = { "/sync-server/remove" }, method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	public String removePost(String id) {
		Map<String, Object> map = new HashMap<String, Object>();

		try {
			this.syncServerService.remove(id);
			map.put("success", true);
		} catch (Exception e) {
			map.put("success", false);
		}

		return GsonUtil.toJson(map);
	}
}
