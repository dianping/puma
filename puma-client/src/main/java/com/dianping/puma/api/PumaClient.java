package com.dianping.puma.api;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.puma.api.sequence.FileSequenceHolder;
import com.dianping.puma.api.sequence.MemcachedSequenceHolder;
import com.dianping.puma.api.sequence.SequenceHolder;
import com.dianping.puma.core.codec.EventCodec;
import com.dianping.puma.core.codec.EventCodecFactory;
import com.dianping.puma.core.event.Event;
import com.dianping.puma.core.event.HeartbeatEvent;
import com.dianping.puma.core.util.ByteArrayUtils;
import com.dianping.puma.core.util.PumaThreadUtils;
import com.dianping.puma.core.util.StreamUtils;

public class PumaClient {
	private static final Logger log = LoggerFactory.getLogger(PumaClient.class);
	private Configuration config;
	private EventListener eventListener;
	// private volatile boolean active = false;
	private SequenceHolder sequenceHolder;
	private EventCodec codec;
	private Thread subscribeThread;
	private volatile boolean hasHeartbeat = false;
	private PumaClientTask pumaClientTask;
	private HeartbeatListener heartbeatListener;

	public SequenceHolder getSeqFileHolder() {
		return sequenceHolder;
	}

	public PumaClient(Configuration config) {
		if (config == null) {
			throw new IllegalArgumentException("Config can't be null!");
		}

		this.config = config;
		String seqBase = config.getSeqFileBase();
		if (seqBase != null && seqBase.equalsIgnoreCase("memcached")) {
			this.sequenceHolder = new MemcachedSequenceHolder(config);
		} else {
			this.sequenceHolder = new FileSequenceHolder(config);
		}
		codec = EventCodecFactory.createCodec(config.getCodecType());
		heartbeatListener = new HeartbeatListener(this);
	}

	public void register(EventListener listener) {
		this.eventListener = listener;
	}

	public void stop() {
		pumaClientTask.setActive(false);
		if (subscribeThread != null) {
			subscribeThread.interrupt();
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

		} catch (Exception ex) {
			log.error("Puma client[" + config.getName() + "] Connect to puma server failed. " + config, ex);
			eventListener.onConnectException(ex);
		}

		return null;

	}

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
			// reconnect while there is some connection problem
			while (true) {
				InputStream is = null;
				if (checkStop()) {
					log.info("Puma client[" + config.getName() + "] stopped.");
					break;
				}
				try {
					is = connect();
					// reconnect case
					if (is == null) {
						Thread.sleep(100);
						log.info("Puma client[" + config.getName() + "] reconnecting...");
						continue;
					}
					log.info("Puma client[" + config.getName() + "] connected.");
					// loop read event from the input stream
					while (true) {
						if (checkStop()) {
							break;
						}
						Event event = readEvent(is);
						if (event instanceof HeartbeatEvent) {
							onHeartbeatEvent(event);
							continue;
						} else {
							onChangedEvent(event);
						}
					}
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				} catch (Exception e) {
					log.warn("Puma client[" + config.getName() + "] Connection problem occurs." + e);
					log.warn("Puma client[" + config.getName() + "] reconnecting...");
					eventListener.onConnectException(e);
				} finally {
					if (is != null) {
						try {
							is.close();
						} catch (IOException e) {
							// ignore
						}
					}
				}
			}
		}

		private boolean checkStop() {
			if (!active) {
				log.info("Puma client[" + config.getName() + "] checked active is false.");
				return true;
			}
			if (Thread.currentThread().isInterrupted()) {
				log.info("Puma client[" + config.getName() + "] checked thread is interrupted.");
				return true;
			}

			return false;
		}

		private void onHeartbeatEvent(Event event) {
			setHasHeartbeat(true);
			eventListener.onHeartbeatEvent(event);
			log.info("Puma client[" + config.getName() + "] heartbeat.");
		}

		private void onChangedEvent(Event event) {
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
					log.warn("Puma client[" + config.getName() + "] Exception occurs in eventListerner. Event: "
							+ event, e);
					if (eventListener.onException(event, e)) {
						log.warn("Puma client[" + config.getName() + "] Event(" + event + ") skipped. ");
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
