package com.dianping.puma.server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.dianping.puma.common.bo.PositionInfo;
import com.dianping.puma.common.bo.PumaContext;
import com.dianping.puma.common.util.PositionFileUtils;
import com.dianping.puma.common.util.PumaThreadUtils;

public class Bootstrap {

	private static Logger log = Logger.getLogger(Bootstrap.class);
	private static final String SPRING_CONFIG = "context-bootstrap.xml";
	private static final String BEAN_SERVERS = "servers";

	// monitor port number
	private static final int DEFAULT_MONITOR_PORT = 12345;
	private static final String MONITOR_PORT_KEY = "monitorPort";

	/**
	 * @param args
	 * @throws Exception
	 * @throws Exception
	 */

	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws Exception {
		// init spring
		ApplicationContext ctx = new ClassPathXmlApplicationContext(
				SPRING_CONFIG);
		List<Server> servers = (List<Server>) ctx.getBean(BEAN_SERVERS);

		// start servers
		for (Server server : servers) {

			PositionInfo posInfo = PositionFileUtils.getPositionInfo(server
					.getServerName(), server.getDefaultBinlogFileName(), server
					.getDefaultBinlogPosition());
			PumaContext context = new PumaContext();
			context.setPumaServerId(server.getServerId());
			context.setPumaServerName(server.getServerName());
			context.setBinlogFileName(posInfo.getBinlogFileName());
			context.setBinlogStartPos(posInfo.getBinlogPosition());
			server.setContext(context);
			startServer(server);
			log.info("Server " + server.getServerName()
					+ " started at binlogFile: " + context.getBinlogFileName()
					+ " position: " + context.getBinlogStartPos());
		}
		startMonitorTaskThread(servers);

	}

	public static void startServer(final Server server) throws Exception {
		PumaThreadUtils.createThread(new Runnable() {

			@Override
			public void run() {

				try {
					server.start();
				} catch (Exception e) {
					log.error("Start server: " + server.getServerName()
							+ " failed.", e);
				}
			}
		}, server.getServerName() + "_Connector", false).start();

	}

	private static void stopServer(Server server) throws Exception {
		server.stop();
	}

	private static void startMonitorTaskThread(List<Server> servers) {
		MonitorTask monitorTask = new MonitorTask();
		monitorTask.init(servers);
		Thread monitorThread=PumaThreadUtils.createThread(monitorTask, "monitor-thread",true);
		monitorThread.start();
	}

	private static class MonitorTask implements Runnable {
		private int port = DEFAULT_MONITOR_PORT;
		private String COMMAND_SHUTDOWN = "shutdown";
		private List<Server> servers;

		public void init(List<Server> servers) {
			this.servers = servers;
			String portStr = System.getProperty(MONITOR_PORT_KEY);
			if (portStr != null && StringUtils.isNumeric(portStr)) {
				port = Integer.parseInt(portStr);
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {

			try {
				ServerSocket ss = new ServerSocket(port);

				log.info("MonitorTask started at port: " + port);

				Socket socket = null;

				while (true) {
					try {
						socket = ss.accept();
						log.info("Accepted one connection : "
								+ socket.getRemoteSocketAddress());
						BufferedReader br = new BufferedReader(
								new InputStreamReader(socket.getInputStream()));
						String command = br.readLine();
						log.info("Command : " + command);
						if (COMMAND_SHUTDOWN.equals(command)) {
							for (Server server : servers) {

								try {
									log.info("Shutdown command received.");
									stopServer(server);

								} catch (Exception e) {
									log.error("Stop Server"
											+ server.getServerName()
											+ " failed.", e);
									e.printStackTrace();
								}
							}
							break;
						}
					} catch (Exception e) {
						// ignore
					} finally {
						if (socket != null) {
							socket.close();
						}
					}
				}

			} catch (Exception e) {
				log.error("MonitorTask start failed.", e);
			}

		}
	}

}
