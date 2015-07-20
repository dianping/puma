package com.dianping.puma.admin.web;

import com.dianping.puma.admin.util.GsonUtil;
import com.dianping.puma.biz.entity.PumaServerEntity;
import com.dianping.puma.biz.service.PumaServerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class PumaClientController {

	@Autowired
	PumaServerService pumaServerService;

	@RequestMapping(value = "/client/server")
	@ResponseBody
	public String list(String database, List<String> table) {
		Map<String, Object> map = new HashMap<String, Object>();

		/*
		List<PumaServerEntity> pumaServers = pumaServerService.findByDatabaseAndTables(database, tables);
		Map<String, Float> loadBalances = new HashMap<String, Float>();
		for (PumaServerEntity pumaServer: pumaServers) {
			loadBalances.put(pumaServer.getHost() + ":" + pumaServer.getPort(), pumaServer.getLoadBalance());
		}*/
		String loadBalances = "hello";

		map.put("loadBalances", loadBalances);

		return GsonUtil.toJson(map);
	}
}
