package com.dianping.puma.api;

import com.dianping.puma.api.handler.ConnectedHandler;
import com.dianping.puma.core.netty.client.ClientConfig;
import com.dianping.puma.core.netty.client.TcpClient;
import com.dianping.puma.core.netty.entity.BinlogMessage;
import com.dianping.puma.core.netty.entity.BinlogQuery;
import com.dianping.puma.core.netty.exception.PumaClientException;
import com.dianping.puma.core.netty.handler.HandlerFactory;
import io.netty.channel.ChannelHandler;
import io.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class NettyPumaConnector implements PumaConnector {

	private static final Logger logger = LoggerFactory.getLogger(NettyPumaConnector.class);

	private String clientName;

	private String remoteIp;
	private int remotePort;
	private int localPort;
	private volatile boolean connecting = false;

	private TcpClient client;
	private ConnectedHandler connectedHandler = new ConnectedHandler();

	public NettyPumaConnector(String clientName, String remoteIp, int remotePort, int localPort) {
		this.clientName = clientName;
		this.remoteIp = remoteIp;
		this.remotePort = remotePort;
		this.localPort = localPort;
	}

	@Override
	public void connect() throws PumaClientException {
	}

	private void doConnect() throws PumaClientException {
		ClientConfig config = new ClientConfig();
		config.setRemoteIp(remoteIp);
		config.setRemotePort(remotePort);
		config.setLocalPort(localPort);
		config.setHandlerFactory(new HandlerFactory() {
			@Override
			public Map<String, ChannelHandler> getHandlers() {
				Map<String, ChannelHandler> handlers = new LinkedHashMap<String, ChannelHandler>();
				handlers.put("connectedHandler", connectedHandler);
				handlers.put("HttpRequestDecoder", new HttpRequestDecoder());
				handlers.put("HttpContentDecompressor", new HttpContentDecompressor());
				handlers.put("HttpResponseEncoder", new HttpResponseEncoder());
				handlers.put("HttpContentCompressor", new HttpContentCompressor());
				handlers.put("HttpObjectAggregator", new HttpObjectAggregator(1024 * 1024 * 32));
				return handlers;
			}
		});

		client = new TcpClient(config);
		client.init();
	}

	@Override
	public void disconnect() throws PumaClientException {
	}

	private void doDisconnect() throws PumaClientException {
	}

	@Override
	public BinlogMessage get(int batchSize) throws PumaClientException {
		return null;
	}

	@Override
	public BinlogMessage get(int batchSize, long timeout, TimeUnit timeUnit) throws PumaClientException {
		return null;
	}

	@Override
	public BinlogMessage getWithoutAck(int batchSize) throws PumaClientException {
		return null;
	}

	@Override
	public BinlogMessage getWithoutAck(int batchSize, long timeout, TimeUnit timeUnit) throws PumaClientException {
		return null;
	}

	@Override
	public void ack(long batchId) throws PumaClientException {

	}

	@Override
	public void rollback(long batchId) throws PumaClientException {

	}

	@Override
	public void rollback() throws PumaClientException {

	}
}
