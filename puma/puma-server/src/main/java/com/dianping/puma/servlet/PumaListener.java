package com.dianping.puma.servlet;

import java.util.List;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;

import com.dianping.puma.ComponentContainer;
import com.dianping.puma.bo.PositionInfo;
import com.dianping.puma.bo.PumaContext;
import com.dianping.puma.core.util.PumaThreadUtils;
import com.dianping.puma.server.Server;
import com.dianping.puma.utils.PositionFileUtils;

public class PumaListener implements ServletContextListener {
	private static Logger		log				= Logger.getLogger(PumaListener.class);

	private static final String	BEAN_SERVERS	= "servers";

	private List<Server>		servers;

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		for (Server server : servers) {
			try {
				server.stop();
				log.info("Server " + server.getServerName() + " stopped.");
			} catch (Exception e) {
				log.error("Stop Server" + server.getServerName() + " failed.", e);
			}
		}
	}

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		List<Server> servers = ComponentContainer.SPRING.lookup(BEAN_SERVERS);

		log.info("Starting " + servers.size() + " servers configured.");

		// start servers
		for (Server server : servers) {
			String serverName = server.getServerName();
			PositionInfo posInfo = PositionFileUtils.getPositionInfo(serverName, server.getDefaultBinlogFileName(),
					server.getDefaultBinlogPosition());
			PumaContext context = new PumaContext();

			context.setPumaServerId(server.getServerId());
			context.setPumaServerName(serverName);
			context.setBinlogFileName(posInfo.getBinlogFileName());
			context.setBinlogStartPos(posInfo.getBinlogPosition());
			server.setContext(context);
			startServer(server);
			log.info("Server " + serverName + " started at binlogFile: " + context.getBinlogFileName() + " position: "
					+ context.getBinlogStartPos());
		}

		this.servers = servers;
	}

	void startServer(final Server server) {
		PumaThreadUtils.createThread(new Runnable() {
			@Override
			public void run() {
				try {
					server.start();
				} catch (Exception e) {
					log.error("Start server: " + server.getServerName() + " failed.", e);
				}
			}
		}, server.getServerName() + "_Connector", false).start();
	}
}
