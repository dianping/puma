package com.dianping.puma.config;

import javax.annotation.PostConstruct;

import com.dianping.puma.core.entity.PumaServerEntity;
import com.dianping.puma.core.service.PumaServerService;
import com.dianping.puma.core.util.IPUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

public class InitializeServerConfig implements InitializingBean {

	private static final Logger LOG = LoggerFactory.getLogger(InitializeServerConfig.class);

	@Autowired
	PumaServerService pumaServerService;

	private String name;

	private String host;

	private String port;

	private static InitializeServerConfig instance;

	@PostConstruct
	public void init() {
		for (String ip : IPUtils.getNoLoopbackIP4Addresses()) {
			PumaServerEntity entity = pumaServerService.findByHostAndPort(ip, port);
			if (entity != null) {
				this.name = entity.getName();
				this.host = entity.getHost();
				this.port = entity.getPort();
				break;
			}
		}

		if (this.name == null) {
			LOG.error("Not match any server in DB.....");
			throw new RuntimeException("Cannot try to find the ServerName, please check the PumaServer in DB.");
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		instance = this;
	}

	public static InitializeServerConfig getInstance() {
		return instance;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getHost() {
		return this.host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getPort() {
		return this.port;
	}

	public void setPort(String port) {
		this.port = port;
	}
}
