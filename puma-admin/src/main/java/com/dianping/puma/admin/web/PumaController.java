package com.dianping.puma.admin.web;

import com.dianping.puma.admin.db.DatabaseService;
import com.dianping.puma.admin.db.TableService;
import com.dianping.puma.admin.model.PumaDto;
import com.dianping.puma.admin.model.PumaServerStatusDto;
import com.dianping.puma.admin.service.PumaTaskStatusService;
import com.dianping.puma.biz.entity.PumaServerEntity;
import com.dianping.puma.biz.entity.PumaServerTargetEntity;
import com.dianping.puma.biz.entity.PumaTargetEntity;
import com.dianping.puma.biz.service.PumaServerService;
import com.dianping.puma.biz.service.PumaServerTargetService;
import com.dianping.puma.biz.service.PumaTargetService;
import com.dianping.puma.core.config.ConfigManager;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

@Controller
public class PumaController extends BasicController {

	private final Logger logger = LoggerFactory.getLogger(PumaController.class);

	@Autowired
	PumaTaskStatusService pumaTaskStatusService;

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
		List<String> serverNames = pumaDto.getServerNames();

		List<PumaServerTargetEntity> pumaServerTargets = pumaServerTargetService.findByDatabase(database);

		// Removes unused puma server target.
		for (PumaServerTargetEntity pumaServerTarget: pumaServerTargets) {
			if (!serverNames.contains(pumaServerTarget.getServerName())) {
				pumaServerTargetService.remove(pumaServerTarget.getId());
			}
		}

		// Creates new puma server target.
		List<String> oriServers = Lists.transform(pumaServerTargets, new Function<PumaServerTargetEntity, String>() {
			@Override
			public String apply(PumaServerTargetEntity pumaServerTarget) {
				return pumaServerTarget.getServerName();
			}
		});
		for (String serverName: serverNames) {
			if (!oriServers.contains(serverName)) {
				PumaServerTargetEntity pumaServerTarget = new PumaServerTargetEntity();
				pumaServerTarget.setServerName(serverName);
				pumaServerTarget.setTargetDb(database);

				if (pumaDto.getBeginTimes() != null) {
					pumaServerTarget.setBeginTime(pumaDto.getBeginTimes().get(serverName));
				}
				pumaServerTargetService.create(pumaServerTarget);
			}
		}

		// Removes unused puma tables.
		List<String> tables = pumaDto.getTables();
		List<PumaTargetEntity> pumaTargets = pumaTargetService.findByDatabase(database);
		List<String> oriTables = Lists.transform(pumaTargets, new Function<PumaTargetEntity, String>() {
			@Override
			public String apply(PumaTargetEntity pumaTarget) {
				return pumaTarget.getTable();
			}
		});
		for (PumaTargetEntity pumaTarget: pumaTargets) {
			if (!tables.contains(pumaTarget.getTable())) {
				pumaTargetService.remove(pumaTarget.getId());
			}
		}

		// Creates new puma tables.
		for (String table: tables) {
			if (!oriTables.contains(table)) {
				PumaTargetEntity pumaTarget = new PumaTargetEntity();
				pumaTarget.setDatabase(database);
				pumaTarget.setTable(table);
				pumaTargetService.create(pumaTarget);
			}
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

	@RequestMapping(value = {"/puma-status"}, method = RequestMethod.GET)
	@ResponseBody
	public Object status() {
		Map<String, Object> status = new HashMap<String, Object>();

		Map<String, PumaServerStatusDto> result = pumaTaskStatusService.getAllStatus();

		List<PumaServerStatusDto.Server> servers = new ArrayList<PumaServerStatusDto.Server>();
		List<PumaServerStatusDto.Client> clients = new ArrayList<PumaServerStatusDto.Client>();

		for (Map.Entry<String, PumaServerStatusDto> dto : result.entrySet()) {
			for (PumaServerStatusDto.Server server : dto.getValue().getServers().values()) {
				servers.add(server);
				server.setServer(dto.getKey());
			}

			for (PumaServerStatusDto.Client client : dto.getValue().getClients().values()) {
				clients.add(client);
				client.setServer(dto.getKey());
			}
		}

		status.put("servers", servers);
		status.put("clients", clients);

		return status;
	}
}
