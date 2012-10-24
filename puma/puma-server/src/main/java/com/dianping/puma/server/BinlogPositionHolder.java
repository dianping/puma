/**
 * Project: puma-server
 * 
 * File Created at 2012-7-27
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
package com.dianping.puma.server;

import com.dianping.puma.bo.PositionInfo;

/**
 * @author Leo Liang
 * 
 */
public interface BinlogPositionHolder {
	public void setBaseDir(String baseDir);

	public PositionInfo getPositionInfo(String serverName, String defaultBinlogFile, Long defaultBinlogPos);

	public void savePositionInfo(String serverName, PositionInfo positionInfor);
}
