/**
 * Project: ${puma-server.aid}
 * 
 * File Created at 2012-6-6 $Id$
 * 
 * Copyright 2010 dianping.com. All rights reserved.
 * 
 * This software is the confidential and proprietary information of Dianping
 * Company. ("Confidential Information"). You shall not disclose such
 * Confidential Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with dianping.com.
 */
package com.dianping.puma.server;

/**
 * @author Leo Liang
 * 
 */
public interface Server {
	/**
	 * 启动Server
	 * 
	 * @throws Exception
	 */
	public void start() throws Exception;

	/**
	 * 停止Server
	 * 
	 * @throws Exception
	 */
	public void stop() throws Exception;

	/**
	 * 
	 * @param binlogFileName
	 */
	public void setBinlogFileName(String binlogFileName);

	/**
	 * 
	 * @param pos
	 */
	public void setBinlogPosition(long pos);

	/**
	 * 
	 * @return
	 */
	public String getServerName();

	/**
	 * 
	 * @return
	 */
	public String getDefaultBinlogFileName();
}
