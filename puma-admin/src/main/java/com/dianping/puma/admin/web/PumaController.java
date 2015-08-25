package com.dianping.puma.admin.web;

import com.dianping.puma.admin.db.DatabaseService;
import com.dianping.puma.admin.db.TableService;
import com.dianping.puma.admin.model.PumaDto;
import com.dianping.puma.biz.entity.PumaServerEntity;
import com.dianping.puma.biz.entity.PumaServerTargetEntity;
import com.dianping.puma.biz.service.PumaServerService;
import com.dianping.puma.biz.service.PumaServerTargetService;
import com.dianping.puma.biz.service.PumaTargetService;
import com.dianping.puma.core.config.ConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;

@Controller
public class PumaController extends BasicController {

	private final Logger logger = LoggerFactory.getLogger(PumaController.class);

	@Autowired
	ConfigManager configManager;

	@Autowired
	DatabaseService databaseService;

	@Autowired
	TableService tableService;

	@Autowired
	PumaServerService pumaServerService;

	@Autowired
	PumaTargetService pumaTargetService;

	@Autowired
	PumaServerTargetService pumaServerTargetService;

	@RequestMapping(value = { "/puma/search" }, method = RequestMethod.GET)
	@ResponseBody
	public Object search(String database) {
		PumaDto pumaDto = new PumaDto();
		pumaDto.setDatabase(database);

		List<PumaServerTargetEntity> pumaServerTargets = pumaServerTargetService.findByDatabase(database);
		for (PumaServerTargetEntity pumaServerTarget : pumaServerTargets) {
			pumaDto.setTables(pumaServerTarget.getTables());
			pumaDto.addServerName(pumaServerTarget.getServerName());
			pumaDto.addBeginTime(pumaServerTarget.getServerName(), pumaServerTarget.getBeginTime());
		}
		return pumaDto;
	}

	@RequestMapping(value = { "/puma-create" }, method = RequestMethod.POST)
	@ResponseBody
	public Object create(@RequestBody PumaDto pumaDto) {
		String database = pumaDto.getDatabase();
		List<String> tables = pumaDto.getTables();

		for (String serverName : pumaDto.getServerNames()) {
			PumaServerTargetEntity pumaServerTarget = new PumaServerTargetEntity();
			pumaServerTarget.setServerName(serverName);
			pumaServerTarget.setTargetDb(database);
			pumaServerTarget.setTables(tables);
			//pumaServerTarget.setBeginTime(pumaDto.getBeginTimes().get(serverName));
			pumaServerTargetService.replace(pumaServerTarget);
		}

		return null;
	}

	@RequestMapping(value = { "/puma-create/server" }, method = RequestMethod.GET)
	@ResponseBody
	public Object findServers() {
		List<String> servers = new ArrayList<String>();
		for (PumaServerEntity pumaServer : pumaServerService.findAll()) {
			servers.add(pumaServer.getName());
		}
		return servers;
	}

	@RequestMapping(value = { "/puma-create/database" }, method = RequestMethod.GET)
	@ResponseBody
	public Object findDatabases() {
		return databaseService.findAll();
	}

	@RequestMapping(value = { "/puma-create/table" }, method = RequestMethod.GET)
	@ResponseBody
	public Object findTables(String database) {
		return tableService.getTables(database);
	}
}
