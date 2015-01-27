package com.dianping.puma.servlet;

import java.util.List;

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
		List<Server> configedServers = null;
		try {
			serverManager.init();

		} catch (Exception e) {
			log.error("initialized failed....");
			e.printStackTrace();
			return;
		}
		try {
			configedServers = serverManager.constructServers();
		} catch (Exception e) {
			log.error("constructed servers failed....");
			e.printStackTrace();
			return;
		}
		log.info("Starting " + configedServers.size() + " servers configured.");

		// start servers
		for (Server server : configedServers) {
			serverManager.initContext(server);
			serverManager.start(server);
			log.info("Server " + server.getServerName()
					+ " started at binlogFile: "
					+ server.getContext().getBinlogFileName() + " position: "
					+ server.getContext().getBinlogStartPos());
		}

		Runtime.getRuntime().addShutdownHook(
				PumaThreadUtils.createThread(new Runnable() {
					@Override
					public void run() {
						serverManager.stopServers();
					}
				}, "ShutdownHook", false));
	}

}
