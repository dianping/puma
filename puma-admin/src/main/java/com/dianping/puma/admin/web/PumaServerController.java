package com.dianping.puma.admin.web;

import com.dianping.puma.biz.entity.PumaServerEntity;
import com.dianping.puma.core.util.GsonUtil;
import com.dianping.puma.biz.service.PumaServerService;
import com.dianping.puma.biz.service.PumaTaskService;
import com.dianping.puma.biz.service.SyncTaskService;

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
public class PumaServerController {

	private final Logger logger = LoggerFactory.getLogger(PumaServerController.class);

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
		List<PumaServerEntity> pumaServerEntities = pumaServerService.findByPage(page, pageSize);
		map.put("count", count);
		map.put("list", pumaServerEntities);
		return GsonUtil.toJson(map);
	}
}
