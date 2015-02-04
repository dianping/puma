package com.dianping.puma.config;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import com.dianping.puma.core.replicate.model.config.ServerConfig;
import com.dianping.puma.core.util.IPUtils;
import com.dianping.puma.service.ServerConfigService;


public class InitializeServerConfig implements InitializingBean {

	private static final Logger LOG=LoggerFactory.getLogger(InitializeServerConfig.class);

	@Autowired
	private ServerConfigService serverConfigService;
	
	private String serverName;
	
	private String serverHost;
	
	private String localPort;

	private static InitializeServerConfig instance;

	@PostConstruct
	public void init() {
		for (String ip : IPUtils.getNoLoopbackIP4Addresses()) {
			String host = ip + ':' + localPort;
			ServerConfig serverConfig = serverConfigService.find(host);
			if(serverConfig!=null){
				this.serverName=serverConfig.getName();
				this.serverHost=serverConfig.getHost();
				break;
			}
		}
		if (serverName == null) {
			LOG.error("Not match any serverName....." );
            throw new RuntimeException("Cannot try to find the ServerName, please check the ServerConfig in DB.");
        }
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		instance = this;
	}

	public static InitializeServerConfig getInstance() {
		return instance;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	public String getServerName() {
		return serverName;
	}

	public void setLocalPort(String localPort) {
		this.localPort = localPort;
	}

	public String getLocalPort() {
		return localPort;
	}

	public void setServerHost(String serverHost) {
		this.serverHost = serverHost;
	}

	public String getServerHost() {
		return serverHost;
	}
}
