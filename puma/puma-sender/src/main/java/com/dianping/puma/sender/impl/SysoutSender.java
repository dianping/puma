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
import com.dianping.puma.sender.Sender;

/**
 * TODO Comment of SysoutSender
 * 
 * @author Leo Liang
 * 
 */
public class SysoutSender implements Sender {
	private String	name;

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
	 * @see com.dianping.puma.sender.Sender#getName()
	 */
	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.puma.sender.Sender#send(com.dianping.puma.client.
	 * DataChangedEvent, com.dianping.puma.common.bo.PumaContext)
	 */
	@Override
	public void send(DataChangedEvent event, PumaContext context) throws Exception {
		System.out.println(event);
	}

}
