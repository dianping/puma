package com.dianping.puma.api;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import com.dianping.cat.Cat;
import com.dianping.puma.api.config.GlobalConfig;
import com.dianping.puma.api.config.Config;
import com.dianping.puma.api.exception.PumaClientConnectException;
import com.dianping.puma.api.exception.PumaClientOnEventException;
import com.dianping.puma.api.manager.impl.DefaultHeartbeatManager;
import com.dianping.puma.api.manager.impl.DefaultHostManager;
import com.dianping.puma.api.manager.impl.DefaultPositionManager;
import com.dianping.puma.core.event.RowChangedEvent;
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

	private DefaultHeartbeatManager defaultHeartbeatManager = new DefaultHeartbeatManager();

	private DefaultHostManager defaultHostManager = new DefaultHostManager();

	private DefaultPositionManager defaultPositionManager = new DefaultPositionManager();

	private Thread subscribeThread;

	private GlobalConfig globalConfig;
	private Config config;

	public PumaClient(Configuration configuration) {

	}

	public void register(EventListener listener) {
		this.eventListener = listener;
	}

	public void start() {
		if (inited) {
			logger.warn("Puma client(%s) is already started.", name);
			return;
		}

		// Starting configurations.
		globalConfig.start();
		config.start();

		// Subscribe thread.
		if (subscribeThread != null) {
			subscribeThread.interrupt();
			subscribeThread = null;
		}
		subscribeThread = new Thread(new SubscribeTask(), String.format("subscribe-thread-%s", name));
		subscribeThread.start();

		// Managers.
		defaultHeartbeatManager.start();
		defaultHostManager.start();
		defaultPositionManager.start();

		inited = true;
	}

	public void stop() {
		if (!inited) {
			return;
		}
		inited = false;

		// Stopping managers.
		defaultHeartbeatManager.stop();
		defaultHostManager.stop();
		defaultPositionManager.stop();

		// Stopping subscribe thread.
		if (subscribeThread != null) {
			subscribeThread.interrupt();
			subscribeThread = null;
		}

		// Stopping configuration.
		//config.stop();
	}

	public void restartSubscribe() {
		if (subscribeThread != null) {
			subscribeThread.interrupt();
			subscribeThread = null;
		}
		subscribeThread = new Thread(new SubscribeTask(), String.format("subscribe-thread-%s", name));
		subscribeThread.start();
	}

	public String getName() {
		return name;
	}

	public DefaultHostManager getDefaultHostManager() {
		return defaultHostManager;
	}

	public DefaultPositionManager getDefaultPositionManager() {
		return defaultPositionManager;
	}

	private class SubscribeTask implements Runnable {

		private volatile boolean stopped = false;

		private HttpURLConnection connection = null;

		private InputStream is = null;

		public void stop() {
			stopped = true;
		}

		@Override
		public void run() {

			while (!checkStop()) {

				try {
					connect();
					defaultHostManager.feedback(DefaultHostManager.ConnectFeedback.CONNECT_ERROR);

					while (!checkStop()) {
						Event event = readEvent(is);
						defaultHostManager.feedback(DefaultHostManager.ConnectFeedback.CONNECT_ERROR);

						if (!checkStop() || event != null) {
							if (handleEvent(event)) {
								if (event instanceof RowChangedEvent) {
									defaultPositionManager.save(((RowChangedEvent) event).getBinlogInfo());
								}
							} else {
								String msg = String.format("Puma client(%s) on event error.", name);
								PumaClientOnEventException pe = new PumaClientOnEventException(msg);
								logger.error(msg, pe);
								Cat.logError(msg, pe);

								// Stop the client.
								stop();
								break;
							}
						}
					}

				} catch (IOException e) {
					if (!checkStop()) {
						defaultHostManager.feedback(DefaultHostManager.ConnectFeedback.CONNECT_ERROR);

						String msg = String.format("Puma client(%s) connect to server error.", name);
						PumaClientConnectException pe = new PumaClientConnectException(msg, e);
						logger.error(msg, pe);
						Cat.logError(msg, pe);

						//sleep(config.getReconnectSleepTime());
					}
				} finally {
					if (is != null) {
						try {
							is.close();
						} catch (IOException e) {
							String msg = String.format("Puma client(%s) close input stream error.", name);
							logger.warn(msg, e);
						}
					}
					if (connection != null) {
						connection.disconnect();
					}
				}
			}
		}

		private void connect() throws IOException {
			URL url = new URL("http://" + defaultHostManager.next() + "/puma/channel");

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
				defaultHeartbeatManager.heartbeat();
				return true;
			} else {
				RowChangedEvent row = (RowChangedEvent) event;

				try {
					eventListener.onEvent(row);
				} catch (Exception e) {
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
