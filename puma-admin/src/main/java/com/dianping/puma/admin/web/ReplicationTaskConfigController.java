package com.dianping.puma.admin.web;

import com.dianping.puma.admin.service.ReplicationTaskConfigService;
import com.dianping.puma.core.replicate.model.config.ReplicationTaskConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ReplicationTaskConfigController {

	private static final Logger LOG = LoggerFactory.getLogger(DBInstanceConfigController.class);

	@Autowired
	ReplicationTaskConfigService replicationTaskConfigService;

	@RequestMapping(value = { "/replicationTaskConfig" })
	public ModelAndView view(HttpServletRequest request, HttpServletResponse response) {
		Map<String, Object> map = new HashMap<String, Object>();

		List<ReplicationTaskConfig> replicationTaskConfigs = replicationTaskConfigService.findAll();

		map.put("replicationTaskConfigs", replicationTaskConfigs);
		map.put("path", "replicationTaskConfig");
		return new ModelAndView("main/container", map);
	}
}
