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
import com.dianping.puma.core.registry.RegistryService;
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

	@Autowired
	RegistryService registryService;

	@RequestMapping(value = { "/puma/search" }, method = RequestMethod.GET)
	@ResponseBody
	public Object search(String database) {
		PumaDto pumaDto = new PumaDto();
		pumaDto.setDatabase(database);

		List<PumaServerTargetEntity> pumaServerTargets = pumaServerTargetService.findByDatabase(database);
		List<String> serverHosts = registryService.find(database);
		for (PumaServerTargetEntity pumaServerTarget : pumaServerTargets) {
			pumaDto.setTables(pumaServerTarget.getTables());
			pumaDto.addServerName(pumaServerTarget.getServerName());
			pumaDto.addHost(pumaServerTarget.getServerName(), pumaServerTarget.getServerHost());
			pumaDto.addBeginTime(pumaServerTarget.getServerName(), pumaServerTarget.getBeginTime());
			pumaDto.addRegistry(pumaServerTarget.getServerName(), serverHosts.contains(pumaServerTarget.getServerHost() + ":4040"));
		}

		return pumaDto;
	}

	@RequestMapping(value = { "/puma-create" }, method = RequestMethod.POST)
	@ResponseBody
	public Object create(@RequestBody PumaDto pumaDto) {
		for (Map.Entry<String, Long> entry : pumaDto.getBeginTimestamps().entrySet()) {
			if (entry.getValue() != null) {
				pumaDto.addBeginTime(entry.getKey(), new Date(entry.getValue()));
			}
		}

		String database = pumaDto.getDatabase();
		List<PumaServerTargetEntity> pumaServerTargets = pumaServerTargetService.findByDatabase(database);

		// Removes unused puma server target.
		List<String> serverNames = pumaDto.getServerNames();
		for (PumaServerTargetEntity pumaServerTarget : pumaServerTargets) {
			if (!serverNames.contains(pumaServerTarget.getServerName())) {
				pumaServerTargetService.remove(pumaServerTarget.getId());
			}
		}

		// Updates new puma server target.
		for (String serverName : serverNames) {
			PumaServerTargetEntity pumaServerTarget = new PumaServerTargetEntity();
			pumaServerTarget.setServerName(serverName);
			pumaServerTarget.setTargetDb(database);

			if (pumaDto.getBeginTimes() != null) {
				pumaServerTarget.setBeginTime(pumaDto.getBeginTimes().get(serverName));
			}
			pumaServerTargetService.replace(pumaServerTarget);
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
		for (PumaTargetEntity pumaTarget : pumaTargets) {
			if (!tables.contains(pumaTarget.getTable())) {
				pumaTargetService.remove(pumaTarget.getId());
			}
		}

		// Creates new puma tables.
		for (String table : tables) {
			if (!oriTables.contains(table)) {
				PumaTargetEntity pumaTarget = new PumaTargetEntity();
				pumaTarget.setDatabase(database);
				pumaTarget.setTable(table);
				pumaTargetService.create(pumaTarget);
			}
		}

		// Register and unregister.
		List<String> hosts = new ArrayList<String>();
		for (Map.Entry<String, Boolean> entry: pumaDto.getRegistries().entrySet()) {
			String serverName = entry.getKey();
			Boolean registry = entry.getValue();
			if (registry) {
				PumaServerEntity pumaServer = pumaServerService.find(serverName);
				String host = pumaServer.getHost();
				if (host != null) {
					hosts.add(host);
				}
			}
		}
		registryService.registerResetAll(hosts, database);

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

	@RequestMapping(value = { "/puma-status" }, method = RequestMethod.GET)
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
