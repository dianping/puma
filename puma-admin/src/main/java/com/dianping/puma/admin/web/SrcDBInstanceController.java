package com.dianping.puma.admin.web;

import com.dianping.puma.admin.util.GsonUtil;
import com.dianping.puma.core.entity.PumaTask;
import com.dianping.puma.core.entity.SrcDBInstance;
import com.dianping.puma.core.service.PumaTaskService;
import com.dianping.puma.core.service.SrcDBInstanceService;
import com.mongodb.MongoException;
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
public class SrcDBInstanceController {

	private static final Logger LOG = LoggerFactory.getLogger(SrcDBInstanceController.class);

	@Autowired
	SrcDBInstanceService srcDBInstanceService;

	@Autowired
	PumaTaskService pumaTaskService;

	@Value("3306")
	Integer dbPort;

	@RequestMapping(value = { "/src-db-instance" })
	public ModelAndView view(HttpServletRequest request, HttpServletResponse response) {
		Map<String, Object> map = new HashMap<String, Object>();

		List<SrcDBInstance> srcDBInstanceEntities = srcDBInstanceService.findAll();

		map.put("entities", srcDBInstanceEntities);
		map.put("path", "src-db-instance");
		return new ModelAndView("main/container", map);
	}

	@RequestMapping(value = { "/src-db-instance/create" })
	public ModelAndView create(HttpServletRequest request, HttpServletResponse response) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("path", "src-db-instance");
		map.put("subPath", "create");
		return new ModelAndView("main/container", map);
	}

	@RequestMapping(value = { "/src-db-instance/update" })
	public ModelAndView update(String id) {
		Map<String, Object> map = new HashMap<String, Object>();

		try {
			List<PumaTask> pumaTasks = pumaTaskService.findBySrcDBInstanceId(id);
			if (pumaTasks != null && pumaTasks.size() != 0) {
				map.put("lock", true);
			} else {
				map.put("lock", false);
			}

			SrcDBInstance entity = srcDBInstanceService.find(id);
			map.put("entity", entity);
			map.put("path", "src-db-instance");
			map.put("subPath", "create");

		} catch (Exception e) {
			// @TODO: error page.
		}

		return new ModelAndView("main/container", map);
	}

	@RequestMapping(value = {
			"/src-db-instance/create" }, method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	public String createPost(
			String id,
			String name,
			Integer serverId,
			String ip,
			String username,
			String password) {

		Map<String, Object> map = new HashMap<String, Object>();

		try {
			SrcDBInstance srcDBInstance;

			if (id != null) {
				// Update.
				srcDBInstance = srcDBInstanceService.find(id);
			} else {
				// Create.

				// Duplicated name?
				srcDBInstance = srcDBInstanceService.findByName(name);
				if (srcDBInstance == null) {
					srcDBInstance = new SrcDBInstance();
				} else {
					throw new Exception("duplicated");
				}
			}

			srcDBInstance.setName(name);
			srcDBInstance.setServerId(serverId);

			// Split host and port.
			String[] hostAndPort = ip.split(":");
			String host = hostAndPort[0];
			Integer port = hostAndPort.length == 1 ? dbPort : Integer.parseInt(hostAndPort[1]);

			srcDBInstance.setHost(host);
			srcDBInstance.setPort(port);
			srcDBInstance.setUsername(username);
			srcDBInstance.setPassword(password);
			srcDBInstance.setMetaHost(host);
			srcDBInstance.setMetaPort(port);
			srcDBInstance.setMetaUsername(username);
			srcDBInstance.setMetaPassword(password);

			if (id != null) {
				srcDBInstanceService.update(srcDBInstance);
			} else {
				srcDBInstanceService.create(srcDBInstance);
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

	@RequestMapping(value = {
			"/src-db-instance/remove" }, method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	public String removePost(String id) {
		Map<String, Object> map = new HashMap<String, Object>();

		try {
			List<PumaTask> pumaTasks = pumaTaskService.findBySrcDBInstanceId(id);

			if (pumaTasks != null && pumaTasks.size() != 0) {
				throw new Exception("lock");
			}

			this.srcDBInstanceService.remove(id);
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
