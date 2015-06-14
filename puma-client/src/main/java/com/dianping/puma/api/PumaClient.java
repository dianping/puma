package com.dianping.puma.api;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import com.dianping.cat.Cat;
import com.dianping.lion.client.ConfigCache;
import com.dianping.puma.api.config.Config;
import com.dianping.puma.api.exception.PumaException;
import com.dianping.puma.api.manager.Feedback;
import com.dianping.puma.api.manager.HeartbeatManager;
import com.dianping.puma.api.manager.HostManager;
import com.dianping.puma.api.manager.PositionManager;
import com.dianping.puma.api.manager.impl.DefaultHeartbeatManager;
import com.dianping.puma.api.manager.impl.DefaultHostManager;
import com.dianping.puma.api.manager.impl.DefaultPositionManager;
import com.dianping.puma.api.util.Clock;
import com.dianping.puma.core.event.*;
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

	public PumaClient(Configuration configuration) {

	}

	public void register(EventListener listener) {
		this.eventListener = listener;
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
		DefaultHeartbeatManager defaultHeartbeatManager = new DefaultHeartbeatManager();
		defaultHeartbeatManager.setClient(this);
		defaultHeartbeatManager.setHostManager(hostManager);
		defaultHeartbeatManager.setConfig(config);
		defaultHeartbeatManager.setClock(clock);
		heartbeatManager = defaultHeartbeatManager;
		heartbeatManager.start();
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

	private void startSubscribe() {
		subscribeTask = new SubscribeTask();
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

	private void stopSubscribe() {
		subscribeTask.shutdown();
		subscribeThread.interrupt();
		subscribeThread = null;
	}

	public String getName() {
		return name;
	}

	public HostManager getHostManager() {
		return hostManager;
	}

	public PositionManager getPositionManager() {
		return positionManager;
	}

	public HeartbeatManager getHeartbeatManager() {
		return heartbeatManager;
	}

	public Config getConfig() {
		return config;
	}

	public Clock getClock() {
		return clock;
	}

	public ConfigCache getConfigCache() {
		return configCache;
	}

	private class SubscribeTask implements Runnable {

		private volatile boolean stopped = false;

		private boolean first = true;

		private HttpURLConnection connection = null;

		private InputStream is = null;

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

		private void connect() throws IOException {
			URL url = new URL("http://" + hostManager.next() + "/puma/channel");

			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			connection.setConnectTimeout(3000);
			connection.setDoOutput(true);
			connection.setUseCaches(false);
			connection.setRequestProperty("Connection", "Keep-Alive");
			connection.setRequestProperty("Cache-Control", "no-cache");

			PrintWriter out = new PrintWriter(connection.getOutputStream());
			String requestParams = configuration.buildRequestParamString(-3);
			out.print(requestParams);
			out.close();

			is = connection.getInputStream();
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
