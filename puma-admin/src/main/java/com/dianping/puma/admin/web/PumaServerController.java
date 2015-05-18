package com.dianping.puma.admin.web;

import com.dianping.puma.admin.util.GsonUtil;
import com.dianping.puma.core.constant.ActionOperation;
import com.dianping.puma.core.entity.PumaServer;
import com.dianping.puma.core.entity.PumaTask;
import com.dianping.puma.core.entity.SyncTask;
import com.dianping.puma.core.service.PumaServerService;
import com.dianping.puma.core.service.PumaTaskService;
import com.dianping.puma.core.service.SyncTaskService;
import com.mongodb.MongoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
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

	@Autowired
	PumaTaskService pumaTaskService;

	@Autowired
	SyncTaskService syncTaskService;

	@Value("8080")
	Integer serverPort;

	@RequestMapping(value = { "/puma-server" })
	public ModelAndView view(HttpServletRequest request, HttpServletResponse response) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("path", "puma-server");
		return new ModelAndView("common/main-container", map);
	}

	@RequestMapping(value = { "/puma-server/list" }, method = RequestMethod.GET, produces = "application/json; charset=utf-8")
	@ResponseBody
	public String list(int page, int pageSize) {
		Map<String, Object> map = new HashMap<String, Object>();
		long count = pumaServerService.count();
		List<PumaServer> pumaServerEntities = null;
		pumaServerEntities = pumaServerService.findByPage(page, pageSize);
		map.put("count", count);
		map.put("list", pumaServerEntities);
		return GsonUtil.toJson(map);
	}

	@RequestMapping(value = { "/puma-server/create" })
	public ModelAndView create(HttpServletRequest request, HttpServletResponse response) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("path", "puma-server");
		map.put("subPath", "create");
		return new ModelAndView("common/main-container", map);
	}


	@RequestMapping(value = { "/puma-server/update/{id}" })
	public ModelAndView update(@PathVariable long id) {
		Map<String, Object> map = new HashMap<String, Object>();

		try {
			PumaServer entity = pumaServerService.find(id);
			if (entity != null) {
				List<PumaTask> pumaTasks = pumaTaskService.findByPumaServerName(entity.getName());
				List<SyncTask> syncTasks = syncTaskService.findByPumaServerName(entity.getName());
				if ((pumaTasks != null && pumaTasks.size() != 0) || (syncTasks != null && syncTasks.size() != 0)) {
					map.put("lock", true);
				} else {
					map.put("lock", false);
				}
			}
			map.put("entity", entity);
			map.put("path", "puma-server");
			map.put("subPath", "create");
		} catch (Exception e) {
			// @TODO: error page.
		}

		return new ModelAndView("common/main-container", map);
	}

	@RequestMapping(value = { "/puma-server/create" }, method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	public String createPost(String name, String host, String port) {
		Map<String, Object> map = new HashMap<String, Object>();

		try {
			ActionOperation operation;

			PumaServer pumaServer = pumaServerService.find(name);
			if (pumaServer == null) {
				operation = ActionOperation.CREATE;
				pumaServer = new PumaServer();
			} else {
				throw new Exception("duplicate name.");
			}

			pumaServer.setName(name);
			pumaServer.setHost(host);
			pumaServer.setPort(port == null ? serverPort : Integer.parseInt(port));

			if (operation == ActionOperation.CREATE) {
				pumaServerService.create(pumaServer);
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

	@RequestMapping(value = { "/puma-server/update/{id}" }, method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	public String updatePost(@PathVariable long id, String name, String host, String port) {
		Map<String, Object> map = new HashMap<String, Object>();

		try {
			ActionOperation operation;

			PumaServer pumaServer = pumaServerService.find(id);
			if (pumaServer == null) {
				operation = ActionOperation.CREATE;
				pumaServer = new PumaServer();
			} else {
				operation = ActionOperation.UPDATE;
			}

			pumaServer.setName(name);
			pumaServer.setHost(host);
			pumaServer.setPort(port == null ? serverPort : Integer.parseInt(port));

			if (operation == ActionOperation.CREATE) {
				pumaServerService.create(pumaServer);
			} else {
				pumaServerService.update(pumaServer);
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

	@RequestMapping(value = { "/puma-server/remove" }, method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	public String removePost(String name) {
		Map<String, Object> map = new HashMap<String, Object>();

		try {
			List<PumaTask> pumaTasks = pumaTaskService.findByPumaServerName(name);
			List<SyncTask> syncTasks = syncTaskService.findByPumaServerName(name);
			if (pumaTasks != null && pumaTasks.size() != 0 && syncTasks != null && syncTasks.size() != 0) {
				throw new Exception("lock");
			}

			pumaServerService.remove(name);
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
