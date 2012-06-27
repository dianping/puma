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
package com.dianping.puma.sender.dispatcher.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.dianping.puma.client.DataChangedEvent;
import com.dianping.puma.common.bo.PumaContext;
import com.dianping.puma.sender.Sender;

/**
 * TODO Comment of SimpleDispatherImpl
 * 
 * @author Leo Liang
 * 
 */
public class SimpleDispatherImpl extends AbstractDispatcher {

	private static final Logger	log	= Logger.getLogger(SimpleDispatherImpl.class);
	private List<Sender>		senders;

	/**
	 * @return the senders
	 */
	public List<Sender> getSenders() {
		return senders;
	}

	/**
	 * @param senders
	 *            the senders to set
	 */
	public void setSenders(List<Sender> senders) {
		this.senders = senders;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dianping.puma.sender.dispatcher.Dispatcher#dispatch(com.dianping.
	 * puma.client.DataChangedEvent, com.dianping.puma.common.bo.PumaContext)
	 */
	@Override
	public void dispatch(DataChangedEvent event, PumaContext context) throws Exception {
		if (senders != null && senders.size() > 0) {
			List<Throwable> exceptionList = new ArrayList<Throwable>();
			for (Sender sender : senders) {
				try {
					sender.send(event, context);
				} catch (Throwable e) {
					log.error("Exception occurs in sender " + sender.getName());
					exceptionList.add(e);
				}
			}

			throwExceptionIfNeeded(exceptionList);
		} else {
			log.warn("No senders in dispatcher " + name);
		}
	}
}
