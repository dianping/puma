package com.dianping.puma.admin.web;

import com.dianping.puma.admin.util.GsonUtil;
import com.dianping.puma.core.constant.ActionOperation;
import com.dianping.puma.core.entity.DstDBInstance;
import com.dianping.puma.core.entity.PumaTask;
import com.dianping.puma.core.service.DstDBInstanceService;
import com.dianping.puma.core.service.PumaTaskService;
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
public class DstDBInstanceController {

	private static final Logger LOG = LoggerFactory.getLogger(DstDBInstanceController.class);

	@Autowired
	DstDBInstanceService dstDBInstanceService;

	@Autowired
	PumaTaskService pumaTaskService;

	@Value("3306")
	Integer dbPort;

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
	public ModelAndView update(String name) {
		Map<String, Object> map = new HashMap<String, Object>();

		List<PumaTask> pumaTasks = pumaTaskService.findBySrcDBInstanceName(name);
		if (pumaTasks != null && pumaTasks.size() != 0) {
			map.put("lock", true);
		} else {
			map.put("lock", false);
		}

		try {
			DstDBInstance entity = dstDBInstanceService.find(name);
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
			Long serverId,
			String ip,
			String username,
			String password) {

		Map<String, Object> map = new HashMap<String, Object>();

		try {
			ActionOperation operation;

			DstDBInstance dstDBInstance = dstDBInstanceService.find(name);
			if (dstDBInstance == null) {
				operation = ActionOperation.CREATE;
				dstDBInstance = new DstDBInstance();
			} else {
				operation = ActionOperation.UPDATE;
			}

			dstDBInstance.setName(name);
			dstDBInstance.setServerId(serverId);

			// Split host and port.
			String[] hostAndPort = ip.split(":");
			String host = hostAndPort[0];
			Integer port = hostAndPort.length == 1 ? dbPort : Integer.parseInt(hostAndPort[1]);

			dstDBInstance.setHost(host);
			dstDBInstance.setPort(port);
			dstDBInstance.setUsername(username);
			dstDBInstance.setPassword(password);
			dstDBInstance.setMetaHost(host);
			dstDBInstance.setMetaPort(port);
			dstDBInstance.setMetaUsername(username);
			dstDBInstance.setMetaPassword(password);

			if (operation == ActionOperation.CREATE) {
				dstDBInstanceService.create(dstDBInstance);
			} else {
				dstDBInstanceService.update(dstDBInstance);
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

	@RequestMapping(value = { "/dst-db-instance/remove" }, method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	public String removePost(String name) {
		Map<String, Object> map = new HashMap<String, Object>();

		try {
			List<PumaTask> pumaTasks = pumaTaskService.findBySrcDBInstanceName(name);

			if (pumaTasks != null && pumaTasks.size() != 0) {
				throw new Exception("lock");
			}

			this.dstDBInstanceService.remove(name);
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
