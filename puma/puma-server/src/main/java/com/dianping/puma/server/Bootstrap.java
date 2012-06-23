package com.dianping.puma.server;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.dianping.puma.common.bo.PositionInfo;
import com.dianping.puma.common.bo.PumaContext;
import com.dianping.puma.common.util.PositionFileUtils;
import com.dianping.puma.common.util.PumaThreadUtils;

public class Bootstrap {

	private static Logger		log				= Logger.getLogger(Bootstrap.class);
	private static final String	SPRING_CONFIG	= "context-bootstrap.xml";
	private static final String	BEAN_SERVERS	= "servers";

	/**
	 * @param args
	 * @throws Exception
	 * @throws Exception
	 */

	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws Exception {
		// init spring
		ApplicationContext ctx = new ClassPathXmlApplicationContext(SPRING_CONFIG);
		List<Server> servers = (List<Server>) ctx.getBean(BEAN_SERVERS);

		// start servers
		for (Server server : servers) {
			PositionInfo posInfo = PositionFileUtils.getPositionInfo(server.getServerName(),
					server.getDefaultBinlogFileName(), server.getDefaultBinlogPosition());
			PumaContext context = new PumaContext();
			context.setBinlogFileName(posInfo.getBinlogFileName());
			context.setBinlogStartPos(posInfo.getBinlogPosition());
			server.setContext(context);
			startServer(server);
			log.info("Server " + server.getServerName() + " started at binlogFile: " + context.getBinlogFileName()
					+ " position: " + context.getBinlogStartPos());
		}

	}

	public static void startServer(final Server server) throws Exception {
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

	public static void stopServer(Server server) throws Exception {
		server.stop();
	}

}
