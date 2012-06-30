/**
 * Project: ${puma-server.aid}
 * 
 * File Created at 2012-6-21
 * $Id$
 * 
 * Copyright 2010 dianping.com.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Dianping Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with dianping.com.
 */
package com.dianping.puma.server.impl;

import java.util.List;
import java.util.Map;

import com.dianping.hawk.jmx.HawkJMXUtil;
import com.dianping.puma.common.annotation.ThreadUnSafe;
import com.dianping.puma.common.bo.PumaContext;
import com.dianping.puma.common.monitor.BinlogInfoAware;
import com.dianping.puma.datahandler.DataHandler;
import com.dianping.puma.parser.Parser;
import com.dianping.puma.sender.Sender;
import com.dianping.puma.sender.dispatcher.Dispatcher;
import com.dianping.puma.server.Server;
import com.dianping.puma.server.monitor.ServerMonitorMBean;

/**
 * TODO Comment of AbstractServer
 * 
 * @author Leo Liang
 * 
 */
@ThreadUnSafe
public abstract class AbstractServer implements Server {
	protected PumaContext		context;
	protected String			defaultBinlogFileName;
	protected Long				defaultBinlogPosition;
	protected Parser			parser;
	protected DataHandler		dataHandler;
	protected Dispatcher		dispatcher;
	protected long				serverId	= 6789;
	protected volatile boolean	stop		= false;

	public void setContext(PumaContext context) {
		this.context = context;
	}

	public PumaContext getContext() {
		return context;
	}

	public String getDefaultBinlogFileName() {
		return defaultBinlogFileName;
	}

	public void setDefaultBinlogFileName(String binlogFileName) {
		this.defaultBinlogFileName = binlogFileName;
	}

	/**
	 * @return the defaultBinlogPosition
	 */
	public Long getDefaultBinlogPosition() {
		return defaultBinlogPosition;
	}

	/**
	 * @param defaultBinlogPosition
	 *            the defaultBinlogPosition to set
	 */
	public void setDefaultBinlogPosition(Long defaultBinlogPosition) {
		this.defaultBinlogPosition = defaultBinlogPosition;
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

	/**
	 * @return the dataHandler
	 */
	public DataHandler getDataHandler() {
		return dataHandler;
	}

	/**
	 * @param dataHandler
	 *            the dataHandler to set
	 */
	public void setDataHandler(DataHandler dataHandler) {
		this.dataHandler = dataHandler;
	}

	/**
	 * @return the dispatcher
	 */
	public Dispatcher getDispatcher() {
		return dispatcher;
	}

	/**
	 * @param dispatcher
	 *            the dispatcher to set
	 */
	public void setDispatcher(Dispatcher dispatcher) {
		this.dispatcher = dispatcher;
	}

	/**
	 * @return the serverId
	 */
	public long getServerId() {
		return serverId;
	}

	/**
	 * @param serverId
	 *            the serverId to set
	 */
	public void setServerId(long serverId) {
		this.serverId = serverId;
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.puma.server.Server#stop()
	 */
	@Override
	public void stop() throws Exception {
		doStop();
		stop = true;

		parser.stop();
		dataHandler.stop();
		dispatcher.stop();
	}

	protected abstract void doStop() throws Exception;

	protected abstract void doStart() throws Exception;

	public void start() throws Exception {
		ServerMonitorMBean smb = new ServerMonitorMBean();
		smb.setServerName(getServerName());
		initMonitorMBeanAdditionInfo(smb);
		if (parser instanceof BinlogInfoAware) {
			smb.addBinlogInfo((BinlogInfoAware) parser);
		}

		if (dataHandler instanceof BinlogInfoAware) {
			smb.addBinlogInfo((BinlogInfoAware) dataHandler);
		}

		if (dispatcher instanceof BinlogInfoAware) {
			smb.addBinlogInfo((BinlogInfoAware) dispatcher);
		}

		List<Sender> senders = dispatcher.getSenders();
		if (senders != null && !senders.isEmpty()) {
			for (Sender sender : senders) {
				if (sender instanceof BinlogInfoAware) {
					smb.addBinlogInfo((BinlogInfoAware) sender);
				}
			}
		}

		HawkJMXUtil.registerMBean(smb);

		doStart();
	}

	public abstract void initMonitorMBeanAdditionInfo(ServerMonitorMBean smb);
}
