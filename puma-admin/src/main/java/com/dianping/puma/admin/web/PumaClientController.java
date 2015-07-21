package com.dianping.puma.admin.web;

import com.dianping.puma.core.util.GsonUtil;
import com.dianping.puma.biz.entity.PumaServerEntity;
import com.dianping.puma.biz.service.PumaServerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class PumaClientController {

	@Autowired
	PumaServerService pumaServerService;

	@RequestMapping(value = "/puma-client/puma-server-router")
	@ResponseBody
	public String getPumaServers(String database, String[] tables) {
		List<PumaServerEntity> pumaServers = pumaServerService.findByDatabaseAndTables(database, Arrays.asList(tables));
		Map<String, Float> loadBalances = new HashMap<String, Float>();
		for (PumaServerEntity pumaServer: pumaServers) {
			loadBalances.put(pumaServer.getHost() + ":" + pumaServer.getPort(), pumaServer.getLoadBalance());
		}

		return GsonUtil.toJson(loadBalances);
	}
}
