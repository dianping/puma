/**
 * Project: ${puma-server.aid}
 * <p/>
 * File Created at 2012-6-6 $Id$
 * <p/>
 * Copyright 2010 dianping.com. All rights reserved.
 * <p/>
 * This software is the confidential and proprietary information of Dianping
 * Company. ("Confidential Information"). You shall not disclose such
 * Confidential Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with dianping.com.
 */
package com.dianping.puma.taskexecutor;

import java.util.List;

import com.dianping.puma.common.PumaContext;
import com.dianping.puma.core.LifeCycle;
import com.dianping.puma.biz.entity.PumaTaskStateEntity;
import com.dianping.puma.datahandler.DataHandler;
import com.dianping.puma.sender.Sender;
import com.dianping.puma.storage.holder.BinlogInfoHolder;

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

	public void setBinlogInfoHolder(BinlogInfoHolder holder);

	public List<Sender> getFileSender();

	public DataHandler getDataHandler();

	public void resume() throws Exception;

	public void pause() throws Exception;

	public PumaTaskStateEntity getTaskState();

	public void setTaskState(PumaTaskStateEntity taskState);
}
