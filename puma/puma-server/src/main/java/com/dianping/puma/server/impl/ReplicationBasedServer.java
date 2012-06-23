/**
 * Project: ${puma-server.aid}
 * 
 * File Created at 2012-6-6 $Id$
 * 
 * Copyright 2010 dianping.com. All rights reserved.
 * 
 * This software is the confidential and proprietary information of Dianping
 * Company. ("Confidential Information"). You shall not disclose such
 * Confidential Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with dianping.com.
 */
package com.dianping.puma.server.impl;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;

import org.apache.log4j.Logger;

import com.dianping.puma.common.bo.PositionInfo;
import com.dianping.puma.common.util.PositionFileUtils;
import com.dianping.puma.server.mysql.packet.AuthenticatePacket;
import com.dianping.puma.server.mysql.packet.ComBinlogDumpPacket;
import com.dianping.puma.server.mysql.packet.OKErrorPacket;
import com.dianping.puma.server.mysql.packet.PacketFactory;
import com.dianping.puma.server.mysql.packet.PacketType;

/**
 * 基于MySQL复制机制的Server
 * 
 * @author Leo Liang
 * 
 */
public class ReplicationBasedServer extends AbstractServer {
	private static final Logger	log			= Logger.getLogger(ReplicationBasedServer.class);
	protected int				port		= 3306;
	protected String			host;
	protected String			user;
	protected String			password;
	protected String			database;
	protected long				serverId	= 6789;
	protected String			encoding	= "utf-8";
	// add socket
	protected PumaSocketWrapper	pumaSocket;
	protected volatile boolean	stop		= false;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.puma.server.Server#start()
	 */
	@Override
	public void start() throws Exception {

		do {
			try {
				// 读position/file文件
				PositionInfo posInfo = PositionFileUtils.getPositionInfo(this.getServerName(),
						context.getBinlogFileName(), context.getBinlogStartPos());

				context.setBinlogFileName(posInfo.getBinlogFileName());
				context.setBinlogStartPos(posInfo.getBinlogPosition());
				context.setServerId(serverId);
				CountDownLatch latch = new CountDownLatch(1);

				this.pumaSocket = new PumaSocketWrapper(new Socket(host, port));

				// connect
				PacketFactory.parsePacket(pumaSocket.getInputStream(), PacketType.CONNECT_PACKET, context);

				// auth
				AuthenticatePacket authPacket = (AuthenticatePacket) PacketFactory.createCommandPacket(
						PacketType.AUTHENTICATE_PACKET, context);

				authPacket.setPassword(password);
				authPacket.setUser(user);
				authPacket.setDatabase(database);
				authPacket.buildPacket(context);
				authPacket.write(pumaSocket.getOutputStream(), context);

				OKErrorPacket okErrorPacket = (OKErrorPacket) PacketFactory.parsePacket(pumaSocket.getInputStream(),
						PacketType.OKERROR_PACKET, context);

				if (okErrorPacket.isOk()) {
					log.info("Server logined... serverId: " + serverId + " host: " + host + " port: " + port
							+ " user: " + user + " database: " + database);

					ComBinlogDumpPacket dumpBinlogPacket = (ComBinlogDumpPacket) PacketFactory.createCommandPacket(
							PacketType.COM_BINLOG_DUMP_PACKET, context);
					dumpBinlogPacket.setBinlogFileName(context.getBinlogFileName());
					dumpBinlogPacket.setBinlogFlag(0);
					dumpBinlogPacket.setBinlogPosition(context.getBinlogStartPos());
					dumpBinlogPacket.setServerId(serverId);
					dumpBinlogPacket.buildPacket(context);

					dumpBinlogPacket.write(pumaSocket.getOutputStream(), context);

					OKErrorPacket dumpCommandResultPacket = (OKErrorPacket) PacketFactory.parsePacket(
							pumaSocket.getInputStream(), PacketType.OKERROR_PACKET, context);

					if (dumpCommandResultPacket.isOk()) {
						log.info("Dump binlog command success.");
					} else {
						log.error("Dump binlog failed. Reason: " + dumpCommandResultPacket.getMessage());
					}

					latch.await();

				} else {
					log.error("Login failed. Reason: " + okErrorPacket.getMessage());
				}
			} catch (IOException e) {
				log.error("IOException occurs. serverId: " + serverId);
				try {
					this.pumaSocket.close();
				} catch (IOException closeException) {

				}
			}
		} while (!stop);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.puma.server.Server#stop()
	 */
	@Override
	public void stop() throws Exception {
		try {
			if (this.pumaSocket != null) {
				this.pumaSocket.close();
			}
		} catch (IOException ioEx) {
			// TODO log
			this.pumaSocket = null;
		}
		this.stop = true;

	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public long getServerId() {
		return serverId;
	}

	public void setServerId(long serverId) {
		this.serverId = serverId;
	}

	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.puma.server.Server#getServerName()
	 */
	@Override
	public String getServerName() {
		return String.valueOf(serverId);
	}

	private static class PumaSocketWrapper extends Socket {
		private Socket	socket;

		public PumaSocketWrapper(Socket socket) {
			this.socket = socket;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.net.Socket#getInputStream()
		 */
		@Override
		public InputStream getInputStream() throws IOException {
			return new BufferedInputStream(socket.getInputStream());
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.net.Socket#getOutputStream()
		 */
		@Override
		public OutputStream getOutputStream() throws IOException {
			return new BufferedOutputStream(socket.getOutputStream());
		}

	}
}
