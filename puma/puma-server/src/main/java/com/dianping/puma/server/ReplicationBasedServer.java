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
package com.dianping.puma.server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.dianping.puma.bo.PositionInfo;
import com.dianping.puma.core.annotation.ThreadUnSafe;
import com.dianping.puma.core.event.DdlEvent;
import com.dianping.puma.core.event.RowChangedEvent;
import com.dianping.puma.datahandler.DataHandlerResult;
import com.dianping.puma.parser.mysql.BinlogConstanst;
import com.dianping.puma.parser.mysql.event.BinlogEvent;
import com.dianping.puma.parser.mysql.event.RotateEvent;
import com.dianping.puma.parser.mysql.packet.AuthenticatePacket;
import com.dianping.puma.parser.mysql.packet.BinlogPacket;
import com.dianping.puma.parser.mysql.packet.ComBinlogDumpPacket;
import com.dianping.puma.parser.mysql.packet.OKErrorPacket;
import com.dianping.puma.parser.mysql.packet.PacketFactory;
import com.dianping.puma.parser.mysql.packet.PacketType;
import com.dianping.puma.server.monitor.ServerMonitorMBean;
import com.dianping.puma.utils.PositionFileUtils;

/**
 * 基于MySQL复制机制的Server
 * 
 * @author Leo Liang
 * 
 */
@ThreadUnSafe
public class ReplicationBasedServer extends AbstractServer {

	private static final Logger	log			= Logger.getLogger(ReplicationBasedServer.class);
	private int					port		= 3306;
	private String				host;
	private String				user;
	private String				password;
	private String				database;

	private String				encoding	= "utf-8";
	private Socket				pumaSocket;
	private InputStream			is;
	private OutputStream		os;

	@Override
	public void doStart() throws Exception {

		do {
			try {
				// 读position/file文件
				PositionInfo posInfo = PositionFileUtils.getPositionInfo(context.getPumaServerName(),
						context.getBinlogFileName(), context.getBinlogStartPos());

				context.setBinlogFileName(posInfo.getBinlogFileName());
				context.setBinlogStartPos(posInfo.getBinlogPosition());
				context.setServerId(serverId);

				connect();

				if (auth()) {
					log.info("Server logined... serverId: " + serverId + " host: " + host + " port: " + port
							+ " user: " + user + " database: " + database);

					if (dumpBinlog()) {
						log.info("Dump binlog command success.");

						processBinlog();

					} else {
						throw new IOException("Dump binlog failed.");
					}

				} else {
					throw new IOException("Login failed.");
				}
			} catch (Exception e) {
				log.error("Exception occurs. serverId: " + serverId + ". Reconnect...", e);
				Thread.sleep(1000);
			}
		} while (!stop);

	}

	private void processBinlog() throws IOException {
		while (!stop) {
			BinlogPacket binlogPacket = (BinlogPacket) PacketFactory.parsePacket(is, PacketType.BINLOG_PACKET, context);
			if (!binlogPacket.isOk()) {
				log.error("Binlog packet response error.");
				throw new IOException("Binlog packet response error.");
			} else {
				BinlogEvent binlogEvent = parser.parse(binlogPacket.getBinlogBuf(), context);

				if (binlogEvent.getHeader().getEventType() == BinlogConstanst.ROTATE_EVENT) {
					RotateEvent rotateEvent = (RotateEvent) binlogEvent;
					PositionFileUtils.savePositionInfo(getServerName(),
							new PositionInfo(rotateEvent.getFirstEventPosition(), rotateEvent.getNextBinlogFileName()));
					context.setBinlogFileName(rotateEvent.getNextBinlogFileName());
					context.setBinlogStartPos(rotateEvent.getFirstEventPosition());
				} else {
					DataHandlerResult dataHandlerResult = null;
					// 一直处理一个binlogEvent的多行，处理完每行马上分发，以防止一个binlogEvent包含太多ChangedEvent而耗费太多内存
					do {
						dataHandlerResult = dataHandler.process(binlogEvent, context);
						if (dataHandlerResult != null && !dataHandlerResult.isEmpty()) {
							try {
								dispatcher.dispatch(dataHandlerResult.getData(), context);
							} catch (Exception e) {
								log.error("Dispatcher dispatch failed.", e);
							}
						}
					} while (dataHandlerResult != null && !dataHandlerResult.isFinished());

					// 只有整个binlogEvent分发完了才save
					if (binlogEvent.getHeader() != null
							&& binlogEvent.getHeader().getNextPosition() != 0
							&& StringUtils.isNotBlank(context.getBinlogFileName())
							&& dataHandlerResult != null
							&& !dataHandlerResult.isEmpty()
							&& (dataHandlerResult.getData() instanceof DdlEvent || (dataHandlerResult.getData() instanceof RowChangedEvent && ((RowChangedEvent) dataHandlerResult
									.getData()).isTransactionCommit()))) {
						// save position
						PositionFileUtils.savePositionInfo(getServerName(), new PositionInfo(binlogEvent.getHeader()
								.getNextPosition(), context.getBinlogFileName()));
						context.setBinlogStartPos(binlogEvent.getHeader().getNextPosition());
					}

				}
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
			if (StringUtils.isBlank(context.getBinlogFileName())
					&& StringUtils.isNotBlank(dumpCommandResultPacket.getMessage())) {
				String msg = dumpCommandResultPacket.getMessage();
				int startPos = msg.lastIndexOf(' ');
				if (startPos != -1) {
					startPos += 1;
				} else {
					startPos = 0;
				}
				String binlogFile = dumpCommandResultPacket.getMessage().substring(startPos);
				PositionFileUtils.savePositionInfo(getServerName(), new PositionInfo(context.getBinlogStartPos(),
						binlogFile));
				context.setBinlogFileName(binlogFile);
			}
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

	protected void doStop() throws Exception {
		closeTransport();
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

			log.info("Server " + this.getServerName() + " failed to close the inputstream.");
		} finally {
			this.is = null;
		}
		try {
			if (this.os != null) {
				this.os.close();
			}
		} catch (IOException ioEx) {
			log.info("Server " + this.getServerName() + " failed to close the outputstream");

		} finally {
			this.os = null;
		}
		try {
			if (this.pumaSocket != null) {
				this.pumaSocket.close();
			}
		} catch (IOException ioEx) {
			log.error("Server " + this.getServerName() + " failed to close the socket", ioEx);

		} finally {
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dianping.puma.server.impl.AbstractServer#initMonitorMBean(com.dianping
	 * .puma.server.monitor.ServerMonitorMBean)
	 */
	@Override
	public void initMonitorMBeanAdditionInfo(ServerMonitorMBean smb) {
		smb.addAdditionInfo("host", host);
		smb.addAdditionInfo("port", String.valueOf(port));
		smb.addAdditionInfo("user", user);
		smb.addAdditionInfo("db", database);
	}

}
