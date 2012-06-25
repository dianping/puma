/**
 * Project: ${puma-datahandler.aid}
 * 
 * File Created at 2012-6-25
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
package com.dianping.puma.datahandler.impl;

import java.sql.Types;

import org.apache.log4j.Logger;

import com.dianping.puma.client.DataChangedEvent;
import com.dianping.puma.client.TableMetaInfo;
import com.dianping.puma.common.bo.PumaContext;
import com.dianping.puma.common.mysql.BinlogConstanst;
import com.dianping.puma.common.mysql.event.BinlogEvent;
import com.dianping.puma.common.mysql.event.PumaIgnoreEvent;
import com.dianping.puma.datahandler.DataHandler;

/**
 * TODO Comment of AbstractDataHandler
 * 
 * @author Leo Liang
 * 
 */
public abstract class AbstractDataHandler implements DataHandler {
	private static final Logger	log	= Logger.getLogger(AbstractDataHandler.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.puma.common.LifeCycle#start()
	 */
	@Override
	public void start() throws Exception {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.puma.common.LifeCycle#stop()
	 */
	@Override
	public void stop() throws Exception {

	}

	@Override
	public DataChangedEvent process(BinlogEvent binlogEvent, PumaContext context) {
		if (binlogEvent instanceof PumaIgnoreEvent) {
			log.info("Ingore one unknown event. eventType: " + binlogEvent.getHeader().getEventType());
		}

		byte eventType = binlogEvent.getHeader().getEventType();

		if (eventType == BinlogConstanst.STOP_EVENT || eventType == BinlogConstanst.ROTATE_EVENT) {
			return null;
		} else {
			return doProcess(binlogEvent, context, eventType);
		}
	}

	protected abstract DataChangedEvent doProcess(BinlogEvent binlogEvent, PumaContext context, byte eventType);

	protected TableMetaInfo getTableMetaInfo(String database, String table) {
		// TODO
		return null;
	}

	protected Types getColumnType(long columnId) {
		// TODO
		return null;
	}

	protected String getColumnName(long columnId) {
		// TODO
		return String.valueOf(columnId);
	}
}
