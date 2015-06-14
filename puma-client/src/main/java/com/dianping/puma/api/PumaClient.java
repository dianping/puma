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
import com.dianping.puma.core.event.RowChangedEvent;
import com.dianping.puma.core.event.ServerErrorEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.puma.core.codec.EventCodec;
import com.dianping.puma.core.event.Event;
import com.dianping.puma.core.event.HeartbeatEvent;
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
			logger.warn("Puma(%s) has been started repeatedly.", name);
			return;
		}

		// Start configuration.
		config = new Config();
		config.setClient(this);
		config.setConfigCache(configCache);
		config.start();

		// Start host manager.
		DefaultHostManager defaultHostManager = new DefaultHostManager();
		defaultHostManager.setClient(this);
		defaultHostManager.setConfigCache(configCache);
		hostManager = defaultHostManager;
		hostManager.start();

		// Start position manager.
		DefaultPositionManager defaultPositionManager = new DefaultPositionManager();
		defaultPositionManager.setClient(this);
		defaultPositionManager.setConfig(config);
		defaultPositionManager.setClock(clock);
		positionManager = defaultPositionManager;
		positionManager.start();

		// Start heartbeat manager.
		DefaultHeartbeatManager defaultHeartbeatManager = new DefaultHeartbeatManager();
		defaultHeartbeatManager.setClient(this);
		defaultHeartbeatManager.setHostManager(hostManager);
		defaultHeartbeatManager.setConfig(config);
		defaultHeartbeatManager.setClock(clock);
		heartbeatManager = defaultHeartbeatManager;
		heartbeatManager.start();

		// Start subscribe thread.
		subscribeTask = new SubscribeTask();
		subscribeThread = new Thread(subscribeTask);
		subscribeThread.setName(String.format("subscribe-thread-%s", name));
		subscribeThread.setDaemon(true);
		subscribeThread.start();

		inited = true;
		logger.info("Puma(%s) has been started successfully.", name);
	}

	public void stop() {
		if (!inited) {
			logger.warn("Puma(%s) has been stopped repeatedly.", name);
			return;
		}

		// Stop subscribe thread.
		subscribeTask.stop();
		subscribeThread.interrupt();
		subscribeThread = null;

		// Stop heartbeat manager.
		heartbeatManager.stop();

		// Stop position manager.
		positionManager.stop();

		// Stop host manager.
		hostManager.stop();

		// Stop configuration.
		config.stop();

		inited = false;
		logger.info("Puma(%s) has been stopped successfully.", name);
	}

	public void restartSubscribe() {
		if (inited) {
			// Stop subscribe task.
			subscribeTask.stop();
			subscribeThread.interrupt();
			subscribeThread = null;
		}

		// Start subscribe task.
		subscribeTask = new SubscribeTask();
		subscribeThread = new Thread(subscribeTask);
		subscribeThread.setName(String.format("subscribe-thread-%s", name));
		subscribeThread.setDaemon(true);
		subscribeThread.start();

		logger.info("Puma(%s) subscription has been restarted successfully.", name);
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

		private PumaClient client;

		private HttpURLConnection connection = null;

		private InputStream is = null;

		public void stop() {
			stopped = true;
		}

		@Override
		public void run() {

			hostManager.feedback(Feedback.INITIAL);

			while (!checkStop()) {

				try {
					connect();

					// Open the heartbeat manager after connection.
					heartbeatManager.open();

					while (!checkStop()) {
						Event event = readEvent(is);

						if (!checkStop() && event != null) {

							if (event instanceof HeartbeatEvent) {
								// Continue reading event.
								heartbeatManager.heartbeat();
								hostManager.feedback(Feedback.SUCCESS);
								continue;
							} else if (event instanceof ServerErrorEvent) {
								// Break reading event and reconnect.
								hostManager.feedback(Feedback.SERVER_ERROR);
								break;
							} else {
								RowChangedEvent row = (RowChangedEvent) event;

								try {
									eventListener.onEvent(row);
								} catch (Exception e) {
									if (eventListener.onException(row, e)) {
										// Continue reading event.
										hostManager.feedback(Feedback.SUCCESS);
										continue;
									} else {
										// Break reading event and stop puma.
										client.stop();
										break;
									}
								}
							}
						}
					}

				} catch (IOException e) {
					if (!checkStop()) {
						hostManager.feedback(Feedback.NET_ERROR);

						String msg = String.format("Puma(%s) connect to server error.", name);
						PumaException pe = new PumaException(name, hostManager.current(), msg, e);
						logger.error(msg, pe);
						Cat.logError(msg, pe);

						// Reconnect sleep.
						sleep(config.getReconnectSleepTime());
					}
				} finally {
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

		private boolean handleEvent(Event event) {
			if (event instanceof HeartbeatEvent) {
				heartbeatManager.heartbeat();
				hostManager.feedback(Feedback.SUCCESS);
				return true;
			} else if (event instanceof ServerErrorEvent) {
				hostManager.feedback(Feedback.SERVER_ERROR);
				return false;
			} else {
				RowChangedEvent row = (RowChangedEvent) event;

				try {
					eventListener.onEvent(row);
					hostManager.feedback(Feedback.SUCCESS);
				} catch (Exception e) {
					// @TODO: How to feedback if on event exception occurs?
					return eventListener.onException(row, e);
				}

				return true;
			}
		}

		private void sleep(long time) {
			try {
				Thread.sleep(time);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}

		private boolean checkStop() {
			return stopped || Thread.currentThread().isInterrupted();
		}
	}
}
