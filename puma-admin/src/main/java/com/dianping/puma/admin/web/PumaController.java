package com.dianping.puma.admin.web;

import com.dianping.puma.admin.model.PumaDto;
import com.dianping.puma.biz.entity.PumaServerTargetEntity;
import com.dianping.puma.biz.entity.PumaTargetEntity;
import com.dianping.puma.biz.service.PumaServerService;
import com.dianping.puma.biz.service.PumaServerTargetService;
import com.dianping.puma.biz.service.PumaTargetService;
import com.dianping.puma.core.util.GsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
public class PumaController {

	private final Logger logger = LoggerFactory.getLogger(PumaController.class);

	@Autowired
	PumaServerService pumaServerService;

	@Autowired
	PumaTargetService pumaTargetService;

	@Autowired
	PumaServerTargetService pumaServerTargetService;

	@RequestMapping(value = { "/puma-task/list" }, method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	public String ajaxList(@RequestBody PumaDto pumaDto) {
		Map<String, Object> result = new HashMap<String, Object>();

		try {
			String database = pumaDto.getDatabase();
			List<PumaServerTargetEntity> pumaServerTargets = pumaServerTargetService.findByDatabase(database);
//			for (PumaServerTargetEntity pumaServerTarget: pumaServerTargets) {
//				pumaDto.setTables(pumaServerTarget.getTables());
//				pumaDto.addServer(pumaServerTarget.getHost());
//			}

			result.put("status", "success");
			result.put("result", pumaDto);
			return GsonUtil.toJson(result);

		} catch (Throwable t) {
			logger.error("failed to list puma task", t);
			result.put("status", "failure");
			result.put("msg", t.getMessage());
			return GsonUtil.toJson(result);
		}
	}

	@RequestMapping(value = { "/puma-task/create" }, method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	public String ajaxCreate(@RequestBody PumaDto pumaDto) {
		try {
			String database = pumaDto.getDatabase();
			List<String> tables = pumaDto.getTables();
			List<Integer> serverIds = pumaDto.getServerIds();
			Date beginTime = pumaDto.getBeginTime();

			PumaTargetEntity pumaTarget = new PumaTargetEntity();
			pumaTarget.setDatabase(database);
//			pumaTarget.setTables(tables);
//			pumaTargetService.createOrUpdate(pumaTarget);
//
//			for (Integer serverId : serverIds) {
//				PumaServerTargetEntity pumaServerTarget = new PumaServerTargetEntity();
//				pumaServerTarget.setServerId(serverId);
//				pumaServerTarget.setTargetId(pumaTarget.getId());
//				pumaServerTarget.setBeginTime(beginTime);
//				pumaServerTargetService.createOrUpdate(pumaServerTarget);
//			}
		} catch (Throwable t) {
			return null;
		}

		return null;
	}
}
