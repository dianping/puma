/**
 * Project: puma-server
 * 
 * File Created at 2012-7-7
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
package com.dianping.puma.sender.impl;

import com.dianping.puma.bo.PumaContext;
import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.storage.EventStorage;

/**
 * TODO Comment of FileDumpSender
 * 
 * @author Leo Liang
 * 
 */
public class FileDumpSender extends AbstractSender {
	private EventStorage	storage;
	private long			binlogPos;
	private String			binlogFile;

	/**
	 * @param storage
	 *            the storage to set
	 */
	public void setStorage(EventStorage storage) {
		this.storage = storage;
	}

	@Override
	public void stop() throws Exception {
		storage.close();
		super.stop();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.puma.common.monitor.BinlogInfoAware#getBinlogPos()
	 */
	@Override
	public long getBinlogPos() {
		return binlogPos;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.puma.common.monitor.BinlogInfoAware#getBinlogFile()
	 */
	@Override
	public String getBinlogFile() {
		return binlogFile;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.puma.common.monitor.Monitorable#getMonitorTargetName()
	 */
	@Override
	public String getMonitorTargetName() {
		return name;
	}

	@Override
	protected void doSend(ChangedEvent event, PumaContext context) throws Exception {
		binlogFile = context.getBinlogFileName();
		binlogPos = context.getBinlogStartPos();

		storage.store(event);
	}

}
