package com.dianping.puma.api;

import java.io.InputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.util.PumaThreadUtils;
import com.dianping.puma.core.util.TransportUtils;

public class PumaClient {

	private static final Logger	log		= Logger.getLogger(PumaClient.class);

	private Configuration		config;
	private EventListener		eventListener;
	private volatile boolean	stop	= true;
	private SeqFileHolder		seqFileHolder;

	public PumaClient(Configuration config) {
		this(config, new DefaultSeqFileHolder(config));
	}

	public PumaClient(Configuration config, SeqFileHolder seqFileHolder) {
		this.config = config;
		this.seqFileHolder = seqFileHolder;
	}

	public void subscribe(EventListener listener) {
		this.eventListener = listener;
	}

	public void stop() {
		stop = true;
	}

	public void start() {
		if (config == null) {
			throw new IllegalArgumentException("Puma client has no config.");
		}
		if (config.getHost() == null || config.getHost().trim().length() == 0) {
			throw new IllegalArgumentException("Puma client's host not set.");
		}
		if (eventListener == null) {
			throw new IllegalArgumentException("Puma client has no listener.");
		}
		Thread subscribeThread = PumaThreadUtils.createThread(new PumaClientTask(), "PumaClientSub", false);

		stop = false;
		subscribeThread.start();
	}

	private boolean checkStop() {
		if (stop) {
			log.info("Puma client stopped.");
			return true;
		}
		if (Thread.currentThread().isInterrupted()) {
			log.info("Puma client stopped since interrupted.");
			return true;
		}
		return false;
	}

	private InputStream connect() {

		try {
			final URL url = new URL("http://" + config.getHost() + ":" + config.getPort() + "/puma/channel");

			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			connection.setConnectTimeout(3000);
			connection.setDoOutput(true);

			// send the encoded message
			PrintWriter out = new PrintWriter(connection.getOutputStream());

			out.print(URLEncoder.encode("seq", "UTF-8") + "="
					+ URLEncoder.encode(String.valueOf(seqFileHolder.getSeq()), "UTF-8"));
			out.print(URLEncoder.encode("ddl", "UTF-8") + "="
					+ URLEncoder.encode(Boolean.toString(config.isNeedDdl()), "UTF-8"));
			out.print(URLEncoder.encode("dml", "UTF-8") + "="
					+ URLEncoder.encode(Boolean.toString(config.isNeedDml()), "UTF-8"));
			out.print(URLEncoder.encode("ts", "UTF-8") + "="
					+ URLEncoder.encode(Boolean.toString(config.isNeedTransactionInfo()), "UTF-8"));

			for (Map.Entry<String, List<String>> entry : config.getDatabaseTablesMapping().entrySet()) {
				for (String tb : entry.getValue()) {
					out.print(URLEncoder.encode("dt", "UTF-8") + "="
							+ URLEncoder.encode(entry.getKey() + "." + tb, "UTF-8"));
				}
			}

			out.close();

			return connection.getInputStream();

		} catch (Exception ex) {
			log.error("Connect to puma server failed. " + config, ex);
		}

		return null;

	}

	private class PumaClientTask implements Runnable {

		@Override
		public void run() {

			// reconnect while there is some connection problem
			while (true) {
				if (checkStop()) {
					break;
				}

				try {

					InputStream is = connect();

					// reconnect case
					if (is == null) {
						Thread.sleep(100);
						log.info("Puma client reconnecting...");
						continue;
					}

					// loop read event from the input stream
					while (true) {
						if (checkStop()) {
							break;
						}

						ChangedEvent event = TransportUtils.read(is);
						boolean listenerCallSuccess = true;

						// call event listener until success
						while (true) {
							if (checkStop()) {
								break;
							}

							try {
								eventListener.onEvent(event);
								break;
							} catch (Exception e) {
								log.error("Exception occurs in eventListerner.", e);
								listenerCallSuccess = false;
							}
						}

						// Maybe not because of the success of last event
						// listener's run, but the stop command that system
						// received. We shouldn't save in this case.
						if (listenerCallSuccess) {
							seqFileHolder.saveSeq(event.getSeq());
						}
					}

				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				} catch (Exception e) {
					log.error("Connection problem occurs.", e);
					log.info("Puma client reconnecting...");
				}
			}
		}
	}
}
