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

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Message;
import com.dianping.puma.bo.PumaContext;
import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.core.model.BinlogStat;
import com.dianping.puma.monitor.FetcherEventCountMonitor;
import com.dianping.puma.monitor.ParserEventCountMonitor;
import com.dianping.puma.server.exception.ServerEventFetcherException;
import com.dianping.puma.server.exception.ServerEventParserException;
import com.dianping.puma.server.exception.ServerEventRuntimeException;

import org.apache.commons.lang.StringUtils;

import com.dianping.puma.common.SystemStatusContainer;
import com.dianping.puma.core.annotation.ThreadUnSafe;
import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.event.DdlEvent;
import com.dianping.puma.core.event.RowChangedEvent;
import com.dianping.puma.datahandler.DataHandlerResult;
import com.dianping.puma.parser.mysql.BinlogConstants;
import com.dianping.puma.parser.mysql.QueryExecutor;
import com.dianping.puma.parser.mysql.ResultSet;
import com.dianping.puma.parser.mysql.UpdateExecutor;
import com.dianping.puma.parser.mysql.event.BinlogEvent;
import com.dianping.puma.parser.mysql.event.RotateEvent;
import com.dianping.puma.parser.mysql.packet.AuthenticatePacket;
import com.dianping.puma.parser.mysql.packet.BinlogPacket;
import com.dianping.puma.parser.mysql.packet.ComBinlogDumpPacket;
import com.dianping.puma.parser.mysql.packet.OKErrorPacket;
import com.dianping.puma.parser.mysql.packet.PacketFactory;
import com.dianping.puma.parser.mysql.packet.PacketType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 基于MySQL复制机制的Server
 *
 * @author Leo Liang
 */
@ThreadUnSafe
public class DefaultTaskExecutor extends AbstractTaskExecutor {

	private static final Logger LOG = LoggerFactory.getLogger(DefaultTaskExecutor.class);

	private int port = 3306;

	private String dbHost;

	private String dbUsername;

	private long dbServerId;

	private String dbPassword;

	private String database;

	private String encoding = "utf-8";

	private Socket pumaSocket;

	private InputStream is;

	private OutputStream os;

	private FetcherEventCountMonitor fetcherEventCountMonitor;

	private ParserEventCountMonitor parserEventCountMonitor;

	@Override
	public void doStart() throws Exception {
		long failCount = 0;
		boolean isNeedStop = false;
		do {
			try {
				// 读position/file文件
				BinlogInfo binlogInfo = binlogInfoHolder.getBinlogInfo(getContext().getPumaServerName());
				if (binlogInfo == null) {
					binlogInfo = new BinlogInfo();
					binlogInfo.setBinlogFile(getContext().getBinlogFileName());
					binlogInfo.setBinlogPosition(getContext().getBinlogStartPos());
				}

				getContext().setBinlogFileName(binlogInfo.getBinlogFile());
				getContext().setBinlogStartPos(binlogInfo.getBinlogPosition());
				getContext().setDBServerId(dbServerId);
				getContext().setMasterUrl(dbHost, port);

				setBinlogInfo(binlogInfo);

				SystemStatusContainer.instance.updateServerStatus(getTaskName(), dbHost, port, database, getContext()
						.getBinlogFileName(), getContext().getBinlogStartPos());
				if (!connect()) {
					throw new IOException("connection failed.");
				}
				
				isNeedStop = true;
				if (!auth()) {
					throw new IOException("Login failed.");
				}
				
				if (getContext().isCheckSum()) {
					if (!updateSetting()) {
						throw new IOException("update setting command failed.");
					}
				}
				if (!queryConfig()) {
					throw new IOException("query config binlogformat failed.");
				}
				
				if (dumpBinlog()) {
					isNeedStop = false;
					processBinlog();
				} else{
					throw new IOException("binlog dump failed.");
				}
			} catch (Throwable e) {
				if (isNeedStop) {
					Cat.logError("Puma.server.failed", new ServerEventFetcherException(e));
					stopTask();
				}
				if (e instanceof RuntimeException) {
					Cat.logError("Puma.server.runtimeException", new ServerEventRuntimeException(e));
				}
				if (isStop()) {
					return;
				}
				if (++failCount % 3 == 0) {
					failCount = 0;
				}
				LOG.error("Exception occurs. taskName: " + getTaskName() + " dbServerId: " + getDbServerId()
						+ ". Reconnect...", e);
				Thread.sleep(((failCount % 10) + 1) * 2000);
			}
		} while (!isStop());

	}

	private void processBinlog() throws IOException {
		while (!isStop()) {
			// LOG.info("Enter `processBinlog` infinite loop!");

			// only slow down parsing, not stop
			if (SystemStatusContainer.instance.isStopTheWorld(this.getTaskName())) {
				try {
					TimeUnit.SECONDS.sleep(1);
				} catch (InterruptedException e) {
					// ignore
				}
			}

			BinlogPacket binlogPacket = (BinlogPacket) PacketFactory.parsePacket(is, PacketType.BINLOG_PACKET,
					getContext());
			if (!binlogPacket.isOk()) {
				LOG.error("Binlog packet response error.");
				throw new IOException("Binlog packet response error.");
			} else {
				processBinlogPacket(binlogPacket);
			}

		}

		// LOG.info("Exit `processBinlog` infinite loop!");
	}

	protected void processBinlogPacket(BinlogPacket binlogPacket) throws IOException {
		fetcherEventCountMonitor.record(getTaskName());
		BinlogEvent binlogEvent = parser.parse(binlogPacket.getBinlogBuf(), getContext());
		if (binlogEvent.getHeader().getEventType() == BinlogConstants.INTVAR_EVENT
				|| binlogEvent.getHeader().getEventType() == BinlogConstants.RAND_EVENT
				|| binlogEvent.getHeader().getEventType() == BinlogConstants.USER_VAR_EVENT) {
			LOG.error("binlog_format is MIXED or STATEMENT ,System is not support.");
			String eventName = String.format("slave(%s) ===> db(%s:%d)", getTaskName(), dbHost, port);
			Cat.logEvent("Slave.dbBinlogFormat", eventName, "1", "");
			Cat.logError("Puma.server.mixedorstatement.format", new ServerEventParserException(
					"binlog_format is MIXED or STATEMENT ,System is not support."));
			stopTask();
		}

		if (binlogEvent.getHeader().getEventType() != BinlogConstants.FORMAT_DESCRIPTION_EVENT) {
			getContext().setNextBinlogPos(binlogEvent.getHeader().getNextPosition());
		}
		if (binlogEvent.getHeader().getEventType() == BinlogConstants.ROTATE_EVENT) {
			processRotateEvent(binlogEvent);
		} else {
			processDataEvent(binlogEvent);
		}
	}

	protected void processDataEvent(BinlogEvent binlogEvent) {
		DataHandlerResult dataHandlerResult = null;
		// 一直处理一个binlogEvent的多行，处理完每行马上分发，以防止一个binlogEvent包含太多ChangedEvent而耗费太多内存
		do {
			dataHandlerResult = dataHandler.process(binlogEvent, getContext());
			if (dataHandlerResult != null && !dataHandlerResult.isEmpty()) {
				parserEventCountMonitor.record(getTaskName());

				ChangedEvent changedEvent = dataHandlerResult.getData();

				updateOpsCounter(changedEvent);

				dispatch(changedEvent);
			}
		} while (dataHandlerResult != null && !dataHandlerResult.isFinished());

		if (binlogEvent.getHeader().getEventType() != BinlogConstants.FORMAT_DESCRIPTION_EVENT) {
			getContext().setBinlogStartPos(binlogEvent.getHeader().getNextPosition());
			setBinlogInfo(new BinlogInfo(getBinlogInfo().getBinlogFile(), binlogEvent.getHeader().getNextPosition()));
		}

		// status report
		SystemStatusContainer.instance.updateServerStatus(getTaskName(), dbHost, port, database, getContext()
				.getBinlogFileName(), getContext().getBinlogStartPos());

		// 只有整个binlogEvent分发完了才save
		if (binlogEvent.getHeader() != null
				&& binlogEvent.getHeader().getNextPosition() != 0
				&& StringUtils.isNotBlank(getContext().getBinlogFileName())
				&& dataHandlerResult != null
				&& !dataHandlerResult.isEmpty()
				&& (dataHandlerResult.getData() instanceof DdlEvent || (dataHandlerResult.getData() instanceof RowChangedEvent && ((RowChangedEvent) dataHandlerResult
						.getData()).isTransactionCommit()))) {

			binlogInfoHolder.setBinlogInfo(getTaskName(), new BinlogInfo(getContext().getBinlogFileName(), binlogEvent
					.getHeader().getNextPosition()));
		}
	}

	protected void dispatch(ChangedEvent changedEvent) {
		try {
			dispatcher.dispatch(changedEvent, getContext());
		} catch (Exception e) {
			this.notifyService.alarm("[" + getContext().getPumaServerName() + "]" + "Dispatch event failed. event("
					+ changedEvent + ")", e, true);
			LOG.error("Dispatcher dispatch failed.", e);
		}
	}

	protected void updateOpsCounter(ChangedEvent changedEvent) {
		// 增加行变更计数器(除去ddl事件和事务信息事件)
		if ((changedEvent instanceof RowChangedEvent) && !((RowChangedEvent) changedEvent).isTransactionBegin()
				&& !((RowChangedEvent) changedEvent).isTransactionCommit()) {
			switch (((RowChangedEvent) changedEvent).getActionType()) {
			case RowChangedEvent.INSERT:
				incrRowsInsert();
				SystemStatusContainer.instance.incServerRowInsertCounter(getTaskName());
				break;
			case RowChangedEvent.UPDATE:
				incrRowsUpdate();
				SystemStatusContainer.instance.incServerRowUpdateCounter(getTaskName());
				break;
			case RowChangedEvent.DELETE:
				incrRowsDelete();
				SystemStatusContainer.instance.incServerRowDeleteCounter(getTaskName());
				break;
			default:
				break;
			}
		} else if (changedEvent instanceof DdlEvent) {
			incrDdls();
			SystemStatusContainer.instance.incServerDdlCounter(getTaskName());
		}
	}

	protected void processRotateEvent(BinlogEvent binlogEvent) {
		RotateEvent rotateEvent = (RotateEvent) binlogEvent;
		binlogInfoHolder.setBinlogInfo(getTaskName(),
				new BinlogInfo(rotateEvent.getNextBinlogFileName(), rotateEvent.getFirstEventPosition()));
		getContext().setBinlogFileName(rotateEvent.getNextBinlogFileName());
		getContext().setBinlogStartPos(rotateEvent.getFirstEventPosition());

		setBinlogInfo(new BinlogInfo(rotateEvent.getNextBinlogFileName(), rotateEvent.getFirstEventPosition()));
		// status report
		SystemStatusContainer.instance.updateServerStatus(getTaskName(), dbHost, port, database, getContext()
				.getBinlogFileName(), getContext().getBinlogStartPos());
	}

	/**
	 * Connect to mysql master and parse the greeting packet
	 *
	 * @throws IOException
	 */
	private boolean connect() {
		try {
			closeTransport();
			this.pumaSocket = new Socket();
			this.pumaSocket.setTcpNoDelay(false);
			this.pumaSocket.setKeepAlive(true);
			this.pumaSocket.connect(new InetSocketAddress(dbHost, port));
			is = new BufferedInputStream(pumaSocket.getInputStream());
			os = new BufferedOutputStream(pumaSocket.getOutputStream());
			PacketFactory.parsePacket(is, PacketType.CONNECT_PACKET, getContext());
			LOG.info("connection db success.");
			return true;
		} catch (Exception e) {
			LOG.error("connect failed. Reason: " + e.getMessage());
			return false;
		}
	}

	/**
	 * Send COM_BINLOG_DUMP packet to mysql master and parse the response
	 *
	 * @return
	 * @throws IOException
	 */
	private boolean dumpBinlog() {
		try {
			ComBinlogDumpPacket dumpBinlogPacket = (ComBinlogDumpPacket) PacketFactory.createCommandPacket(
					PacketType.COM_BINLOG_DUMP_PACKET, getContext());
			dumpBinlogPacket.setBinlogFileName(getContext().getBinlogFileName());
			dumpBinlogPacket.setBinlogFlag(0);
			dumpBinlogPacket.setBinlogPosition(getContext().getBinlogStartPos());
			dumpBinlogPacket.setServerId(getServerId());
			dumpBinlogPacket.buildPacket(getContext());

			dumpBinlogPacket.write(os, getContext());

			OKErrorPacket dumpCommandResultPacket = (OKErrorPacket) PacketFactory.parsePacket(is,
					PacketType.OKERROR_PACKET, getContext());

			if (dumpCommandResultPacket.isOk()) {
				if (StringUtils.isBlank(getContext().getBinlogFileName())
						&& StringUtils.isNotBlank(dumpCommandResultPacket.getMessage())) {
					String msg = dumpCommandResultPacket.getMessage();
					int startPos = msg.lastIndexOf(' ');
					if (startPos != -1) {
						startPos += 1;
					} else {
						startPos = 0;
					}
					String binlogFile = dumpCommandResultPacket.getMessage().substring(startPos);
					binlogInfoHolder.setBinlogInfo(getTaskName(), new BinlogInfo(binlogFile, getContext()
							.getBinlogStartPos()));
					getContext().setBinlogFileName(binlogFile);
				}
				LOG.info("dump binlog command success.");
				return true;
			} else {
				LOG.error("Dump binlog failed. Reason: " + dumpCommandResultPacket.getMessage());
				return false;
			}
		} catch (Exception e) {
			LOG.error("Dump binlog failed. Reason: " + e.getMessage());
			return false;
		}

	}

	/**
	 * Send Authentication Packet to mysql master and parse the response
	 *
	 * @return
	 * @throws IOException
	 */
	private boolean auth() {
		try {
			// auth
			LOG.info("server logining taskName: " + getTaskName() + " host: " + dbHost + " port: " + port + " username: "
					+ dbUsername + " database: " + database + " dbServerId: " + getDbServerId());
			AuthenticatePacket authPacket = (AuthenticatePacket) PacketFactory.createCommandPacket(
					PacketType.AUTHENTICATE_PACKET, getContext());

			authPacket.setPassword(dbPassword);
			authPacket.setUser(dbUsername);
			authPacket.setDatabase(database);
			authPacket.buildPacket(getContext());
			authPacket.write(os, getContext());

			OKErrorPacket okErrorPacket = (OKErrorPacket) PacketFactory.parsePacket(is, PacketType.OKERROR_PACKET,
					getContext());
			boolean isAuth;

			if (okErrorPacket.isOk()) {
				LOG.info("server login success.");
				isAuth = true;
			} else {
				isAuth = false;
				LOG.error("Login failed. Reason: " + okErrorPacket.getMessage());
			}
			return isAuth;
		} catch (Exception e) {
			LOG.error("Login failed. Reason: " + e.getMessage());
			return false;
		}
	}

	/**
	 * Send QueryCommand Packet to update binlog_checksum
	 * 
	 * @return
	 * @throws IOException
	 */
	private boolean updateSetting() throws IOException {
		try {
			UpdateExecutor executor = new UpdateExecutor(is, os);
			String cmd = "set @master_binlog_checksum= '@@global.binlog_checksum'";
			OKErrorPacket okErrorPacket = executor.update(cmd, getContext());
			String eventStatus;
			String eventName = String.format("slave(%s) ===> db(%s:%d)", getTaskName(), dbHost, port);
			if (okErrorPacket.isOk()) {
				eventStatus = Message.SUCCESS;
				LOG.info("update setting command success.");
			} else {
				eventStatus = "1";
				LOG.error("updateSetting failed. Reason: " + okErrorPacket.getMessage());
			}

			Cat.logEvent("Slave.dbSetCheckSum", eventName, eventStatus, "");
			return okErrorPacket.isOk();
		} catch (Exception e) {
			LOG.error("updateSetting failed. Reason: " + e.getMessage());
			return false;
		}
	}

	/**
	 * Send QueryCommand Packet to query binlog_format
	 * 
	 * @return
	 * @throws IOException
	 */
	private boolean queryConfig() throws IOException {
		try {
			QueryExecutor executor = new QueryExecutor(is, os);
			String cmd = "show global variables like 'binlog_format'";
			ResultSet rs = executor.query(cmd, getContext());
			List<String> columnValues = rs.getFiledValues();
			boolean isQuery = true;
			if (columnValues == null || columnValues.size() != 2 || columnValues.get(1) == null) {
				LOG.error("queryConfig failed Reason:unexcepted binlog format query result.");
				isQuery = false;
			}
			BinlogFormat binlogFormat = BinlogFormat.valuesOf(columnValues.get(1));
			String eventName = String.format("slave(%s) ===> db(%s:%d)", getTaskName(), dbHost, port);
			if (binlogFormat == null || !binlogFormat.isRow()) {
				isQuery = false;
				LOG.error("unexcepted binlog format: " + binlogFormat.value);
			}

			Cat.logEvent("Slave.dbBinlogFormat", eventName, isQuery ? Message.SUCCESS : "1", "");
			if(isQuery){
				LOG.info("query config binlogformat is legal.");
			}
			return isQuery;
		} catch (Exception e) {
			LOG.error("queryConfig failed Reason: " + e.getMessage());
			return false;
		}
	}

	protected void doStop() throws Exception {
		closeTransport();
	}

	private void stopTask() {
		String eventName = String.format("slave(%s) ===> db(%s:%d)", getTaskName(), dbHost, port);
		try {
			DefaultTaskExecutorContainer.instance.stopExecutor(this);
			Cat.logEvent("Slave.doStop", eventName, Message.SUCCESS, "");
		} catch (Exception e) {
			LOG.error("task " + getTaskName() + "stop error.");
			Cat.logEvent("Slave.doStop", eventName, "1", "");
		}
	}

	private void closeTransport() {
		// Close in.
		try {
			if (this.is != null) {
				this.is.close();
			}
		} catch (IOException ioEx) {
			LOG.warn("Server " + this.getTaskName() + " failed to close the input stream.");
		} finally {
			this.is = null;
		}

		// Close os.
		try {
			if (this.os != null) {
				this.os.close();
			}
		} catch (IOException ioEx) {
			LOG.warn("Server " + this.getTaskName() + " failed to close the output stream");
		} finally {
			this.os = null;
		}

		// Close socket.
		try {
			if (this.pumaSocket != null) {
				this.pumaSocket.close();
			}
		} catch (IOException ioEx) {
			LOG.warn("Server " + this.getTaskName() + " failed to close the socket", ioEx);
		} finally {
			this.pumaSocket = null;
		}
	}

	public void initContext() {
		PumaContext context = new PumaContext();

		BinlogInfo binlogInfo = binlogInfoHolder.getBinlogInfo(this.getTaskName());

		if (binlogInfo == null) {
			context.setBinlogStartPos(this.getBinlogInfo().getBinlogPosition());
			context.setBinlogFileName(this.getBinlogInfo().getBinlogFile());
		} else {
			context.setBinlogFileName(binlogInfo.getBinlogFile());
			context.setBinlogStartPos(binlogInfo.getBinlogPosition());
		}

		// context.setPumaServerId(taskExecutor.getServerId());
		context.setPumaServerName(this.getTaskName());
		this.setContext(context);
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getDBHost() {
		return dbHost;
	}

	public void setDBHost(String dbHost) {
		this.dbHost = dbHost;
	}

	public String getDBUsername() {
		return dbUsername;
	}

	public void setDBUsername(String dbUsername) {
		this.dbUsername = dbUsername;
	}

	public String getDBPassword() {
		return dbPassword;
	}

	public void setDBPassword(String dbPassword) {
		this.dbPassword = dbPassword;
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
	 * @return the dbServerId
	 */
	public long getDbServerId() {
		return dbServerId;
	}

	/**
	 * @param dbServerId
	 *            the dbServerId to set
	 */
	public void setDbServerId(long dbServerId) {
		this.dbServerId = dbServerId;
	}

	public BinlogInfo getBinlogInfo() {
		return this.state.getBinlogInfo();
	}

	public void setBinlogInfo(BinlogInfo binlogInfo) {
		this.state.setBinlogInfo(binlogInfo);
	}

	public BinlogStat getBinlogStat() {
		return this.state.getBinlogStat();
	}

	public void setBinlogStat(BinlogStat binlogStat) {
		this.state.setBinlogStat(binlogStat);
	}

	public void incrRowsInsert() {
		Long rowsInsert = this.state.getBinlogStat().getRowsInsert();
		this.getBinlogStat().setRowsInsert(rowsInsert + 1);
	}

	public void incrRowsUpdate() {
		Long rowsUpdate = this.state.getBinlogStat().getRowsUpdate();
		this.state.getBinlogStat().setRowsUpdate(rowsUpdate + 1);
	}

	public void incrRowsDelete() {
		Long rowsDelete = this.state.getBinlogStat().getRowsDelete();
		this.state.getBinlogStat().setRowsDelete(rowsDelete + 1);
	}

	public void incrDdls() {
		Long ddls = this.state.getBinlogStat().getDdls();
		this.state.getBinlogStat().setDdls(ddls + 1);
	}

	public void setFetcherEventCountMonitor(FetcherEventCountMonitor fetcherEventCountMonitor) {
		this.fetcherEventCountMonitor = fetcherEventCountMonitor;
	}

	public void setParserEventCountMonitor(ParserEventCountMonitor parserEventCountMonitor) {
		this.parserEventCountMonitor = parserEventCountMonitor;
	}

	public static enum BinlogFormat {
		STATEMENT("STATEMENT"), ROW("ROW"), MIXED("MIXED");

		private String value;

		private BinlogFormat(String value) {
			this.value = value;
		}

		public boolean isRow() {
			return this == ROW;
		}

		public boolean isStatement() {
			return this == STATEMENT;
		}

		public boolean isMixed() {
			return this == MIXED;
		}

		public static BinlogFormat valuesOf(String value) {
			BinlogFormat[] formats = values();
			for (BinlogFormat format : formats) {
				if (format.value.equalsIgnoreCase(value)) {
					return format;
				}
			}
			return null;
		}
	}
}
