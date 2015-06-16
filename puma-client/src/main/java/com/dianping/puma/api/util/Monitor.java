package com.dianping.puma.api.util;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Message;
import com.dianping.puma.api.PumaClient;
import com.dianping.puma.api.exception.PumaException;
import com.dianping.puma.api.manager.HostManager;
import org.slf4j.Logger;

public class Monitor {

	private PumaClient client;

	private HostManager hostManager;

	public void logError(Logger logger, String cause) {
		String msg = genMsgHead(client.getName(), hostManager.current())+ cause;
		PumaException pe = new PumaException(msg);
		logger.error(msg, pe);
		Cat.logError(msg, pe);
	}

	public void logError(Logger logger, String cause, Throwable e) {
		String msg = genMsgHead(client.getName(), hostManager.current())+ cause;
		PumaException pe = new PumaException(msg, e);
		logger.error(msg, pe);
		Cat.logError(msg, pe);
	}

	public void logInfo(Logger logger, String info) {
		String msg = genMsgHead(client.getName(), hostManager.current()) + info;
		logger.info(msg);
		Cat.logEvent("Puma", msg, Message.SUCCESS, "");
	}

	private String genMsgHead(String clientName, String serverHost) {
		return String.format("[client: %s][server: %s] ", clientName, serverHost);
	}

	public void setClient(PumaClient client) {
		this.client = client;
	}

	public void setHostManager(HostManager hostManager) {
		this.hostManager = hostManager;
	}
}
