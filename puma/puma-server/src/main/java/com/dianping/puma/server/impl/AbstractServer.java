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

import com.dianping.puma.common.bo.PumaContext;
import com.dianping.puma.server.Server;

/**
 * TODO Comment of AbstractServer
 * 
 * @author Leo Liang
 * 
 */
public abstract class AbstractServer implements Server {
	protected PumaContext	context;
	protected String		defaultBinlogFileName;
	protected Long			defaultBinlogPosition;

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

}
