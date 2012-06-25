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
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.CountDownLatch;

import org.apache.log4j.Logger;

import com.dianping.puma.common.bo.PositionInfo;
import com.dianping.puma.common.mysql.event.BinlogEvent;
import com.dianping.puma.common.mysql.packet.AuthenticatePacket;
import com.dianping.puma.common.mysql.packet.BinlogPacket;
import com.dianping.puma.common.mysql.packet.ComBinlogDumpPacket;
import com.dianping.puma.common.mysql.packet.OKErrorPacket;
import com.dianping.puma.common.mysql.packet.PacketFactory;
import com.dianping.puma.common.mysql.packet.PacketType;
import com.dianping.puma.common.util.PositionFileUtils;
import com.dianping.puma.parser.Parser;

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
	protected Socket			pumaSocket;
	protected InputStream		is;
	protected OutputStream		os;
	protected volatile boolean	stop		= false;

	protected Parser			parser;

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

				connect();

				if (auth()) {
					log.info("Server logined... serverId: " + serverId + " host: " + host + " port: " + port
							+ " user: " + user + " database: " + database);

					if (dumpBinlog()) {
						log.info("Dump binlog command success.");

						readBinlog();

					} else {
						throw new IOException("Dump binlog failed.");
					}

					latch.await();

				} else {
					throw new IOException("Login failed.");
				}
			} catch (IOException e) {
				log.error("IOException occurs. serverId: " + serverId, e);
				try {
					this.pumaSocket.close();
				} catch (IOException closeException) {

				}
			}
		} while (!stop);

	}

	private void readBinlog() throws IOException {
		while (!stop) {
			BinlogPacket binlogPacket = (BinlogPacket) PacketFactory.parsePacket(is, PacketType.BINLOG_PACKET, context);
			if (!binlogPacket.isOk()) {
				log.error("Binlog packet response error.");
				throw new IOException("Binlog packet response error.");
			} else {
				BinlogEvent event = parser.parse(binlogPacket.getBinlogBuf(), context);
				System.out.println(event.getHeader().getNextPosition());
				System.out.println(event);
			}

		}
	}

	/**
	 * Connect to mysql master and parse the greeting packet
	 * 
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	private void connect() throws UnknownHostException, IOException {
		closeTransport();
		this.pumaSocket = new Socket();
		this.pumaSocket.setTcpNoDelay(false);
		this.pumaSocket.setKeepAlive(true);
		this.pumaSocket.connect(new InetSocketAddress(host, port));
		is = new BufferedInputStream(pumaSocket.getInputStream());
		os = new BufferedOutputStream(pumaSocket.getOutputStream());
		PacketFactory.parsePacket(is, PacketType.CONNECT_PACKET, context);
	}

	/**
	 * Send COM_BINLOG_DUMP packet to mysql master and parse the response
	 * 
	 * @return
	 * @throws IOException
	 */
	private boolean dumpBinlog() throws IOException {
		ComBinlogDumpPacket dumpBinlogPacket = (ComBinlogDumpPacket) PacketFactory.createCommandPacket(
				PacketType.COM_BINLOG_DUMP_PACKET, context);
		dumpBinlogPacket.setBinlogFileName(context.getBinlogFileName());
		dumpBinlogPacket.setBinlogFlag(0);
		dumpBinlogPacket.setBinlogPosition(context.getBinlogStartPos());
		dumpBinlogPacket.setServerId(serverId);
		dumpBinlogPacket.buildPacket(context);

		dumpBinlogPacket.write(os, context);

		OKErrorPacket dumpCommandResultPacket = (OKErrorPacket) PacketFactory.parsePacket(is,
				PacketType.OKERROR_PACKET, context);
		if (dumpCommandResultPacket.isOk()) {
			return true;
		} else {
			log.error("Dump binlog failed. Reason: " + dumpCommandResultPacket.getMessage());
			return false;
		}
	}

	/**
	 * Send Authentication Packet to mysql master and parse the response
	 * 
	 * @return
	 * @throws IOException
	 */
	private boolean auth() throws IOException {
		// auth
		AuthenticatePacket authPacket = (AuthenticatePacket) PacketFactory.createCommandPacket(
				PacketType.AUTHENTICATE_PACKET, context);

		authPacket.setPassword(password);
		authPacket.setUser(user);
		authPacket.setDatabase(database);
		authPacket.buildPacket(context);
		authPacket.write(os, context);

		OKErrorPacket okErrorPacket = (OKErrorPacket) PacketFactory.parsePacket(is, PacketType.OKERROR_PACKET, context);

		if (okErrorPacket.isOk()) {
			return true;
		} else {
			log.error("Login failed. Reason: " + okErrorPacket.getMessage());
			return false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.puma.server.Server#stop()
	 */
	@Override
	public void stop() throws Exception {
		closeTransport();
		this.stop = true;

	}

	/**
	 * 
	 */
	private void closeTransport() {
		try {
			if (this.is != null) {
				this.is.close();
			}
		} catch (IOException ioEx) {
			// TODO log
			this.is = null;
		}
		try {
			if (this.os != null) {
				this.os.close();
			}
		} catch (IOException ioEx) {
			// TODO log
			this.os = null;
		}
		try {
			if (this.pumaSocket != null) {
				this.pumaSocket.close();
			}
		} catch (IOException ioEx) {
			// TODO log
			this.pumaSocket = null;
		}
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

	/**
	 * @return the database
	 */
	public String getDatabase() {
		return database;
	}

	/**
	 * @param database
	 *            the database to set
	 */
	public void setDatabase(String database) {
		this.database = database;
	}

	/**
	 * @return the parser
	 */
	public Parser getParser() {
		return parser;
	}

	/**
	 * @param parser
	 *            the parser to set
	 */
	public void setParser(Parser parser) {
		this.parser = parser;
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

}
