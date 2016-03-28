/**
 * Project: ${puma-server.aid}
 * <p/>
 * File Created at 2012-6-21
 * $Id$
 * <p/>
 * Copyright 2010 dianping.com.
 * All rights reserved.
 * <p/>
 * This software is the confidential and proprietary information of
 * Dianping Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with dianping.com.
 */
package com.dianping.puma.taskexecutor;

import com.dianping.puma.annotation.ThreadUnSafe;
import com.dianping.puma.biz.entity.PumaTaskStateEntity;
import com.dianping.puma.common.PumaContext;
import com.dianping.puma.datahandler.DataHandler;
import com.dianping.puma.instance.InstanceManager;
import com.dianping.puma.model.TableSet;
import com.dianping.puma.parser.Parser;
import com.dianping.puma.sender.Sender;
import com.dianping.puma.sender.dispatcher.Dispatcher;
import com.dianping.puma.storage.manage.InstanceStorageManager;

import java.util.Date;
import java.util.List;

/**
 *
 * @author Leo Liang
 */
@ThreadUnSafe
public abstract class AbstractTaskExecutor implements TaskExecutor {
	private PumaContext context;

	private String taskId;

	private long serverId;

	protected String taskName;

	protected Date beginTime;

	protected TableSet tableSet;

	private String defaultBinlogFileName;

	private Long defaultBinlogPosition;

	protected Parser parser;

	protected DataHandler dataHandler;

	protected Dispatcher dispatcher;

	private volatile boolean stop = true;

	protected InstanceStorageManager instanceStorageManager;

	protected PumaTaskStateEntity state;

	protected InstanceManager instanceManager;

	@Override
	public String getTaskId() {
		return taskId;
	}

	@Override
	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}

	@Override
	public String getTaskName() {
		return taskName;
	}

	@Override
	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	/**
	 * @param instanceStorageManager
	 *           the binlogPositionHolder to set
	 */
	public void setInstanceStorageManager(InstanceStorageManager instanceStorageManager) {
		this.instanceStorageManager = instanceStorageManager;
	}

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
	 *           the defaultBinlogPosition to set
	 */
	public void setDefaultBinlogPosition(Long defaultBinlogPosition) {
		this.defaultBinlogPosition = defaultBinlogPosition;
	}

	/**
	 * @param parser
	 *           the parser to set
	 */
	public void setParser(Parser parser) {
		this.parser = parser;
	}

	/**
	 * @param dataHandler
	 *           the dataHandler to set
	 */
	public void setDataHandler(DataHandler dataHandler) {
		this.dataHandler = dataHandler;
	}

	/**
	 * @param dispatcher
	 *           the dispatcher to set
	 */
	public void setDispatcher(Dispatcher dispatcher) {
		this.dispatcher = dispatcher;
	}

	public long getServerId() {
		return serverId;
	}

	public void setServerId(long serverId) {
		this.serverId = serverId;
	}

	public boolean isStop() {
		return stop;
	}

	protected abstract void doStop() throws Exception;

	protected abstract void doStart() throws Exception;

	@Override
	public void start() {
		try {
			stop = false;

			parser.start();
			dataHandler.start();
			dispatcher.start();
			doStart();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void stop() {
		try {
			stop = true;

			parser.stop();
			dataHandler.stop();
			dispatcher.stop();

			doStop();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void resume() throws Exception {
		stop = false;
	}

	public void pause() throws Exception {
		stop = true;
	}

	@Override
	public List<Sender> getFileSender() {
		return dispatcher.getSenders();
	}

	@Override
	public DataHandler getDataHandler() {
		return this.dataHandler;
	}

	public PumaTaskStateEntity getTaskState() {
		return state;
	}

	public void setTaskState(PumaTaskStateEntity state) {
		this.state = state;
	}

	public Date getBeginTime() {
		return beginTime;
	}

	public void setBeginTime(Date beginTime) {
		this.beginTime = beginTime;
	}

	public TableSet getTableSet() {
		return tableSet;
	}

	public void setTableSet(TableSet tableSet) {
		this.tableSet = tableSet;
	}

	public InstanceManager getInstanceManager() {
		return instanceManager;
	}

	public void setInstanceManager(InstanceManager instanceManager) {
		this.instanceManager = instanceManager;
	}
}
