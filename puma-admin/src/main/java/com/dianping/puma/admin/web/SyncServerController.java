package com.dianping.puma.admin.web;

import com.dianping.puma.admin.util.GsonUtil;
import com.dianping.puma.core.entity.SyncServer;
import com.dianping.puma.core.service.SyncServerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

	@Value("8080")
	Integer serverPort;

	@RequestMapping(value = { "/sync-server" })
	public ModelAndView view(HttpServletRequest request, HttpServletResponse response) {
		Map<String, Object> map = new HashMap<String, Object>();
		List<SyncServer> syncServerEntities = syncServerService.findAll();
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

			SyncServer entity = syncServerService.find(id);
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
	public String createPost(String name, String ip) {
		Map<String, Object> map = new HashMap<String, Object>();

		SyncServer syncServer = new SyncServer();
		syncServer.setName(name);

		// Split host and port.
		String[] hostAndPort = ip.split(":");
		String host = hostAndPort[0];
		Integer port = hostAndPort.length == 1 ? serverPort : Integer.parseInt(hostAndPort[1]);

		syncServer.setHost(host);
		syncServer.setPort(port);

		try {
			this.syncServerService.create(syncServer);
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
