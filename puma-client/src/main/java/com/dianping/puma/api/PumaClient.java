package com.dianping.puma.api;

import java.io.IOException;
import java.io.InputStream;

import com.dianping.cat.Cat;
import com.dianping.puma.api.manager.HostManager;
import com.dianping.puma.api.manager.PositionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.puma.api.sequence.FileSequenceHolder;
import com.dianping.puma.api.sequence.MemcachedSequenceHolder;
import com.dianping.puma.api.sequence.SequenceHolder;
import com.dianping.puma.core.codec.EventCodec;
import com.dianping.puma.core.codec.EventCodecFactory;
import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.event.Event;
import com.dianping.puma.core.event.HeartbeatEvent;
import com.dianping.puma.core.util.ByteArrayUtils;
import com.dianping.puma.core.util.PumaThreadUtils;
import com.dianping.puma.core.util.StreamUtils;

import static com.google.common.base.Preconditions.checkNotNull;

public class PumaClient {
	private static final Logger logger = LoggerFactory.getLogger(PumaClient.class);

	private String name;

	private boolean stopped = true;

	private Configuration config;
	private EventListener eventListener;
	// private volatile boolean active = false;
	private SequenceHolder sequenceHolder;
	private EventCodec codec;
	private Thread subscribeThread;
	private volatile boolean hasHeartbeat = false;
	private PumaClientTask pumaClientTask;
	private HeartbeatListener heartbeatListener;

	private InputStream is;

	private HostManager hostManager = new HostManager();

	private PositionManager positionManager = new PositionManager();

	public SequenceHolder getSeqFileHolder() {
		return sequenceHolder;
	}

	public PumaClient(Configuration configuration) {
		checkNotNull(configuration, "Puma client configuration is null");

		logger.info("Puma client configuration({}).", configuration.toString());

		configuration.validate();

		this.config = configuration;
		String seqBase = configuration.getSeqFileBase();
		if (seqBase != null && seqBase.equalsIgnoreCase("local")) {
			this.sequenceHolder = new FileSequenceHolder(configuration);
		} else {
			this.sequenceHolder = new MemcachedSequenceHolder(configuration);
		}

		codec = EventCodecFactory.createCodec(configuration.getCodecType());
		heartbeatListener = new HeartbeatListener(this);
	}

	public void register(EventListener listener) {
		this.eventListener = listener;
	}

	private void init() {
		hostManager.setName(name);
		hostManager.init();

		positionManager.setName(name);
		positionManager.init();
	}

	public void stop() {
		pumaClientTask.setActive(false);
		if (subscribeThread != null) {
			subscribeThread.interrupt();
			subscribeThread = null;
		}

		heartbeatListener.stop();
	}

	public void start() {
		config.validate();
		if (pumaClientTask != null) {
			pumaClientTask = null;
		}
		pumaClientTask = new PumaClientTask();
		subscribeThread = PumaThreadUtils.createThread(pumaClientTask, "PumaClientSub", false);
		subscribeThread.start();
		heartbeatListener.start();
	}

	public Configuration getConfig() {
		return config;
	}

	/*
	private InputStream connect() {
		try {
			final URL url = new URL(config.buildUrl());

			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			connection.setConnectTimeout(3000);
			connection.setDoOutput(true);
			connection.setUseCaches(false);
			connection.setRequestProperty("Connection", "Keep-Alive");
			connection.setRequestProperty("Cache-Control", "no-cache");

			PrintWriter out = new PrintWriter(connection.getOutputStream());

			String requestParams = config.buildRequestParamString(sequenceHolder.getSeq());

			out.print(requestParams);

			out.close();

			eventListener.onConnected();

			return connection.getInputStream();

		} catch (Exception e) {
			String msg = String.format("Puma client(%s) connecting failure.", config.getName());
			PumaClientConnectException pe = new PumaClientConnectException(msg, e);
			logger.error(msg, pe);
			Cat.logError(msg, pe);

			eventListener.onConnectException(pe);
		}

		return null;

	}*/

	private Event readEvent(InputStream is) throws IOException {
		byte[] lengthArray = new byte[4];
		StreamUtils.readFully(is, lengthArray, 0, 4);
		int length = ByteArrayUtils.byteArrayToInt(lengthArray, 0, 4);
		byte[] data = new byte[length];
		StreamUtils.readFully(is, data, 0, length);
		return codec.decode(data);
	}

	public void setHasHeartbeat(boolean hasHeartbeat) {
		this.hasHeartbeat = hasHeartbeat;
	}

	public boolean isHasHeartbeat() {
		return hasHeartbeat;
	}

	private class PumaClientTask implements Runnable {

		private volatile boolean active = false;

		public PumaClientTask() {
			this.active = true;
		}

		@Override
		public void run() {

			while (!stopped) {

				// Connect to server.
				try {
					connect();
				} catch (IOException e) {
					if (!stopped) {
						String msg = String.format("");
						logger.error(msg, e);
						Cat.logError(msg, e);

						// Thread sleep.
					}
				}

				// Read events.

			}

		}

		private void connect() throws IOException {

		}

		/*
		@Override
		public void run() {
			// reconnect while there is some connection problem
			while (true) {
				InputStream is = null;

				if (checkStop()) {
					logger.info("Puma client({}) stopped.", config.getName());
					break;
				}

				try {
					logger.info("Puma client({}) connecting...", config.getName());

					is = connect();

					// Client connect to server failure, sleep and reconnect.
					if (is == null) {
						Thread.sleep(10000);
						Cat.logEvent("Puma.state", "client:reconnect", "0", "");
						continue;
					}

					Cat.logEvent("Puma.state", "client:connected", "0", "");
					logger.info("Puma client({}) reading events...", config.getName());

					while (true) {
						if (checkStop()) {
							break;
						}

						Event event = readEvent(is);

						if (checkStop()) {
							break;
						}

						if (event instanceof HeartbeatEvent) {
							onHeartbeatEvent((HeartbeatEvent)event);
						} else {
							onChangedEvent((ChangedEvent)event);
						}
					}
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				} catch (Exception e) {
					if (checkStop()) {
						logger.info("Puma client({}) stopped.", config.getName());
						break;
					} else {
						String msg = String.format("Puma client(%s) reading events failure.", config.getName());
						PumaClientConnectException pe = new PumaClientConnectException(msg, e);

						logger.error(msg, pe);
						Cat.logError(msg, pe);

						eventListener.onConnectException(pe);

						try {
							Thread.sleep(10000);
						} catch (InterruptedException e1) {
							//
						}
					}
				} finally {
					if (is != null) {
						try {
							logger.info("Puma client({}) closing connection...", config.getName());

							is.close();
							Cat.logEvent("Puma.state", "client:close", "0", "");
						} catch (IOException e) {
							logger.warn("Puma client({}) closing connection failure.", config.getName());
						}
					}
				}
			}
		}*/

		private boolean checkStop() {
			if (!active) {
				logger.info("Puma client({}) checked active is false.", config.getName());
				return true;
			}
			if (Thread.currentThread().isInterrupted()) {
				logger.info("Puma client({}) checked thread is interrupted.", config.getName());
				return true;
			}

			return false;
		}

		private void onHeartbeatEvent(HeartbeatEvent event) {
			setHasHeartbeat(true);
			logger.info("Puma client[" + config.getName() + "] heartbeat.");
		}

		private void onChangedEvent(ChangedEvent event) {
			boolean listenerCallSuccess = true;
			// call event listener until success
			while (true) {
				if (checkStop()) {
					break;
				}
				try {
					eventListener.onEvent(event);
					listenerCallSuccess = true;
					break;
				} catch (Exception e) {
					logger.warn("Puma client[" + config.getName() + "] Exception occurs in eventListerner. Event: "
							+ event, e);
					if (eventListener.onException(event, e)) {
						logger.warn("Puma client[" + config.getName() + "] Event(" + event + ") skipped. ");
						eventListener.onSkipEvent(event);
						listenerCallSuccess = true;
						break;
					} else {
						listenerCallSuccess = false;
					}
				}
			}
			// Maybe not because of the success of last event
			// listener's run, but the stop command that system
			// received. We shouldn't save in this case.
			if (listenerCallSuccess) {
				sequenceHolder.saveSeq(event.getSeq());
			}
		}

		public void setActive(boolean active) {
			this.active = active;
		}

	}

}
