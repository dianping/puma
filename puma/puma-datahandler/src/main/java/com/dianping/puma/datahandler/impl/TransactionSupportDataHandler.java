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

import com.dianping.puma.client.DataChangedEvent;
import com.dianping.puma.common.mysql.event.BinlogEvent;
import com.dianping.puma.datahandler.DataHandler;

/**
 * TODO Comment of TransactionSupportDataHandler
 * 
 * @author Leo Liang
 * 
 */
public class TransactionSupportDataHandler implements DataHandler {

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.puma.common.LifeCycle#start()
	 */
	@Override
	public void start() throws Exception {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.puma.common.LifeCycle#stop()
	 */
	@Override
	public void stop() throws Exception {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dianping.puma.datahandler.DataHandler#process(com.dianping.puma.common
	 * .mysql.event.BinlogEvent)
	 */
	@Override
	public DataChangedEvent process(BinlogEvent binlogEvent) {
		// TODO Auto-generated method stub
		return null;
	}

}
