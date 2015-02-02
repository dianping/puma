package com.dianping.puma.servlet;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.dianping.puma.ComponentContainer;
import com.dianping.puma.core.util.PumaThreadUtils;
import com.dianping.puma.server.DefaultServerManager;
import com.dianping.puma.server.Server;
import com.dianping.puma.server.ServerManager;

public class PumaListener implements ServletContextListener {

	private static Logger log = Logger.getLogger(PumaListener.class);

	private ServerManager serverManager;

	private static final String BEAN_SERVERMANAGER = "serverManager";
	
	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		serverManager.stopServers();
	}

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		serverManager =  ComponentContainer.SPRING.lookup(BEAN_SERVERMANAGER);
		//serverManager = new DefaultServerManager();
		Runtime.getRuntime().addShutdownHook(
				PumaThreadUtils.createThread(new Runnable() {
					@Override
					public void run() {
						serverManager.stopServers();
					}
				}, "ShutdownHook", false));
	}

}
