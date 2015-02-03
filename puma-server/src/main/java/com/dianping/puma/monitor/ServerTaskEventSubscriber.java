package com.dianping.puma.monitor;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;

import com.dianping.puma.config.InitializeServerConfig;
import com.dianping.puma.core.monitor.SwallowEventSubscriber;

public class ServerTaskEventSubscriber extends SwallowEventSubscriber {
	
	@Autowired
	private InitializeServerConfig serverConfig;
	
	@PostConstruct
	public void init(){
		super.setType(serverConfig.getServerName());
		super.init();
	}

}
