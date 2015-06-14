package com.dianping.puma.api;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import com.dianping.cat.Cat;
import com.dianping.lion.client.ConfigCache;
import com.dianping.puma.api.config.Config;
import com.dianping.puma.api.exception.AuthException;
import com.dianping.puma.api.exception.PumaException;
import com.dianping.puma.api.manager.*;
import com.dianping.puma.api.manager.impl.DefaultHeartbeatManager;
import com.dianping.puma.api.manager.impl.DefaultHostManager;
import com.dianping.puma.api.manager.impl.DefaultLockManager;
import com.dianping.puma.api.manager.impl.DefaultPositionManager;
import com.dianping.puma.api.util.Clock;
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

	private volatile boolean inited = true;

	private String name;

	private Configuration configuration;
	private EventListener eventListener;
	private EventCodec codec;

	private SubscribeTask subscribeTask;
	private Thread subscribeThread;

	private ConfigCache configCache = ConfigCache.getInstance();
	private Clock clock = new Clock();

	private Config config;
	private HostManager hostManager;
	private PositionManager positionManager;
	private HeartbeatManager heartbeatManager;
	private LockManager lockManager;

	public PumaClient(Configuration configuration) {
	}

	public void register(EventListener listener) {
		this.eventListener = listener;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void start() {
		if (inited) {
			logger.warn("Puma(%s) has been started already.", name);
			return;
		}

		startConfig();
		startHostManager();
		startPositionManager();
		startHeartbeatManager();
		startLockManager();
		startSubscribe();

		inited = true;
		logger.info("Puma(%s) has been started successfully.", name);
	}

	public void stop() {
		if (!inited) {
			logger.warn("Puma(%s) has been stopped already.", name);
			return;
		}

		stopSubscribe();
		stopLockManager();
		stopHeartbeatManager();
		stopPositionManager();
		stopHostManager();
		stopConfig();

		inited = false;
		logger.info("Puma(%s) has been stopped successfully.", name);
	}

	private void startConfig() {
		config = new Config();
		config.setClient(this);
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
		defaultPositionManager.setConfig(config);
		defaultPositionManager.setClock(clock);
		positionManager = defaultPositionManager;
		positionManager.start();
	}

	private void startHeartbeatManager() {
		DefaultHeartbeatManager defaultHeartbeatManager = new DefaultHeartbeatManager();
		defaultHeartbeatManager.setClient(this);
		defaultHeartbeatManager.setHostManager(hostManager);
		defaultHeartbeatManager.setConfig(config);
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

	private void startSubscribe() {
		subscribeTask = new SubscribeTask();
		subscribeTask.setConfig(config);
		subscribeTask.setHostManager(hostManager);
		subscribeTask.setPositionManager(positionManager);
		subscribeTask.setHeartbeatManager(heartbeatManager);
		subscribeTask.setLockManager(lockManager);
		subscribeThread = new Thread(subscribeTask);
		subscribeThread.setName(String.format("subscribe-thread-%s", name));
		subscribeThread.setDaemon(true);
		subscribeThread.start();
	}

	private void stopConfig() {
		config.stop();
		config = null;
	}

	private void stopHostManager() {
		hostManager.stop();
		hostManager = null;
	}

	private void stopPositionManager() {
		positionManager.stop();
		positionManager = null;
	}

	private void stopHeartbeatManager() {
		heartbeatManager.stop();
		heartbeatManager = null;
	}

	private void stopLockManager() {
		lockManager.stop();
		lockManager = null;
	}

	private void stopSubscribe() {
		subscribeTask.shutdown();
		subscribeThread.interrupt();
		subscribeThread = null;
	}

	public String getName() {
		return name;
	}

	private class SubscribeTask implements Runnable {

		private volatile boolean stopped = false;

		private boolean first = true;
		private HttpURLConnection connection = null;
		private InputStream is = null;

		private Config config;
		private HostManager hostManager;
		private PositionManager positionManager;
		private HeartbeatManager heartbeatManager;
		private LockManager lockManager;

		@Override
		public void run() {

			while (!isShutdown()) {

				try {
					if (!first) {
						// Sleep for a while if reconnection.
						sleep(config.getReconnectSleepTime());
					}
					first = false;
					connect();

					// Open heartbeat manager after connection.
					heartbeatManager.open();

					while (!isShutdown()) {
						Event event = readEvent(is);

						if (!isShutdown() && event != null) {

							// Got event!
							heartbeatManager.heartbeat();

							// Changed event, handle it.
							// If no exception occurs or exception can be handled, keep on reading events.
							// If exception can't be handled, stop the entire client.
							if (event instanceof ChangedEvent) {
								hostManager.feedback(Feedback.SUCCESS);

								ChangedEvent changedEvent = (ChangedEvent) event;
								try {
									eventListener.onEvent(changedEvent);
								} catch (Exception e) {
									if (eventListener.onException(changedEvent, e)) {
										positionManager.feedback(changedEvent.getBinlogInfo());
										continue;
									} else {
										stop();
										break;
									}
								}
							}

							// Heartbeat event, pass it and keep on reading events.
							if (event instanceof HeartbeatEvent) {
								hostManager.feedback(Feedback.SUCCESS);
								continue;
							}

							// Server error event, reconnect.
							if (event instanceof ServerErrorEvent) {
								hostManager.feedback(Feedback.SERVER_ERROR);
								break;
							}
						}
					}

				} catch (IOException e) {
					if (!isShutdown()) {
						// Feeds back network error after connection failure.
						hostManager.feedback(Feedback.NET_ERROR);

						String msg = "Connection to server failure.";
						PumaException pe = new PumaException(name, hostManager.current(), msg, e);
						logger.error(msg, pe);
						Cat.logError(msg, pe);
					}
				} finally {
					// Close heartbeat manager here.
					heartbeatManager.close();

					if (is != null) {
						try {
							is.close();
							logger.info("Puma({}) close input stream successfully.", name);
						} catch (IOException e) {
							logger.warn("Puma({}) close input stream failure.", name, e);
						}
					}
					if (connection != null) {
						connection.disconnect();
						logger.info("Puma({}) close connection successfully.", name);
					}
				}
			}
		}

		public void shutdown() {
			heartbeatManager.close();
			stopped = true;
		}

		private boolean isShutdown() {
			return stopped || Thread.currentThread().isInterrupted();
		}

		private void connect() throws IOException, AuthException {
			if (!lockManager.tryLock()) {
				throw new AuthException("Puma locks connection failure.");
			}

			logger.info("Puma({}) locks connection successfully.");

			String host = hostManager.next();
			URL url = new URL("http://" + host + "/puma/channel");

			logger.info("Puma({}) connects to host: {}.", name, host);

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

			logger.info("Puma({}) connection configuration: {}.", name, requestParams);

			is = connection.getInputStream();
		}

		private String buildRequestParamString() {
			BinlogInfo binlogInfo = positionManager.next();

			StringBuilder builder = (new StringBuilder())
					.append("seq=").append(-3)
					.append("&binlog=").append(binlogInfo.getBinlogFile())
					.append("&binlogPos=").append(binlogInfo.getBinlogPosition())
					.append("&serverId=").append(config.getServerId())
					.append("&name=").append(name)
					.append("&target=").append(config.getTarget())
					.append("&dml=").append(config.getDml())
					.append("&ddl=").append(config.getDdl())
					.append("&ts=").append(config.getTransaction())
					.append("&codec=").append(config.getCodecType());

			for (String table: config.getTables()) {
				builder.append("&dt=").append(config.getSchema()).append(".").append(table);
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

		public void setHostManager(HostManager hostManager) {
			this.hostManager = hostManager;
		}

		public void setPositionManager(PositionManager positionManager) {
			this.positionManager = positionManager;
		}

		public void setHeartbeatManager(HeartbeatManager heartbeatManager) {
			this.heartbeatManager = heartbeatManager;
		}

		public void setLockManager(LockManager lockManager) {
			this.lockManager = lockManager;
		}

		public void setConfig(Config config) {
			this.config = config;
		}
	}
}
