package com.dianping.puma.core.netty.server;

import com.dianping.puma.core.netty.handler.HandlerFactory;

/**
 * Created by Dozer on 11/21/14.
 */
public class ServerConfig {

	private int port;

	private HandlerFactory handlerFactory;

	public HandlerFactory getHandlerFactory() {
		return handlerFactory;
	}

	public void setHandlerFactory(HandlerFactory handlerFactory) {
		this.handlerFactory = handlerFactory;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}
}
