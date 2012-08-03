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

import com.dianping.puma.bo.PumaContext;
import com.dianping.puma.common.LifeCycle;

/**
 * @author Leo Liang
 * 
 */
public interface Server extends LifeCycle<Exception> {

	public void setContext(PumaContext context);

	public PumaContext getContext();

	public String getDefaultBinlogFileName();

	public void setDefaultBinlogFileName(String binlogFileName);

	public Long getDefaultBinlogPosition();

	public void setDefaultBinlogPosition(Long binlogFileName);

	public void setServerId(long serverId);

	public long getServerId();

	public String getServerName();

	public void setBinlogPositionHolder(BinlogPositionHolder holder);
}
