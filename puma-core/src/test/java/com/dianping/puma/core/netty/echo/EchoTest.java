package com.dianping.puma.core.netty.echo;

import com.dianping.puma.core.netty.client.ClientConfig;
import com.dianping.puma.core.netty.client.TcpClient;
import com.dianping.puma.core.netty.handler.HandlerFactory;
import com.dianping.puma.core.netty.server.ServerConfig;
import com.dianping.puma.core.netty.server.TcpServer;
import io.netty.channel.ChannelHandler;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class EchoTest {

	static final int LOCAL_PORT = 10000;
	static final int REMOTE_PORT = 5050;

	public static void main(String[] args) {
		startEchoServer();

		try {
			TimeUnit.SECONDS.sleep(5);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		startEchoClient();
	}

	private static void startEchoServer() {
		ServerConfig serverConfig = new ServerConfig();
		serverConfig.setPort(REMOTE_PORT);
		serverConfig.setHandlerFactory(new HandlerFactory() {
			@Override
			public Map<String, ChannelHandler> getHandlers() {
				Map<String, ChannelHandler> handlers = new LinkedHashMap<String, ChannelHandler>();
				handlers.put("FrameDecoder", new LineBasedFrameDecoder(32768));
				handlers.put("StringEncoder", new StringEncoder());
				handlers.put("StringDecoder", new StringDecoder());
				handlers.put("EchoServerHandler", new EchoServerHandler());
				return handlers;
			}
		});

		TcpServer server = new TcpServer(serverConfig);
		server.init();

		System.out.println(String.format("[%s] start echo server", EchoTest.class));
	}

	private static void startEchoClient() {
		ClientConfig clientConfig = new ClientConfig();
		clientConfig.setRemoteIp("127.0.0.1");
		clientConfig.setRemotePort(REMOTE_PORT);
		clientConfig.setLocalPort(LOCAL_PORT);
		clientConfig.setHandlerFactory(new HandlerFactory() {
			@Override
			public Map<String, ChannelHandler> getHandlers() {
				Map<String, ChannelHandler> handlers = new LinkedHashMap<String, ChannelHandler>();
				handlers.put("FrameDecoder", new LineBasedFrameDecoder(32768));
				handlers.put("StringEncoder", new StringEncoder());
				handlers.put("StringDecoder", new StringDecoder());
				handlers.put("EchoClientHandler", new EchoClientHandler());
				return handlers;
			}
		});

		TcpClient client = new TcpClient(clientConfig);
		client.init();

		System.out.println(String.format("[%s] start echo client", EchoTest.class));
	}
}
