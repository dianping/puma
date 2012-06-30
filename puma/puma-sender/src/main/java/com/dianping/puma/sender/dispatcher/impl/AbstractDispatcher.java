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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import com.dianping.puma.common.monitor.BinlogInfoAware;
import com.dianping.puma.sender.dispatcher.Dispatcher;

/**
 * TODO Comment of AbstractDispatcher
 * 
 * @author Leo Liang
 * 
 */
public abstract class AbstractDispatcher implements Dispatcher, BinlogInfoAware {
	protected String	name;

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

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.puma.sender.dispatcher.Dispatcher#getName()
	 */
	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	protected void throwExceptionIfNeeded(List<Throwable> exceptionList) throws Exception {

		if (exceptionList != null && !exceptionList.isEmpty()) {
			StringWriter buffer = new StringWriter();
			PrintWriter out = null;
			try {
				out = new PrintWriter(buffer);

				for (Throwable exception : exceptionList) {
					exception.printStackTrace(out);
				}
			} finally {
				if (out != null) {
					out.close();
				}
			}

			throw new Exception(buffer.toString());
		}

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
}
