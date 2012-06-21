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

import com.dianping.puma.server.Server;

/**
 * TODO Comment of AbstractServer
 * 
 * @author Leo Liang
 * 
 */
public abstract class AbstractServer implements Server {
	protected String	binlogFileName;
	protected long		binlogPosition;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.puma.server.Server#setBinlogFileName(java.lang.String)
	 */
	@Override
	public void setBinlogFileName(String binlogFileName) {
		this.binlogFileName = binlogFileName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.puma.server.Server#setBinlogPosition(long)
	 */
	@Override
	public void setBinlogPosition(long pos) {
		this.binlogPosition = pos;
	}

}
