package com.dianping.puma.api;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import com.dianping.lion.client.ConfigCache;
import com.dianping.puma.api.config.Config;
import com.dianping.puma.api.exception.AuthException;
import com.dianping.puma.api.manager.*;
import com.dianping.puma.api.manager.impl.DefaultHeartbeatManager;
import com.dianping.puma.api.manager.impl.DefaultHostManager;
import com.dianping.puma.api.manager.impl.DefaultLockManager;
import com.dianping.puma.api.manager.impl.DefaultPositionManager;
import com.dianping.puma.api.service.PositionService;
import com.dianping.puma.api.service.impl.PigeonPositionService;
import com.dianping.puma.api.util.Clock;
import com.dianping.puma.api.util.Monitor;
import com.dianping.puma.core.codec.EventCodecFactory;
import com.dianping.puma.core.event.*;
import com.dianping.puma.core.model.BinlogInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.puma.core.codec.EventCodec;
import com.dianping.puma.core.util.ByteArrayUtils;
import com.dianping.puma.core.util.StreamUtils;

public class PumaClient {

	private static final Logger logger = LoggerFactory.getLogger(PumaClient.class);

	private volatile boolean inited = false;

	private String name;
	private String database;
	private List<String> tables;
	private boolean dml = true;
	private boolean ddl = false;
	private boolean transaction = false;

	private EventListener eventListener;
	private EventCodec codec;
	private boolean async;

	private SubscribeTask subscribeTask;
	private Thread subscribeThread;

	private ConfigCache configCache = ConfigCache.getInstance();
	private Clock clock = new Clock();

	private Monitor monitor;
	private Config config;
	private HostManager hostManager;
	private PositionManager positionManager;

	private HeartbeatManager heartbeatManager;
	private LockManager lockManager;

	private PositionService positionService;

	public PumaClient() {
	}

	@Deprecated
	public PumaClient(Configuration configuration) {
	}

	public void register(EventListener listener) {
		this.eventListener = listener;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setDatabase(String database) {
		this.database = database;
	}

	public void setTables(List<String> tables) {
		this.tables = tables;
	}

	public void setDml(boolean dml) {
		this.dml = dml;
	}

	public void setDdl(boolean ddl) {
		this.ddl = ddl;
	}

	public void setTransaction(boolean transaction) {
		this.transaction = transaction;
	}

	public void setAsync(boolean async) {
		this.async = async;
	}

	public void asyncSavePosition(BinlogInfo binlogInfo) {
		positionManager.save(binlogInfo);
	}

	public void start() {
		if (inited) {
			return;
		}

		try {
			startSpringContainer();
			startMonitor();
			startConfig();
			startHostManager();
			startPositionManager();
			startHeartbeatManager();
			//startLockManager();
			startSubscribe();
		} catch (Exception e) {
			monitor.logError(logger, "start error", e);
			throw new RuntimeException(e);
		}

		inited = true;
		monitor.logInfo(logger, "started");
	}

	public void stop() {
		if (!inited) {
			return;
		}

		try {
			stopSubscribe();
			//stopLockManager();
			stopHeartbeatManager();
			stopPositionManager();
			stopHostManager();
			stopConfig();
			stopSpringContainer();
		} catch (Throwable e) {
			monitor.logError(logger, "stop error", e);
			throw new RuntimeException(e);
		}

		inited = false;
		monitor.logInfo(logger, "stopped");
	}

	private void startSpringContainer() {
		positionService = new PigeonPositionService();
	}

	private void startMonitor() {
		monitor = new Monitor();
		monitor.setClient(this);
	}

	private void startConfig() {
		config = new Config();
		config.setClient(this);
		config.setMonitor(monitor);
		config.setConfigCache(configCache);
		config.start();

		// Get event codec.
		codec = EventCodecFactory.createCodec(config.getCodecType());
	}

	private void startHostManager() {
		DefaultHostManager defaultHostManager = new DefaultHostManager();
		defaultHostManager.setClient(this);
		defaultHostManager.setConfig(config);
		defaultHostManager.setConfigCache(configCache);
		hostManager = defaultHostManager;
		hostManager.start();
	}

	private void startPositionManager() {
		DefaultPositionManager defaultPositionManager = new DefaultPositionManager();
		defaultPositionManager.setClient(this);
		defaultPositionManager.setMonitor(monitor);
		defaultPositionManager.setConfig(config);
		defaultPositionManager.setHostManager(hostManager);
		defaultPositionManager.setClock(clock);
		defaultPositionManager.setPositionService(positionService);
		positionManager = defaultPositionManager;
		positionManager.start();
	}

	private void startHeartbeatManager() {
		DefaultHeartbeatManager defaultHeartbeatManager = new DefaultHeartbeatManager();
		defaultHeartbeatManager.setClient(this);
		defaultHeartbeatManager.setMonitor(monitor);
		defaultHeartbeatManager.setConfig(config);
		defaultHeartbeatManager.setHostManager(hostManager);
		defaultHeartbeatManager.setClock(clock);
		heartbeatManager = defaultHeartbeatManager;
		heartbeatManager.start();
	}

	private void startLockManager() {
		DefaultLockManager defaultLockManager = new DefaultLockManager();
		defaultLockManager.setClient(this);
		lockManager = defaultLockManager;
		lockManager.start();
	}

	public void startSubscribe() {
		subscribeTask = new SubscribeTask();
		subscribeThread = new Thread(subscribeTask);
		subscribeThread.setName(String.format("puma-subscribe-thread-%s", name));
		subscribeThread.setDaemon(true);
		subscribeThread.start();
	}

	private void stopSpringContainer() {
		SpringContainer.getInstance().stop();
	}

	private void stopConfig() {
		config.stop();
	}

	private void stopHostManager() {
		hostManager.stop();
	}

	private void stopPositionManager() {
		positionManager.stop();
	}

	private void stopHeartbeatManager() {
		heartbeatManager.stop();
	}

	private void stopLockManager() {
		lockManager.stop();
	}

	public void stopSubscribe() {
		subscribeTask.stop();
		subscribeThread.interrupt();
	}

	public String getName() {
		return name;
	}

	private class SubscribeTask implements Runnable {

		private volatile boolean stopped = false;

		private boolean first = true;
		private HttpURLConnection connection = null;
		private InputStream is = null;

		@Override
		public void run() {

			while (!checkStop()) {

				// Sleep if reconnect.
				if (!first) {
					sleep(config.getReconnectSleepTime());
				}
				first = false;

				// Connect.
				try {
					if (!checkStop()) {
						connect();

						monitor.logInfo(logger, hostManager.current(), "connected");
					}
				} catch (IOException e) {

					if (!checkStop()) {
						hostManager.feedback(Feedback.NET_ERROR);
						monitor.logError(logger, hostManager.current(), "connect error", e);
					}

					disconnect();
					monitor.logInfo(logger, hostManager.current(), "disconnected");

					continue;
				}

				// Read events.
				try {

					heartbeatManager.open();

					while (!checkStop()) {

						// The subscribe thread might be blocked here and can not be stopped and
						// interrupted. Deal carefully.
						Event event = readEvent(is);

						// No.1 reborn place of the zombie thread.
						// Send the revived zombie thread to the `finally` block.
						if (!checkStop() && event != null) {

							// Each event is treated as a heartbeat.
							heartbeatManager.heartbeat();

							// Changed event, retry for a several times if on event exception occurs.
							if (event instanceof ChangedEvent) {
								ChangedEvent changedEvent = (ChangedEvent) event;

								for (int i = 0; i != config.getOnEventRetryCount(); ++i) {
									try {
										eventListener.onEvent(changedEvent);
										break;
									} catch (Throwable e) {
										monitor.logError(logger, hostManager.current(), String.format("subscribe error(%s)", event), e);
									}
								}

								if (!async) {
									positionManager.save(changedEvent.getBinlogInfo());
								}
							}

							// Heartbeat event, pass it and keep on reading events.
							if (event instanceof HeartbeatEvent) {
								monitor.logInfo(logger, hostManager.current(), "heartbeat");
								continue;
							}

							// Server error event, reconnect.
							if (event instanceof ServerErrorEvent) {
								// @TODO
								ServerErrorEvent serverErrorEvent = (ServerErrorEvent) event;
								monitor.logError(logger, serverErrorEvent.getMsg(), serverErrorEvent.getCause());
								hostManager.feedback(Feedback.SERVER_ERROR);
								break;
							}
						}
					}

				} catch (IOException e) {

					// No.2 reborn place of the zombie thread.
					// Send the revived zombie thread to the `finally` block.
					if (!checkStop()) {
						hostManager.feedback(Feedback.NET_ERROR);
						monitor.logError(logger, hostManager.current(), "subscribe error", e);
					}

				} finally {
					// Revived zombie thread need not close the heartbeat manager.
					if (checkStop()) {
						heartbeatManager.close();
					}

					disconnect();
					monitor.logInfo(logger, hostManager.current(), "disconnected");
				}
			}
		}

		public void stop() {
			stopped = true;

			// Maybe the current thread will be blocked at `readEvent`. Under that
			// circumstance, we close the heartbeat manager in the stop function and
			// check stop in the finally block.
			heartbeatManager.close();
		}

		private boolean checkStop() {
			return stopped || Thread.currentThread().isInterrupted();
		}

		private void connect() throws IOException, AuthException {
			/*
			if (!lockManager.tryLock()) {
				throw new AuthException("Puma locks connection failure.");
			}

			logger.info("Puma({}) locks connection successfully.", name);
			*/

			String host = hostManager.next();
			URL url = new URL("http://" + host + "/puma/channel");
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			connection.setConnectTimeout(3000);
			connection.setDoOutput(true);
			connection.setUseCaches(false);
			connection.setRequestProperty("Connection", "Keep-Alive");
			connection.setRequestProperty("Cache-Control", "no-cache");

			PrintWriter out = new PrintWriter(connection.getOutputStream());
			String requestParams = buildRequestParamString();
			out.print(requestParams);
			out.close();

			//logger.info(loggerName + "connection host: {}", host);
			//logger.info(loggerName + "connection params: {}", requestParams);

			is = connection.getInputStream();
		}

		private void disconnect() {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					// Ignore.
				}
			}
			if (connection != null) {
				connection.disconnect();
			}
		}

		private String buildRequestParamString() {
			String binlogFile;
			long binlogPosition;
			BinlogInfo binlogInfo = positionManager.next();
			if (binlogInfo == null) {
				binlogFile = null;
				binlogPosition = 0;
			} else {
				binlogFile = binlogInfo.getBinlogFile();
				binlogPosition = binlogInfo.getBinlogPosition();
			}

			StringBuilder builder = (new StringBuilder())
					.append("seq=").append(-3)
					.append("&binlog=").append(binlogFile)
					.append("&binlogPos=").append(binlogPosition)
					.append("&serverId=").append(config.getServerId())
					.append("&name=").append(name)
					.append("&target=").append(config.getTarget())
					.append("&dml=").append(dml)
					.append("&ddl=").append(ddl)
					.append("&ts=").append(transaction)
					.append("&codec=").append(config.getCodecType());

			for (String table: tables) {
				builder.append("&dt=").append(database).append(".").append(table);
			}

			return builder.toString();
		}

		private Event readEvent(InputStream is) throws IOException {
			byte[] lengthArray = new byte[4];
			StreamUtils.readFully(is, lengthArray, 0, 4);
			int length = ByteArrayUtils.byteArrayToInt(lengthArray, 0, 4);
			byte[] data = new byte[length];
			StreamUtils.readFully(is, data, 0, length);
			return codec.decode(data);
		}

		private void sleep(long time) {
			try {
				Thread.sleep(time);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
	}
}
