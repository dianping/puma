package com.dianping.puma.server;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.dianping.puma.common.util.PositionFileUtils;
import com.dianping.puma.common.util.PositionInfo;

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
		ApplicationContext ctx = new ClassPathXmlApplicationContext(SPRING_CONFIG);
		List<Server> servers = (List<Server>) ctx.getBean(BEAN_SERVERS);

		for (Server server : servers) {
			PositionInfo posInfo = PositionFileUtils.getPositionInfo(server.getServerName(),
					server.getDefaultBinlogFileName());
			server.setBinlogFileName(posInfo.getBinlogFileName());
			server.setBinlogPosition(posInfo.getBinlogPosition());
			startServer(server);
			log.info("Server " + server.getServerName() + " started.");
		}

	}

	public static void startServer(final Server server) throws Exception {
		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {

				try {
					server.start();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		t.setName("Server-" + server.getServerName() + "-thread");
		t.start();

	}

	public static void stopServer(Server server) throws Exception {
		server.stop();
	}

}
