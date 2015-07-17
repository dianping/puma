package com.dianping.puma.server.service;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dianping.puma.biz.entity.PumaServerEntity;
import com.dianping.puma.biz.service.PumaServerService;
import com.dianping.puma.core.util.IPUtils;

@Service("pumaServerConfig")
public class PumaServerConfig implements InitializingBean {

	private static final Logger LOG = LoggerFactory.getLogger(PumaServerConfig.class);

	@Autowired
	PumaServerService pumaServerService;

	private PumaServerEntity entity;

	@PostConstruct
	public void init() {
		for (String ip : IPUtils.getNoLoopbackIP4Addresses()) {
			LOG.info("ip = {}.", ip);
			entity = pumaServerService.findByHost(ip);

			if (entity != null) {
				LOG.info("Initialize puma server: name `{}`, host `{}`, port `{}`.", new Object[] { entity.getName(),
				      entity.getHost(), entity.getPort() });
				break;
			}
		}

		if (this.entity == null) {
			this.entity = new PumaServerEntity();

			String ip = IPUtils.getFirstNoLoopbackIP4Address();
			this.entity.setHost(ip);
			this.entity.setPort(8080);
			this.entity.setName(ip);

			pumaServerService.create(this.entity);
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
	}

	public String getName() {
		return this.entity.getName();
	}

	public String getHost() {
		return this.entity.getHost();
	}

	public Integer getPort() {
		return this.entity.getPort();
	}
}