package com.dianping.puma.api;

public class NettyPumaConnectorTest {

	public static void main(String[] args) {
		String remoteIp = "127.0.0.1";
		int remotePort = 4040;
		int localPort  = 4050;
		PumaConnector pumaConnector = new NettyPumaConnector(remoteIp, remotePort, localPort);

		pumaConnector.connect();
	}
}
