/**
 * Project: ${puma-sender.aid}
 * 
 * File Created at 2012-6-27
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

import com.dianping.puma.client.DataChangedEvent;
import com.dianping.puma.common.bo.PumaContext;

/**
 * For test only
 * 
 * @author Leo Liang
 * 
 */
public class SysoutSender extends AbstractSender {

	@Override
	protected void doSend(DataChangedEvent event, PumaContext context) {
		System.out.println(event);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.puma.common.monitor.BinlogInfoAware#getBinlogPos()
	 */
	@Override
	public long getBinlogPos() {
		// TODO Auto-generated method stub
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.puma.common.monitor.BinlogInfoAware#getBinlogFile()
	 */
	@Override
	public String getBinlogFile() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.puma.common.monitor.Monitorable#getMonitorTargetName()
	 */
	@Override
	public String getMonitorTargetName() {
		// TODO Auto-generated method stub
		return null;
	}

}
