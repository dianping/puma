package com.dianping.puma.monitor;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;

import com.dianping.puma.config.ServerConfig;
import com.dianping.puma.core.monitor.SwallowEventSubscriber;

public class ServerTaskEventSubscriber extends SwallowEventSubscriber {
	
	@Autowired
	private ServerConfig config;
	
	@PostConstruct
	public void init(){
		super.setType(config.getServerName());
		super.init();
	}

}
