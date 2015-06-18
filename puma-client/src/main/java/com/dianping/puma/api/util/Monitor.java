package com.dianping.puma.api.util;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Message;
import com.dianping.puma.api.PumaClient;
import com.dianping.puma.api.exception.PumaException;
import org.slf4j.Logger;

public class Monitor {

	private PumaClient client;

	public void logError(Logger logger, String cause) {
		String msg = genMsgHead(client.getName())+ cause;
		PumaException pe = new PumaException(msg);
		logger.error(msg, pe);
		Cat.logError(msg, pe);
	}

	public void logError(Logger logger, String cause, Throwable e) {
		String msg = genMsgHead(client.getName())+ cause;
		PumaException pe = new PumaException(msg, e);
		logger.error(msg, pe);
		Cat.logError(msg, pe);
	}

	public void logError(Logger logger, String serverHost, String cause) {
		String msg = genMsgHead(client.getName(), serverHost)+ cause;
		PumaException pe = new PumaException(msg);
		logger.error(msg, pe);
		Cat.logError(msg, pe);
	}

	public void logError(Logger logger, String serverHost, String cause, Throwable e) {
		String msg = genMsgHead(client.getName(), serverHost)+ cause;
		PumaException pe = new PumaException(msg, e);
		logger.error(msg, pe);
		Cat.logError(msg, pe);
	}

	public void logInfo(Logger logger, String info) {
		String msg = genMsgHead(client.getName()) + info;
		logger.info(msg);
		Cat.logEvent("Puma", msg, Message.SUCCESS, "");
	}

	public void logInfo(Logger logger, String serverHost, String info) {
		String msg = genMsgHead(client.getName(), serverHost) + info;
		logger.info(msg);
		Cat.logEvent("Puma", msg, Message.SUCCESS, "");
	}

	private String genMsgHead(String clientName) {
		return String.format("[client: %s] ", clientName);
	}

	private String genMsgHead(String clientName, String serverHost) {
		return String.format("[client: %s][server: %s] ", clientName, serverHost);
	}

	public void setClient(PumaClient client) {
		this.client = client;
	}
}
