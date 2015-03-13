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

import java.util.List;

import com.dianping.puma.bo.PumaContext;
import com.dianping.puma.core.LifeCycle;
import com.dianping.puma.core.constant.Status;
import com.dianping.puma.core.holder.BinlogInfoHolder;
import com.dianping.puma.sender.Sender;

/**
 * @author Leo Liang
 * 
 */
public interface TaskExecutor extends LifeCycle<Exception> {

	public void setContext(PumaContext context);

	public void initContext();

	public PumaContext getContext();

	public String getTaskId();

	public void setTaskId(String taskId);

	public String getTaskName();

	public void setTaskName(String taskName);

	public String getDefaultBinlogFileName();

	public void setDefaultBinlogFileName(String binlogFileName);

	public Long getDefaultBinlogPosition();

	public void setDefaultBinlogPosition(Long binlogFileName);

	//public void setServerId(long serverId);

	//public long getServerId();

	//public String getServerName();

	public void setBinlogInfoHolder(BinlogInfoHolder holder);

	public Status getStatus();

	public void setStatus(Status status);

	public List<Sender> getFileSender();

}
